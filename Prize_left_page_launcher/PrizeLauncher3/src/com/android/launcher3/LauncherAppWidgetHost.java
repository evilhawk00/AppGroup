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

package com.android.launcher3;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.mediatek.launcher3.ext.LauncherLog;

/**
 * Specific {@link AppWidgetHost} that creates our {@link LauncherAppWidgetHostView}
 * which correctly captures all long-press events. This ensures that users can
 * always pick up and move widgets.
 */
public class LauncherAppWidgetHost extends AppWidgetHost {
    static final String TAG = "LauncherAppWidgetHost";
    WeakReference<Launcher> mLauncher;

    public LauncherAppWidgetHost(Launcher launcher, int hostId) {
        super(launcher.getApplicationContext(), hostId);
        mLauncher = new WeakReference<Launcher>(launcher);
    }

    @Override
    protected AppWidgetHostView onCreateView(Context context, int appWidgetId,
            AppWidgetProviderInfo appWidget) {
    	if(mLauncher.get()==null) {
    		return null;
    	}
        return new LauncherAppWidgetHostView(mLauncher.get());
    }

    @Override
    public void stopListening() {
        super.stopListening();
        clearViews();
    }

    protected void onProvidersChanged() {
        // Once we get the message that widget packages are updated, we need to rebind items
        // in AppsCustomize accordingly.
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onProvidersChanged");
        }
        if(mLauncher.get()!=null) {
            final ArrayList<Object> widgetsAndShortcuts = LauncherModel.getSortedWidgetsAndShortcuts(mLauncher.get());
            LauncherModel.runOnWorkerThread(new Runnable() {
                @Override
                public void run() {
                    LauncherModel model = mLauncher.get().getModel();
                    if (LauncherLog.DEBUG) {
                        LauncherLog.d(TAG, "onProvidersChanged widgetsAndShortcuts = " + widgetsAndShortcuts);
                    }
                    if(model != null) {
                        model.runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                            	mLauncher.get().bindPackagesUpdated(widgetsAndShortcuts);
                            }
                        });
                    } else {
                        LauncherLog.d(TAG, "onProvidersChanged: model = null");
                    }
                }
            });
        }
    }
}
