package com.prize.music.ui.adapters.base;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;

import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.history.HistoryColumns;
import com.prize.music.views.ViewHolderList;
import com.prize.music.R;

public abstract class EditListViewAdapter extends SimpleCursorAdapter {
	private String TAG = EditListViewAdapter.class.getSimpleName();;

	private WeakReference<ViewHolderList> holderReference;

	private AnimationDrawable mPeakTwoAnimation;

	protected Context mContext;

	private int left, top;

	public String mListType = null, mLineOneText = null, mLineTwoText = null;

	public String[] mImageData = null;

	public long mPlayingId = 0, mCurrentId = 0;

	public boolean showContextEnabled = true;

	/** 存储每个条目勾选的状态 */
	private SparseBooleanArray mCheckedStates = null;

	private boolean isSelectMode = false;

	private boolean isSelectAll = false;

	private Handler mHandler;

	public boolean isSelectAll() {
		return isSelectAll;
	}

	public boolean isSelectMode() {
		return isSelectMode;
	}

	public void setSelectMode(boolean isSelectMode) {
		this.isSelectMode = isSelectMode;
	}

	public EditListViewAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		mContext = context;
		// Helps center the text in the Playlist/Genre tab
		left = mContext.getResources().getDimensionPixelSize(
				R.dimen.listview_items_padding_left_top);
		top = mContext.getResources().getDimensionPixelSize(
				R.dimen.listview_items_padding_gp_top);

		// mImageProvider = ImageProvider.getInstance((Activity) mContext);
		mCheckedStates = new SparseBooleanArray();
		mHandler = new Handler();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final View view = super.getView(position, convertView, parent);
		Cursor mCursor = (Cursor) getItem(position);
		setupViewData(mCursor);
		final ViewHolderList viewholder;
		if (view != null) {
			viewholder = new ViewHolderList(view);
			holderReference = new WeakReference<ViewHolderList>(viewholder);
			view.setTag(holderReference.get());
		} else {
			viewholder = (ViewHolderList) convertView.getTag();
		}
		if (isSelectMode) {
			holderReference.get().checkBox.setVisibility(View.VISIBLE);

		} else {
			holderReference.get().checkBox.setVisibility(View.INVISIBLE);
		}
		if (mCheckedStates.get(position)) {
			holderReference.get().checkBox.setChecked(true);
		} else {
			holderReference.get().checkBox.setChecked(false);
		}

		if (mLineOneText != null) {
			holderReference.get().mViewHolderLineOne.setText(mLineOneText);
		} else {
			holderReference.get().mViewHolderLineOne.setVisibility(View.GONE);
		}

		if (mLineTwoText != null) {
			holderReference.get().mViewHolderLineTwo.setText(mLineTwoText);
		} else {
			holderReference.get().mViewHolderLineOne
					.setPadding(left, top, 0, 0);
			holderReference.get().mViewHolderLineTwo.setVisibility(View.GONE);
		}

		if ((mPlayingId != 0 && mCurrentId != 0) && mPlayingId == mCurrentId) {
			holderReference.get().mPeakTwo
					.setImageResource(R.anim.peak_meter_black_2);
			mPeakTwoAnimation = (AnimationDrawable) holderReference.get().mPeakTwo
					.getDrawable();
			try {
				if (MusicUtils.mService != null
						&& MusicUtils.mService.isPlaying()) {
					mHandler.post(new Runnable() {

						@Override
						public void run() {
							mPeakTwoAnimation.start();
						}
					});
				} else {
					mPeakTwoAnimation.stop();
					holderReference.get().mPeakTwo
							.setImageResource(R.drawable.icon_play_stop_black);
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else {
			holderReference.get().mPeakTwo.setImageResource(0);
		}
		return view;
	}

	public abstract void setupViewData(Cursor mCursor);

	/**
	 * 全选或全不选
	 *
	 * @param selectAll
	 *            true表示全选,false表示全不选
	 */
	public void selectAllItem(boolean selectAll) {
		if (selectAll) {
			for (int i = 0; i < getCount(); i++) {
				mCheckedStates.put(i, true);
			}
			isSelectAll = true;
		} else {
			for (int i = 0; i < getCount(); i++) {
				mCheckedStates.put(i, false);
			}
			isSelectAll = false;
		}
		notifyDataSetChanged();
	}

	/**
	 * 改变指定位置条目的选择状态，如果已经处于勾选状态则取消勾选，如果处于没有勾选则勾选
	 * 
	 * @param position
	 *            要改变的条目选择状态的位置
	 */
	public void toggleCheckedState(int position) {
		if (position >= 0 && position < getCount()) {
			if (mCheckedStates.get(position)) {
				mCheckedStates.put(position, false);
			} else {
				mCheckedStates.put(position, true);
			}
			notifyDataSetChanged();
		}
	}

	/**
	 * 获得已选择的条目们在列表中的位置
	 * 
	 * @return 所有已选择的条目在列表中的位置
	 */
	public int[] getSelectedItemPositions() {
		int count = 0;
		for (int i = 0; i < getCount(); i++) {
			if (mCheckedStates.get(i)) {
				count++;
			}
		}
		int[] checkedPostions = new int[count];
		for (int i = 0, j = 0; i < getCount(); i++) {
			if (mCheckedStates.get(i)) {
				checkedPostions[j] = i;
				j++;
			}
		}
		return checkedPostions;
	}

	public SparseBooleanArray getmCheckedStates() {
		return mCheckedStates;
	}

	public long[] getSelectedAudioIds() {
		int[] checkedPostions = getSelectedItemPositions();
		long[] selectedAudioIds = new long[checkedPostions.length];
		Cursor mCursor;
		int len = checkedPostions.length;
		for (int i = 0; i < len; i++) {
			mCursor = (Cursor) getItem(checkedPostions[i]);
			int index = mCursor.getColumnIndex(HistoryColumns.AUDIO_ID);
			if (index < 0) {
				index = mCursor.getColumnIndex(BaseColumns._ID);
			}

			selectedAudioIds[i] = mCursor.getLong(index);
		}
		return selectedAudioIds;
	}

}