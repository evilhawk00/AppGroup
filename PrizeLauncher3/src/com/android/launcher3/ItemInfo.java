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

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Represents an item in the launcher.
 */
public class ItemInfo {
    
    static final int NO_ID = -1;
    
    /**
     * The id in the settings database for this item
     */
    long id = NO_ID;

    boolean select=false;//文件夹编辑状态
    
    public int firstInstall=0;
    public int down_state=-1;
    
    public int progress;
    
    public int hide=0;
    
    public String iconUri;
    
    public String packageName="";
    public int fromAppStore=0;

	/**
	 * @author Administrator
	 *批量整理状态
	 */
	public enum State {
		NONE,BATCH_SELECT_MODEL
	};
	
	public State mItemState = State.NONE;
    /**
     * One of {@link LauncherSettings.Favorites#ITEM_TYPE_APPLICATION},
     * {@link LauncherSettings.Favorites#ITEM_TYPE_SHORTCUT},
     * {@link LauncherSettings.Favorites#ITEM_TYPE_FOLDER}, or
     * {@link LauncherSettings.Favorites#ITEM_TYPE_APPWIDGET}.
     */
    int itemType;
    
    /**
     * The id of the container that holds this item. For the desktop, this will be 
     * {@link LauncherSettings.Favorites#CONTAINER_DESKTOP}. For the all applications folder it
     * will be {@link #NO_ID} (since it is not stored in the settings DB). For user folders
     * it will be the id of the folder.
     */
    long container = NO_ID;
    
    String unreadTitle;
    
    
    /**
     * Iindicates the screen in which the shortcut appears.
     */
    long screenId = -1;
    
    /**
     * Indicates the X position of the associated cell.
     */
    public int cellX = -1;

    /**
     * Indicates the Y position of the associated cell.
     */
    public int cellY = -1;

    /**
     * Indicates the X cell span.
     */
    public int spanX = 1;

    /**
     * Indicates the Y cell span.
     */
    public int spanY = 1;

    /**
     * Indicates the minimum X cell span.
     */
    int minSpanX = 1;

    /**
     * Indicates the minimum Y cell span.
     */
    int minSpanY = 1;

    /**
     * Indicates that this item needs to be updated in the db
     */
    boolean requiresDbUpdate = false;

    /**
     * Title of the item
     */
    public CharSequence title;
    /**
     * Title_id of the item
     */
    int title_id=-1;
	//add by zhouerlong

    /**
     * The position of the item in a drag-and-drop operation.
     */
    int[] dropPos = null;


    /**
     * M: the position of the application icon in all app list page, add for
     * op09.
     */
    int pos;
    
    

    /**
     * M: The unread messages number of the item
     */
   public int unreadNum = 0;

    ItemInfo() {
    }

    ItemInfo(ItemInfo info) {
        id = info.id;
        cellX = info.cellX;
        cellY = info.cellY;
        spanX = info.spanX;
        spanY = info.spanY;
        screenId = info.screenId;
        itemType = info.itemType;
        container = info.container;
        // tempdebug:
        LauncherModel.checkItemInfo(this);

        ///M: Added for unread message feature
        unreadNum = info.unreadNum;
    }

    protected Intent getIntent() {
        throw new RuntimeException("Unexpected Intent");
    }

    /**
     * Write the fields of this item to the DB
     * 
     * @param values
     */
    void onAddToDatabase(ContentValues values) { 
        values.put(LauncherSettings.BaseLauncherColumns.ITEM_TYPE, itemType);
        values.put(LauncherSettings.Favorites.CONTAINER, container);
        values.put(LauncherSettings.Favorites.SCREEN, screenId);
        values.put(LauncherSettings.Favorites.CELLX, cellX);
        values.put(LauncherSettings.Favorites.CELLY, cellY);
        values.put(LauncherSettings.Favorites.SPANX, spanX);
        values.put(LauncherSettings.Favorites.SPANY, spanY);
        values.put(LauncherSettings.Favorites.FIRST_INSTALL, firstInstall);
        values.put(LauncherSettings.Favorites.PACKAGE_NAME, packageName);
        values.put(LauncherSettings.Favorites.DOWN_PROGRESS, progress);
        values.put(LauncherSettings.Favorites.FROM_APPSTORE, fromAppStore);
        values.put(LauncherSettings.Favorites.DOWN_STATE, down_state);
        values.put(LauncherSettings.Favorites.ICON_URI, iconUri);
        values.put(LauncherSettings.Favorites.HIDE, hide);
    }

    void updateValuesWithCoordinates(ContentValues values, int cellX, int cellY) {
        values.put(LauncherSettings.Favorites.CELLX, cellX);
        values.put(LauncherSettings.Favorites.CELLY, cellY);
    }
    
    void updateValuesWithFirstInstall(ContentValues values,int firstInstall) {
        values.put(LauncherSettings.Favorites.FIRST_INSTALL, firstInstall);
    	
    }
    

    void updateValuesWithDownLoadProgress(ContentValues values,int progress) {
        values.put(LauncherSettings.Favorites.FIRST_INSTALL, progress);
    	
    }
    
    void updateValuesWithFirstHide(ContentValues values,int hide) {
        values.put(LauncherSettings.Favorites.FIRST_INSTALL, firstInstall);
    	
    }

    static byte[] flattenBitmap(Bitmap bitmap) {
        // Try go guesstimate how much space the icon will take when serialized
        // to avoid unnecessary allocations/copies during the write.
        int size = bitmap.getWidth() * bitmap.getHeight() * 4;
        ByteArrayOutputStream out = new ByteArrayOutputStream(size);
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
            Log.w("Favorite", "Could not write icon");
            return null;
        }
    }

    static void writeBitmap(ContentValues values, Bitmap bitmap) {
        if (bitmap != null) {
            byte[] data = flattenBitmap(bitmap);
            values.put(LauncherSettings.Favorites.ICON, data);
        }
    }

    /**
     * It is very important that sub-classes implement this if they contain any references
     * to the activity (anything in the view hierarchy etc.). If not, leaks can result since
     * ItemInfo objects persist across rotation and can hence leak by holding stale references
     * to the old view hierarchy / activity.
     */
    void unbind() {
    }

    @Override
    public String toString() {
        return "Item(id=" + this.id + " type=" + this.itemType + " container=" + this.container
            + " screen=" + screenId + " cellX=" + cellX + " cellY=" + cellY + " spanX=" + spanX
            + " spanY=" + spanY + " dropPos=" + " unreadNum=" + unreadNum+ ")";
    }
}
