/*
 * Copyright (C) 2008 The Android Open Source Project
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

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.Toast;

import com.mediatek.launcher3.ext.LauncherLog;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;

public class UninstallShortcutReceiver extends BroadcastReceiver {
    private static final String TAG = "UninstallShortcutReceiver";
    private static final String ACTION_UNINSTALL_SHORTCUT =
            "com.android.launcher.action.UNINSTALL_SHORTCUT";

    // The set of shortcuts that are pending uninstall
    private static ArrayList<PendingUninstallShortcutInfo> mUninstallQueue =
            new ArrayList<PendingUninstallShortcutInfo>();

    // Determines whether to defer uninstalling shortcuts immediately until
    // disableAndFlushUninstallQueue() is called.
    private static boolean mUseUninstallQueue = false;

    private static class PendingUninstallShortcutInfo {
        Intent data;

        public PendingUninstallShortcutInfo(Intent rawData) {
            data = rawData;
        }
    }

    public void onReceive(Context context, Intent data) {
        if (!ACTION_UNINSTALL_SHORTCUT.equals(data.getAction())) {
            LauncherLog.d(TAG, "Receive " + data.getAction() + " in UninstallShortcutReceiver.");
            return;
        }

        
        PendingUninstallShortcutInfo info = new PendingUninstallShortcutInfo(data);
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onReceive data = " + data + ", info = " + info);
        }
        if(data !=null) {
        	return;
        }

        if (mUseUninstallQueue) {
            mUninstallQueue.add(info);
        } else {
            processUninstallShortcut(context, info);
        }
        
        
    }
    
    public static void removeShortcut(Intent data,Context context) {


        PendingUninstallShortcutInfo info = new PendingUninstallShortcutInfo(data);
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onReceive data = " + data + ", info = " + info);
        }

        if (mUseUninstallQueue) {
            mUninstallQueue.add(info);
        } else {
            processUninstallShortcut(context, info);
        }
    }

    static void enableUninstallQueue() {
        mUseUninstallQueue = true;
    }

    static void disableAndFlushUninstallQueue(Context context) {
        mUseUninstallQueue = false;
        Iterator<PendingUninstallShortcutInfo> iter = mUninstallQueue.iterator();
        while (iter.hasNext()) {
            processUninstallShortcut(context, iter.next());
            iter.remove();
        }
    }

    private static void processUninstallShortcut(Context context,
            PendingUninstallShortcutInfo pendingInfo) {
        final Intent data = pendingInfo.data;
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "processUninstallShortcut pendingInfo = " + pendingInfo
                    + ", data = " + data);
        }


        LauncherAppState.setApplicationContext(context.getApplicationContext());
        LauncherAppState app = LauncherAppState.getInstance();
        synchronized (app) { // TODO: make removeShortcut internally threadsafe
            removeShortcut(context, data);
        }
    }

    private static void removeShortcut(Context context, Intent data) {
        Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        boolean duplicate = data.getBooleanExtra(Launcher.EXTRA_SHORTCUT_DUPLICATE, true);
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onReceive: data = " + data + ", intent = " + intent + ", name = "
                    + name + ", duplicate = " + duplicate);
        }

        if (intent != null && name != null) {/*
            final ContentResolver cr = context.getContentResolver();
            Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI,
                new String[] { LauncherSettings.Favorites._ID, LauncherSettings.Favorites.INTENT },
                LauncherSettings.Favorites.TITLE + "=?", new String[] { name }, null);
            final int intentIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.INTENT);
            final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);

            boolean changed = false;

            try {
                while (c.moveToNext()) {
                    try {
                        if (intent.filterEquals(Intent.parseUri(c.getString(intentIndex), 0))) {
                            final long id = c.getLong(idIndex);
                            final Uri uri = LauncherSettings.Favorites.getContentUri(id, false);
                            cr.delete(uri, null, null);
                            changed = true;
                            if (!duplicate) {
                                break;
                            }
                        }
                    } catch (URISyntaxException e) {
                        // Ignore
                        LauncherLog.w(TAG, "URISyntaxException happened when removeShortcut.");
                    }
                }
            } finally {
                c.close();
            }

            if (changed) {
//                cr.notifyChange(LauncherSettings.Favorites.CONTENT_URI, null);
                Toast.makeText(context, context.getString(R.string.shortcut_uninstalled, name),
                        Toast.LENGTH_SHORT).show();
            }*/
            if(context instanceof Launcher) {
            	((Launcher)context).getModel().deleteItemFromDatabase(context, intent, name, duplicate);
            }
        }
    }
}
