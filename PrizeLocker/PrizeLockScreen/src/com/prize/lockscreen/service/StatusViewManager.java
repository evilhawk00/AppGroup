﻿package com.prize.lockscreen.service;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;

/**
 * 该类主要用来管理在锁屏界面上显示时间(包括12小时制下显示上午和下午)和日期。
 * @author fanjunchen
 *
 */
public class StatusViewManager 
{

    private static final String SYSTEM = "/system/fonts/";
    private static final String SYSTEM_FONT_TIME_BACKGROUND = SYSTEM + "AndroidClock.ttf";
    private static final String SYSTEM_FONT_TIME_FOREGROUND = SYSTEM + "AndroidClock_Highlight.ttf";
    
    private final static String M12 = "h:mm";
    private final static String M24 = "kk:mm";
    
	private TextView mDateView;
	private TextView mTimeView;
	public TextView mArtistView;
	public TextView mMusicView;
	
	private String mDateFormat;
	private String mFormat;
	
	private static Activity mActivity;
	private AmPm mAmPm;
    private Calendar mCalendar;
    public ContentObserver mFormatChangeObserver;
    public BroadcastReceiver mIntentReceiver;
   
    private final Handler mHandler = new Handler();
    
    private static final Typeface sBackgroundFont;
    private static final Typeface sForegroundFont;
    
    private static Context mContext;
	
    static 
    {
    	//创建获取字体风格
        sBackgroundFont = Typeface.createFromFile(SYSTEM_FONT_TIME_BACKGROUND);
        sForegroundFont = Typeface.createFromFile(SYSTEM_FONT_TIME_FOREGROUND);
    }
    
	public StatusViewManager(Activity activity, Context context)
	{
		mContext = context;
		mActivity = activity;
		refreshDate();
	}
	
	private View findViewById(int id) 
	{
        return mActivity.findViewById(id);
    }
	
    private void refreshDate()
    {
    	if (mDateView != null)
    	{
    		//锁屏界面显示日期
    		mDateView.setText(DateFormat.format(mDateFormat, new Date()));
    	}
    }
	
    private String getString(int id)
    {
    	return mActivity.getString(id);
    }
    
    class AmPm {
        private TextView mAmPmTextView;
        private String mAmString, mPmString;

        AmPm(Typeface tf) {
           /* mAmPmTextView = (TextView)findViewById(R.id.am_pm);
            if (mAmPmTextView != null && tf != null) {
            	//设置显示的上午、下午字体风格
                mAmPmTextView.setTypeface(tf);
            }

            //获取显示上午、下午的字符串数组
            String[] ampm = new DateFormatSymbols().getAmPmStrings();
            mAmString = ampm[0];
            mPmString = ampm[1];*/
        }

        void setShowAmPm(boolean show) {
            /*if (mAmPmTextView != null) {
                mAmPmTextView.setVisibility(show ? View.VISIBLE : View.GONE);
            }*/
        }

        void setIsMorning(boolean isMorning) {
            /*if (mAmPmTextView != null) {
                mAmPmTextView.setText(isMorning ? mAmString : mPmString);
            }*/
        }
    }
    
    private static class TimeChangedReceiver extends BroadcastReceiver {
        private WeakReference<StatusViewManager> mStatusViewManager;
        //private Context mContext;

        public TimeChangedReceiver(StatusViewManager status) {
        	mStatusViewManager = new WeakReference<StatusViewManager>(status);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            // Post a runnable to avoid blocking the broadcast.
            final boolean timezoneChanged =
                    intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED);
            final StatusViewManager status = mStatusViewManager.get();
            if (status != null) {
            	status.mHandler.post(new Runnable() {
                    public void run() {
                        if (timezoneChanged) {
                        	status.mCalendar = Calendar.getInstance();
                        }
                        status.updateTime();
                    }
                });
            } else {
                try {
                	mContext.unregisterReceiver(this);
                } catch (RuntimeException e) {
                    // Shouldn't happen
                }
            }
        }
    };
    
    /**监听URI为Settings.System.CONTENT_URI的数据变化，即12小时制还是24小时制
     * 的变化(一般来自用户在设置里对时间显示的设置) 暂时不用
     */
    private static class FormatChangeObserver extends ContentObserver {
        private WeakReference<StatusViewManager> mStatusViewManager;
        //private Context mContext;
        public FormatChangeObserver(StatusViewManager status) {
            super(new Handler());
            //创建保存在弱应用中的StatusViewManager对象
            mStatusViewManager = new WeakReference<StatusViewManager>(status);
        }
        @Override
        public void onChange(boolean selfChange) {
        	StatusViewManager mStatusManager = mStatusViewManager.get();
            if (mStatusManager != null) {
            	mStatusManager.setDateFormat();
            	mStatusManager.updateTime();
            } else {
                try {
                	mContext.getContentResolver().unregisterContentObserver(this);
                } catch (RuntimeException e) {
                    // Shouldn't happen
                }
            }
        }
    }
    
    //更新时间
    private void updateTime() 
    {
        mCalendar.setTimeInMillis(System.currentTimeMillis());

        CharSequence newTime = DateFormat.format(mFormat, mCalendar);
        mTimeView.setText(newTime);
        mAmPm.setIsMorning(mCalendar.get(Calendar.AM_PM) == 0);
    }
    
    //设置时间显示格式，如果时间显示为12小时制，则显示上午、下午
    private void setDateFormat() 
    {
        mFormat = android.text.format.DateFormat.is24HourFormat(mContext)
            ? M24 : M12;
        mAmPm.setShowAmPm(mFormat.equals(M12));
    }
    
}