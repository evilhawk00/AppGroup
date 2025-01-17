/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.prize.music.helpers;

import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.content.ContentQueryMap;
import android.content.Context;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.provider.BaseColumns;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Audio.AlbumColumns;
import android.provider.MediaStore.Audio.AudioColumns;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;

/**
 * A loader that queries the {@link ContentResolver} and returns a
 * {@link Cursor}. This class implements the {@link Loader} protocol in a
 * standard way for querying cursors, building on {@link AsyncTaskLoader} to
 * perform the cursor query on a background thread so that it does not block the
 * application's UI.
 * 
 * <p>
 * A CursorLoader must be built with the full information for the query to
 * perform, either through the
 * {@link #CursorLoader(Context, Uri, String[], String, String[], String)} or
 * creating an empty instance with {@link #CursorLoader(Context)} and filling in
 * the desired paramters with {@link #setUri(Uri)},
 * {@link #setSelection(String)}, {@link #setSelectionArgs(String[])},
 * {@link #setSortOrder(String)}, and {@link #setProjection(String[])}.
 */
public class AddIdCursorLoader extends AsyncTaskLoader<Cursor> {
	final ForceLoadContentObserver mObserver;

	Uri mUri;
	String[] mProjection;
	String mSelection;
	String[] mSelectionArgs;
	String mSortOrder;

	Cursor mCursor;

	/* Runs on a worker thread */
	@Override
	public Cursor loadInBackground() {

		Cursor mediaCursor = getContext().getContentResolver().query(mUri,
				mProjection, mSelection, mSelectionArgs, mSortOrder);
		// Get cursor filled with Audio Id's
		String[] projection = new String[] { BaseColumns._ID,
				AlbumColumns.ALBUM };
		Uri uri = Audio.Albums.EXTERNAL_CONTENT_URI;
		String sortOrder = Audio.Albums.DEFAULT_SORT_ORDER;
		Cursor albumCursor = null;
		try {
			albumCursor = getContext().getContentResolver().query(uri,
					projection, null, null, sortOrder);
		} catch (Exception e) {
			// TODO: handle exception
		}
		if (albumCursor == null) return null;
		// Matrix cursor to hold final data to be returned to calling context
		MatrixCursor cursor = new MatrixCursor(new String[] { BaseColumns._ID,
				MediaColumns.TITLE, AudioColumns.ARTIST, AudioColumns.ALBUM,
				AudioColumns.ALBUM_ID });
		// Map data from Audio Id cursor to the ALbumName Colum
		
		ContentQueryMap mQueryMap = new ContentQueryMap(albumCursor,
				AlbumColumns.ALBUM, false, null);

		Map<String, ContentValues> data = mQueryMap.getRows();
		if (mediaCursor != null) {
			while (mediaCursor.moveToNext()) {
				String id = mediaCursor.getString(mediaCursor
						.getColumnIndexOrThrow(BaseColumns._ID));
				String title = mediaCursor.getString(mediaCursor
						.getColumnIndexOrThrow(MediaColumns.TITLE));
				String artist = mediaCursor.getString(mediaCursor
						.getColumnIndexOrThrow(AudioColumns.ARTIST));
				String album = mediaCursor.getString(mediaCursor
						.getColumnIndexOrThrow(AudioColumns.ALBUM));

				ContentValues tData = data.get(album);
				String albumid = (String) tData.get(BaseColumns._ID);
				cursor.addRow(new String[] { id, title, artist, album, albumid });
			}
			mediaCursor.close();
		}

		if (cursor != null) {
			// Ensure the cursor window is filled
			registerContentObserver(cursor, mObserver);
		}
		return cursor;
	}

	/**
	 * Registers an observer to get notifications from the content provider when
	 * the cursor needs to be refreshed.
	 */
	void registerContentObserver(Cursor cursor, ContentObserver observer) {
		cursor.registerContentObserver(mObserver);
	}

