/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.calendar.widget;
import android.Manifest;
import android.content.pm.PackageManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Instances;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.android.calendar.R;
import com.android.calendar.Utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


public class CalendarAppWidgetService extends RemoteViewsService {
    private static final String TAG = "CalendarWidget";

    static final int EVENT_MIN_COUNT = 20;
    static final int EVENT_MAX_COUNT = 100;
    // Minimum delay between queries on the database for widget updates in ms
    static final int WIDGET_UPDATE_THROTTLE = 500;

    private static final String EVENT_SORT_ORDER = Instances.START_DAY + " ASC, "
            + Instances.START_MINUTE + " ASC, " + Instances.END_DAY + " ASC, "
            + Instances.END_MINUTE + " ASC LIMIT " + EVENT_MAX_COUNT;

    private static final String EVENT_SELECTION = Calendars.VISIBLE + "=1";
    private static final String EVENT_SELECTION_HIDE_DECLINED = Calendars.VISIBLE + "=1 AND "
            + Instances.SELF_ATTENDEE_STATUS + "!=" + Attendees.ATTENDEE_STATUS_DECLINED;

    static final String[] EVENT_PROJECTION = new String[] {
        Instances.ALL_DAY,
        Instances.BEGIN,
        Instances.END,
        Instances.TITLE,
        Instances.EVENT_LOCATION,
        Instances.EVENT_ID,
        Instances.START_DAY,
        Instances.END_DAY,
        Instances.DISPLAY_COLOR, // If SDK < 16, set to Instances.CALENDAR_COLOR.
        Instances.SELF_ATTENDEE_STATUS,
    };

    static final int INDEX_ALL_DAY = 0;
    static final int INDEX_BEGIN = 1;
    static final int INDEX_END = 2;
    static final int INDEX_TITLE = 3;
    static final int INDEX_EVENT_LOCATION = 4;
    static final int INDEX_EVENT_ID = 5;
    static final int INDEX_START_DAY = 6;
    static final int INDEX_END_DAY = 7;
    static final int INDEX_COLOR = 8;
    static final int INDEX_SELF_ATTENDEE_STATUS = 9;

    static {
        if (!Utils.isJellybeanOrLater()) {
            EVENT_PROJECTION[INDEX_COLOR] = Instances.CALENDAR_COLOR;
        }
    }
    static final int MAX_DAYS = 7;

    private static final long SEARCH_DURATION = MAX_DAYS * DateUtils.DAY_IN_MILLIS;

    /**
     * Update interval used when no next-update calculated, or bad trigger time in past.
     * Unit: milliseconds.
     */
    private static final long UPDATE_TIME_NO_EVENTS = DateUtils.HOUR_IN_MILLIS * 6;

    private static final String[] CALENDAR_PERMISSION = {Manifest.permission.READ_CALENDAR,
                                                    Manifest.permission.WRITE_CALENDAR};
    public static boolean CALENDAR_PERMISSION_GRANTED = false;

