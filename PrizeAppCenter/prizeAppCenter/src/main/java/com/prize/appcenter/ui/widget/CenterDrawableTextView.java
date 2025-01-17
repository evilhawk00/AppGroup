package com.prize.appcenter.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

/*
 * Created by 聂礼刚 on 16/11/11.
 */
public class CenterDrawableTextView extends TextView {

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public CenterDrawableTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public CenterDrawableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public CenterDrawableTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CenterDrawableTextView(Context context) {
		super(context);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		CenterDrawableHelper.preDraw(this, canvas);
		super.onDraw(canvas);
	}

}