	/* Runs on the UI thread */
	@Override
	public void deliverResult(Cursor cursor) {
		if (isReset()) {
			// An async query came in while the loader is stopped
			if (cursor != null) {
				//cursor.close();
				if (Integer.parseInt(Build.VERSION.SDK) < 11) {
					cursor.close();
				}
			}
			return;
		}
		Cursor oldCursor = mCursor;
		mCursor = cursor;

		if (isStarted()) {
			super.deliverResult(cursor);
		}

		if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
			oldCursor.close();
		}
	}

	/**
	 * Creates an empty unspecified CursorLoader. You must follow this with
	 * calls to {@link #setUri(Uri)}, {@link #setSelection(String)}, etc to
	 * specify the query to perform.
	 */
	public AddIdCursorLoader(Context context) {
		super(context);
		mObserver = new ForceLoadContentObserver();
	}

	/**
	 * Creates a fully-specified CursorLoader. See
	 * {@link ContentResolver#query(Uri, String[], String, String[], String)
	 * ContentResolver.query()} for documentation on the meaning of the
	 * parameters. These will be passed as-is to that call.
	 */
	public AddIdCursorLoader(Context context, Uri uri, String[] projection,
			String selection, String[] selectionArgs, String sortOrder) {
		super(context);
		mObserver = new ForceLoadContentObserver();
		mUri = uri;
		mProjection = projection;
		mSelection = selection;
		mSelectionArgs = selectionArgs;
		mSortOrder = sortOrder;
	}

	/**
	 * Starts an asynchronous load of the contacts list data. When the result is
	 * ready the callbacks will be called on the UI thread. If a previous load
	 * has been completed and is still valid the result may be passed to the
	 * callbacks immediately.
	 *
	 * Must be called from the UI thread
	 */
	@Override
	protected void onStartLoading() {
		if (mCursor != null) {
			deliverResult(mCursor);
		}
		if (takeContentChanged() || mCursor == null) {
			forceLoad();
		}
	}

	/**
	 * Must be called from the UI thread
	 */
	@Override
	protected void onStopLoading() {
		// Attempt to cancel the current load task if possible.
		cancelLoad();
	}

	@Override
	public void onCanceled(Cursor cursor) {
		if (cursor != null && !cursor.isClosed()) {
			//cursor.close();
			if (Integer.parseInt(Build.VERSION.SDK) < 11) {
				cursor.close();
			}
		}
	}

	@Override
	protected void onReset() {
		super.onReset();

		// Ensure the loader is stopped
		onStopLoading();

		if (mCursor != null && !mCursor.isClosed()) {
			//mCursor.close();
			if (Integer.parseInt(Build.VERSION.SDK) < 11) {
				mCursor.close();
			}
		}
		mCursor = null;
	}

	public Uri getUri() {
		return mUri;
	}

	public void setUri(Uri uri) {
		mUri = uri;
	}

	public String[] getProjection() {
		return mProjection;
	}

	public void setProjection(String[] projection) {
		mProjection = projection;
	}

	public String getSelection() {
		return mSelection;
	}

	public void setSelection(String selection) {
		mSelection = selection;
	}

	public String[] getSelectionArgs() {
		return mSelectionArgs;
	}

	public void setSelectionArgs(String[] selectionArgs) {
		mSelectionArgs = selectionArgs;
	}

	public String getSortOrder() {
		return mSortOrder;
	}

	public void setSortOrder(String sortOrder) {
		mSortOrder = sortOrder;
	}

	@Override
	public void dump(String prefix, FileDescriptor fd, PrintWriter writer,
			String[] args) {
		super.dump(prefix, fd, writer, args);
		writer.print(prefix);
		writer.print("mUri=");
		writer.println(mUri);
		writer.print(prefix);
		writer.print("mProjection=");
		writer.println(Arrays.toString(mProjection));
		writer.print(prefix);
		writer.print("mSelection=");
		writer.println(mSelection);
		writer.print(prefix);
		writer.print("mSelectionArgs=");
		writer.println(Arrays.toString(mSelectionArgs));
		writer.print(prefix);
		writer.print("mSortOrder=");
		writer.println(mSortOrder);
		writer.print(prefix);
		writer.print("mCursor=");
		writer.println(mCursor);
	}
}