    private boolean hasRequiredPermission(String[] permissions) {
        for (String permission : permissions) {
            if (checkSelfPermission(permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private boolean checkPermissions() {
        boolean flagRequestPermission = false;

        if (!hasRequiredPermission(CALENDAR_PERMISSION)) {
            return false;
        }
        return true;
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
            CalendarAppWidgetService.CALENDAR_PERMISSION_GRANTED = checkPermissions();
        return new CalendarFactory(getApplicationContext(), intent);
    }

    public static class CalendarFactory extends BroadcastReceiver implements
            RemoteViewsService.RemoteViewsFactory, Loader.OnLoadCompleteListener<Cursor> {
        private static final boolean LOGD = true;
        /// M: Change mLoader to static to support reuse it to avoid create many new
        //  CursorLoader which can cause cursorLeak.
        private static CursorLoader mLoader;
        /// M: add this to save the last registered listener, when reuse
        //  mLoader will unregister it.
        private static CalendarFactory mLastRegisterListener;

        // Suppress unnecessary logging about update time. Need to be static as this object is
        // re-instanciated frequently.
        // TODO: It seems loadData() is called via onCreate() four times, which should mean
        // unnecessary CalendarFactory object is created and dropped. It is not efficient.
        private static long sLastUpdateTime = UPDATE_TIME_NO_EVENTS;

        private Context mContext;
        private Resources mResources;
        private static CalendarAppWidgetModel mModel;
        private static Object mLock = new Object();
        private static volatile int mSerialNum = 0;
        private int mLastSerialNum = -1;
        private final Handler mHandler = new Handler();
        private static final AtomicInteger currentVersion = new AtomicInteger(0);
        private final ExecutorService executor = Executors.newSingleThreadExecutor();
        private int mAppWidgetId;
        private int mDeclinedColor;
        private int mStandardColor;
        private int mAllDayColor;

        private final Runnable mTimezoneChanged = new Runnable() {
            @Override
            public void run() {
                if (mLoader != null) {
                    mLoader.forceLoad();
                }
            }
        };

        protected CalendarFactory(Context context, Intent intent) {
            mContext = context;
            mResources = context.getResources();
            mAppWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            mDeclinedColor = mResources.getColor(R.color.appwidget_item_declined_color);
            mStandardColor = mResources.getColor(R.color.appwidget_item_standard_color);
            mAllDayColor = mResources.getColor(R.color.appwidget_item_allday_color);
        }

        public CalendarFactory() {
            // This is being created as part of onReceive

        }

        @Override
        public void onCreate() {
            if (CalendarAppWidgetService.CALENDAR_PERMISSION_GRANTED == false)
            {
                return;
            }
            String selection = queryForSelection();
            initLoader(selection);
        }

        @Override
        public void onDataSetChanged() {
        }

        @Override
        public void onDestroy() {
            if (mLoader != null) {
                mLoader.reset();
                ///M: As reset method has destroyed the CursorLoader and mLoader is
                // static variable, so set mLoader to null
                mLoader = null;
            }
        }

        @Override
        public RemoteViews getLoadingView() {
            RemoteViews views = new RemoteViews(mContext.getPackageName(),
                    R.layout.appwidget_loading);
            return views;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            // we use getCount here so that it doesn't return null when empty
            if (position < 0 || position >= getCount()) {
                return null;
            }

            if (mModel == null) {
                RemoteViews views = new RemoteViews(mContext.getPackageName(),
                        R.layout.appwidget_loading);
                final Intent intent = CalendarAppWidgetProvider.getLaunchFillInIntent(mContext, 0,
                        0, 0, false);
                ///M:setOnClickPendingIntent directly, instead of setOnClickFillInIntent.@{
                final PendingIntent launchCalendarPendingIntent = PendingIntent.getActivity(
                        mContext, 0 /* no requestCode */, intent, 0 /* no flags */);
                views.setOnClickPendingIntent(R.id.appwidget_loading, launchCalendarPendingIntent);
                ///@}
                return views;

            }
            if (mModel.mEventInfos.isEmpty() || mModel.mRowInfos.isEmpty()) {
                RemoteViews views = new RemoteViews(mContext.getPackageName(),
                        R.layout.appwidget_no_events);
                final Intent intent = CalendarAppWidgetProvider.getLaunchFillInIntent(mContext, 0,
                        0, 0, false);
                ///M:setOnClickPendingIntent directly, instead of setOnClickFillInIntent.@{
                final PendingIntent launchCalendarPendingIntent = PendingIntent.getActivity(
                        mContext, 0 /* no requestCode */, intent, 0 /* no flags */);
                views.setOnClickPendingIntent(R.id.appwidget_no_events, launchCalendarPendingIntent);
                ///@}
                return views;
            }

            CalendarAppWidgetModel.RowInfo rowInfo = mModel.mRowInfos.get(position);
            if (rowInfo.mType == CalendarAppWidgetModel.RowInfo.TYPE_DAY) {
                RemoteViews views = new RemoteViews(mContext.getPackageName(),
                        R.layout.appwidget_day);
                CalendarAppWidgetModel.DayInfo dayInfo = mModel.mDayInfos.get(rowInfo.mIndex);
                updateTextView(views, R.id.date, View.VISIBLE, dayInfo.mDayLabel);
                return views;
            } else {
                RemoteViews views;
                final CalendarAppWidgetModel.EventInfo eventInfo = mModel.mEventInfos.get(rowInfo.mIndex);
                if (eventInfo.allDay) {
                    views = new RemoteViews(mContext.getPackageName(),
                            R.layout.widget_all_day_item);
                } else {
                    views = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);
                }
                int displayColor = Utils.getDisplayColorFromColor(eventInfo.color);

                final long now = System.currentTimeMillis();
                if (!eventInfo.allDay && eventInfo.start <= now && now <= eventInfo.end) {
                    views.setInt(R.id.widget_row, "setBackgroundResource",
                            R.drawable.agenda_item_bg_secondary);
                } else {
                    views.setInt(R.id.widget_row, "setBackgroundResource",
                            R.drawable.agenda_item_bg_primary);
                }

                if (!eventInfo.allDay) {
                    updateTextView(views, R.id.when, eventInfo.visibWhen, eventInfo.when);
                    updateTextView(views, R.id.where, eventInfo.visibWhere, eventInfo.where);
                }
                updateTextView(views, R.id.title, eventInfo.visibTitle, eventInfo.title);

                views.setViewVisibility(R.id.agenda_item_color, View.VISIBLE);

                int selfAttendeeStatus = eventInfo.selfAttendeeStatus;
                if (eventInfo.allDay) {
                    if (selfAttendeeStatus == Attendees.ATTENDEE_STATUS_INVITED) {
                        views.setInt(R.id.agenda_item_color, "setImageResource",
                                R.drawable.widget_chip_not_responded_bg);
                        views.setInt(R.id.title, "setTextColor", displayColor);
                    } else {
                        views.setInt(R.id.agenda_item_color, "setImageResource",
                                R.drawable.widget_chip_responded_bg);
                        views.setInt(R.id.title, "setTextColor", mAllDayColor);
                    }
                    if (selfAttendeeStatus == Attendees.ATTENDEE_STATUS_DECLINED) {
                        // 40% opacity
                        views.setInt(R.id.agenda_item_color, "setColorFilter",
                                Utils.getDeclinedColorFromColor(displayColor));
                    } else {
                        views.setInt(R.id.agenda_item_color, "setColorFilter", displayColor);
                    }
                } else if (selfAttendeeStatus == Attendees.ATTENDEE_STATUS_DECLINED) {
                    views.setInt(R.id.title, "setTextColor", mDeclinedColor);
                    views.setInt(R.id.when, "setTextColor", mDeclinedColor);
                    views.setInt(R.id.where, "setTextColor", mDeclinedColor);
                    // views.setInt(R.id.agenda_item_color, "setDrawStyle",
                    // ColorChipView.DRAW_CROSS_HATCHED);
                    views.setInt(R.id.agenda_item_color, "setImageResource",
                            R.drawable.widget_chip_responded_bg);
                    // 40% opacity
                    views.setInt(R.id.agenda_item_color, "setColorFilter",
                            Utils.getDeclinedColorFromColor(displayColor));
                } else {
                    views.setInt(R.id.title, "setTextColor", mStandardColor);
                    views.setInt(R.id.when, "setTextColor", mStandardColor);
                    views.setInt(R.id.where, "setTextColor", mStandardColor);
                    if (selfAttendeeStatus == Attendees.ATTENDEE_STATUS_INVITED) {
                        views.setInt(R.id.agenda_item_color, "setImageResource",
                                R.drawable.widget_chip_not_responded_bg);
                    } else {
                        views.setInt(R.id.agenda_item_color, "setImageResource",
                                R.drawable.widget_chip_responded_bg);
                    }
                    views.setInt(R.id.agenda_item_color, "setColorFilter", displayColor);
                }

                long start = eventInfo.start;
                long end = eventInfo.end;
                // An element in ListView.
                if (eventInfo.allDay) {
                    String tz = Utils.getTimeZone(mContext, null);
                    Time recycle = new Time();
                    start = Utils.convertAlldayLocalToUTC(recycle, start, tz);
                    end = Utils.convertAlldayLocalToUTC(recycle, end, tz);
                }
                final Intent fillInIntent = CalendarAppWidgetProvider.getLaunchFillInIntent(
                        mContext, eventInfo.id, start, end, eventInfo.allDay);
                views.setOnClickFillInIntent(R.id.widget_row, fillInIntent);
                return views;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 5;
        }

        @Override
        public int getCount() {
            // if there are no events, we still return 1 to represent the "no
            // events" view
            if (mModel == null) {
                return 1;
            }
            return Math.max(1, mModel.mRowInfos.size());
        }

        @Override
        public long getItemId(int position) {
            if (mModel == null ||  mModel.mRowInfos.isEmpty() || position >= getCount()) {
                return 0;
            }
            CalendarAppWidgetModel.RowInfo rowInfo = mModel.mRowInfos.get(position);
            if (rowInfo.mType == CalendarAppWidgetModel.RowInfo.TYPE_DAY) {
                return rowInfo.mIndex;
            }
            CalendarAppWidgetModel.EventInfo eventInfo = mModel.mEventInfos.get(rowInfo.mIndex);
            long prime = 31;
            long result = 1;
            result = prime * result + (int) (eventInfo.id ^ (eventInfo.id >>> 32));
            result = prime * result + (int) (eventInfo.start ^ (eventInfo.start >>> 32));
            return result;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        /**
         * Query across all calendars for upcoming event instances from now
         * until some time in the future. Widen the time range that we query by
         * one day on each end so that we can catch all-day events. All-day
         * events are stored starting at midnight in UTC but should be included
         * in the list of events starting at midnight local time. This may fetch
         * more events than we actually want, so we filter them out later.
         *
         * @param selection The selection string for the loader to filter the query with.
         */
        public void initLoader(String selection) {
            if (LOGD)
                Log.d(TAG, "Querying for widget events...");

            // Search for events from now until some time in the future
            Uri uri = createLoaderUri();
            mLoader = new CursorLoader(mContext, uri, EVENT_PROJECTION, selection, null,
                    EVENT_SORT_ORDER);
            mLoader.setUpdateThrottle(WIDGET_UPDATE_THROTTLE);
            synchronized (mLock) {
                mLastSerialNum = ++mSerialNum;
            }
            mLoader.registerListener(mAppWidgetId, this);
            /// M: save this registered listener, when reuse mLoader will unregister it.
            mLastRegisterListener = this;
            mLoader.startLoading();
        }

        /**
         * M: Use this function to change the cursorLoader and start load again,
         * If there is a newer load request in the queue, skip loading.
         */
        public void resetLoader(String selection, final int version) {
            Uri uri = createLoaderUri();
            Log.d(TAG, "CalendarAppWidgetService restarLoad uri: " + uri);
            synchronized (mLock) {
                mLastSerialNum = ++mSerialNum;
            }
            if (mLastRegisterListener != null) {
                mLoader.unregisterListener(mLastRegisterListener);
            }
            mLoader.registerListener(mAppWidgetId, this);
            mLastRegisterListener = this;
            mLoader.setSelection(selection);
            mLoader.setUri(uri);
            if (version < currentVersion.get()) {
                return;
            }
            mLoader.forceLoad();
        }

        /**
         * This gets the selection string for the loader.  This ends up doing a query in the
         * shared preferences.
         */
        private String queryForSelection() {
            return Utils.getHideDeclinedEvents(mContext) ? EVENT_SELECTION_HIDE_DECLINED
                    : EVENT_SELECTION;
        }

        /**
         * @return The uri for the loader
         */
        private Uri createLoaderUri() {
            long now = System.currentTimeMillis();
            // Add a day on either side to catch all-day events
            long begin = now - DateUtils.DAY_IN_MILLIS;
            long end = now + SEARCH_DURATION + DateUtils.DAY_IN_MILLIS;

            Uri uri = Uri.withAppendedPath(Instances.CONTENT_URI, Long.toString(begin) + "/" + end);
            return uri;
        }

        /* @VisibleForTesting */
        protected static CalendarAppWidgetModel buildAppWidgetModel(
                Context context, Cursor cursor, String timeZone) {
            CalendarAppWidgetModel model = new CalendarAppWidgetModel(context, timeZone);
            model.buildFromCursor(cursor, timeZone);
            return model;
        }

        /**
         * Calculates and returns the next time we should push widget updates.
         */
        private long calculateUpdateTime(CalendarAppWidgetModel model, long now, String timeZone) {
            // Make sure an update happens at midnight or earlier
            long minUpdateTime = getNextMidnightTimeMillis(timeZone);
            for (CalendarAppWidgetModel.EventInfo event : model.mEventInfos) {
                final long start;
                final long end;
                start = event.start;
                end = event.end;

                // We want to update widget when we enter/exit time range of an event.
                if (now < start) {
                    minUpdateTime = Math.min(minUpdateTime, start);
                } else if (now < end) {
                    minUpdateTime = Math.min(minUpdateTime, end);
                }
            }
            return minUpdateTime;
        }

        private static long getNextMidnightTimeMillis(String timezone) {
            Time time = new Time();
            time.setToNow();
            time.monthDay++;
            time.hour = 0;
            time.minute = 0;
            time.second = 0;
            long midnightDeviceTz = time.normalize(true);

            time.timezone = timezone;
            time.setToNow();
            time.monthDay++;
            time.hour = 0;
            time.minute = 0;
            time.second = 0;
            long midnightHomeTz = time.normalize(true);

            return Math.min(midnightDeviceTz, midnightHomeTz);
        }

        static void updateTextView(RemoteViews views, int id, int visibility, String string) {
            views.setViewVisibility(id, visibility);
            if (visibility == View.VISIBLE) {
                views.setTextViewText(id, string);
            }
        }

        /*
         * (non-Javadoc)
         * @see
         * android.content.Loader.OnLoadCompleteListener#onLoadComplete(android
         * .content.Loader, java.lang.Object)
         */
        @Override
        public void onLoadComplete(Loader<Cursor> loader, Cursor cursor) {
            if (CalendarAppWidgetService.CALENDAR_PERMISSION_GRANTED == false)
            {
                return;
            }
            if (cursor == null) {
                return;
            }
            // If a newer update has happened since we started clean up and
            // return
            synchronized (mLock) {
                if (cursor.isClosed()) {
                    Log.wtf(TAG, "Got a closed cursor from onLoadComplete");
                    return;
                }

                if (mLastSerialNum != mSerialNum) {
                    return;
                }

                final long now = System.currentTimeMillis();
                String tz = Utils.getTimeZone(mContext, mTimezoneChanged);

                // Copy it to a local static cursor.
                MatrixCursor matrixCursor = Utils.matrixCursorFromCursor(cursor);
                try {
                    mModel = buildAppWidgetModel(mContext, matrixCursor, tz);
                } finally {
                    if (matrixCursor != null) {
                        matrixCursor.close();
                    }

                    if (cursor != null) {
                        cursor.close();
                    }
                }

                // Schedule an alarm to wake ourselves up for the next update.
                // We also cancel
                // all existing wake-ups because PendingIntents don't match
                // against extras.
                long triggerTime = calculateUpdateTime(mModel, now, tz);

                // If no next-update calculated, or bad trigger time in past,
                // schedule
                // update about six hours from now.
                if (triggerTime < now) {
                    Log.w(TAG, "Encountered bad trigger time " + formatDebugTime(triggerTime, now));
                    triggerTime = now + UPDATE_TIME_NO_EVENTS;
                }

                final AlarmManager alertManager = (AlarmManager) mContext
                        .getSystemService(Context.ALARM_SERVICE);
                final PendingIntent pendingUpdate = CalendarAppWidgetProvider
                        .getUpdateIntent(mContext);

                alertManager.cancel(pendingUpdate);
                alertManager.set(AlarmManager.RTC, triggerTime, pendingUpdate);
                Time time = new Time(Utils.getTimeZone(mContext, null));
                time.setToNow();

                if (time.normalize(true) != sLastUpdateTime) {
                    Time time2 = new Time(Utils.getTimeZone(mContext, null));
                    time2.set(sLastUpdateTime);
                    time2.normalize(true);
                    if (time.year != time2.year || time.yearDay != time2.yearDay) {
                        final Intent updateIntent = new Intent(
                                Utils.getWidgetUpdateAction(mContext));
                        mContext.sendBroadcast(updateIntent);
                    }

                    sLastUpdateTime = time.toMillis(true);
                }

                AppWidgetManager widgetManager = AppWidgetManager.getInstance(mContext);
                if (mAppWidgetId == -1) {
                    int[] ids = widgetManager.getAppWidgetIds(CalendarAppWidgetProvider
                            .getComponentName(mContext));

                    widgetManager.notifyAppWidgetViewDataChanged(ids, R.id.events_list);
                } else {
                    widgetManager.notifyAppWidgetViewDataChanged(mAppWidgetId, R.id.events_list);
                }
            }
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (LOGD)
                Log.d(TAG, "AppWidgetService received an intent. It was " + intent.toString());

            if (!CalendarAppWidgetService.CALENDAR_PERMISSION_GRANTED) {
              return;
            }

            mContext = context;

            // We cannot do any queries from the UI thread, so push the 'selection' query
            // to a background thread.  However the implementation of the latter query
            // (cursor loading) uses CursorLoader which must be initiated from the UI thread,
            // so there is some convoluted handshaking here.
            //
            // Note that as currently implemented, this must run in a single threaded executor
            // or else the loads may be run out of order.
            //
            // TODO: Remove use of mHandler and CursorLoader, and do all the work synchronously
            // in the background thread.  All the handshaking going on here between the UI and
            // background thread with using goAsync, mHandler, and CursorLoader is confusing.
            final PendingResult result = goAsync();
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    // We always complete queryForSelection() even if the load task ends up being
                    // canceled because of a more recent one.  Optimizing this to allow
                    // canceling would require keeping track of all the PendingResults
                    // (from goAsync) to abort them.  Defer this until it becomes a problem.
                    final String selection = queryForSelection();
                    /** M: Change this function to support reuse mLoader to avoid cursor leak. */
                    mAppWidgetId = -1;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mLoader == null) {
                                initLoader(selection);
                            } else {
                                resetLoader(selection, currentVersion.incrementAndGet());
                            }
                            result.finish();
                        }
                    });
                    /** @} */
                }
            });
        }
    }

    /**
     * Format given time for debugging output.
     *
     * @param unixTime Target time to report.
     * @param now Current system time from {@link System#currentTimeMillis()}
     *            for calculating time difference.
     */
    static String formatDebugTime(long unixTime, long now) {
        Time time = new Time();
        time.set(unixTime);

        long delta = unixTime - now;
        if (delta > DateUtils.MINUTE_IN_MILLIS) {
            delta /= DateUtils.MINUTE_IN_MILLIS;
            return String.format("[%d] %s (%+d mins)", unixTime,
                    time.format("%H:%M:%S"), delta);
        } else {
            delta /= DateUtils.SECOND_IN_MILLIS;
            return String.format("[%d] %s (%+d secs)", unixTime,
                    time.format("%H:%M:%S"), delta);
        }
    }
}

