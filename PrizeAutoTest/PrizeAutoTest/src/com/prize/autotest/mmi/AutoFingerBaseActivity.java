package com.prize.autotest.mmi;

import com.prize.autotest.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AutoFingerBaseActivity extends Activity implements OnClickListener {

	private View contentView;
	private RelativeLayout headerLayoutArea;
	private RelativeLayout leftClickArea;
	private LinearLayout subContentLayout;
	private Button backButton;
	private LinearLayout baseLayoutArea;
	private TextView titleHeaderView;
	private RelativeLayout rightClickArea;
	private TextView operationButton;
	private TextView midTitleView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Window window = getWindow();
		window.requestFeature(Window.FEATURE_NO_TITLE);
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			window = getWindow();
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
					| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			window.getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(getResources().getColor(
					R.color.title_header_bg_color));
		}

		setContentView(R.layout.activity_base);
		initViews();
	}

	private void initViews() {
		baseLayoutArea = (LinearLayout) findViewById(R.id.base_avitivity_rl);

		headerLayoutArea = (RelativeLayout) findViewById(R.id.header_layout);

		leftClickArea = (RelativeLayout) findViewById(R.id.left_click_area);
		leftClickArea.setOnClickListener(this);

		backButton = (Button) findViewById(R.id.back_image_view);

		rightClickArea = (RelativeLayout) findViewById(R.id.right_click_area);
		rightClickArea.setOnClickListener(this);

		operationButton = (TextView) findViewById(R.id.right_operation_button);

		titleHeaderView = (TextView) findViewById(R.id.title_header_view);
		midTitleView = (TextView) findViewById(R.id.mid_title_view);

		subContentLayout = (LinearLayout) findViewById(R.id.sub_layout);
	}

	public void setSubContentView(int resId) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contentView = inflater.inflate(resId, null);
		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		contentView.setLayoutParams(layoutParams);
		if (null != subContentLayout) {
			subContentLayout.addView(contentView);
		}
	}

	public void setContentLayout(View view) {
		if (null != subContentLayout) {
			subContentLayout.addView(view);
		}
	}

	public View getLyContentView() {
		return contentView;
	}

	public void setHeaderLayoutAreaRes(int resId) {
		if (null != headerLayoutArea) {
			headerLayoutArea.setBackgroundColor(resId);
		}
	}

	public void setHeaderLayoutAreaDrawable(Drawable drawable) {
		if (null != headerLayoutArea) {
			headerLayoutArea.setBackground(drawable);
		}
	}

	public void displayHeaderLayoutArea() {
		if (null != headerLayoutArea) {
			headerLayoutArea.setVisibility(View.VISIBLE);
		}
	}

	public void hideHeaderLayoutArea() {
		if (null != headerLayoutArea) {
			headerLayoutArea.setVisibility(View.INVISIBLE);
		}
	}

	public void setLeftClickAreaRes(int resId) {
		if (null != leftClickArea) {
			leftClickArea.setBackgroundResource(resId);
		}
	}

	public void setLeftClickAreaDrawable(Drawable drawable) {
		if (null != leftClickArea) {
			leftClickArea.setBackground(drawable);
		}
	}

	public void displayLeftClickArea() {
		if (null != leftClickArea) {
			leftClickArea.setVisibility(View.VISIBLE);
		}
	}

	public void hideLeftClickArea() {
		if (null != leftClickArea) {
			leftClickArea.setVisibility(View.INVISIBLE);
		}
	}

	public void hideLeftClickAreaGone() {
		if (null != leftClickArea) {
			leftClickArea.setVisibility(View.GONE);
		}
	}

	public void setLeftClickAreaListener(OnClickListener onClickListener) {
		if (null != leftClickArea) {
			leftClickArea.setOnClickListener(onClickListener);
		}
	}

	public void setBackButtonRes(int resId) {
		if (null != backButton) {
			backButton.setBackgroundResource(resId);
		}
	}

	public void setBackButtonDrawable(Drawable drawable) {
		if (null != backButton) {
			backButton.setBackground(drawable);
		}
	}

	public void displayBackButton() {
		if (null != backButton) {
			backButton.setVisibility(View.VISIBLE);
		}
	}

	public void hideBackButton() {
		if (null != backButton) {
			backButton.setVisibility(View.INVISIBLE);
		}
	}

	public void hideBackButtonGone() {
		if (null != backButton) {
			backButton.setVisibility(View.GONE);
		}
	}

	public void setLeftButtonPressed(boolean flag) {
		if (null != backButton) {
			backButton.setPressed(flag);
		}
	}

	public void displayRightClickArea() {
		if (null != rightClickArea) {
			rightClickArea.setVisibility(View.VISIBLE);
		}
	}

	public void hideRightClickArea() {
		if (null != rightClickArea) {
			rightClickArea.setVisibility(View.INVISIBLE);
		}
	}

	public void setRightClickAreaListener(OnClickListener onClickListener) {
		if (null != rightClickArea) {
			rightClickArea.setOnClickListener(onClickListener);
		}
	}

	public void setOperationButtonRes(int resId) {
		if (null != operationButton) {
			operationButton.setBackgroundResource(resId);
		}
	}

	public void setOperationButtonDrawable(Drawable drawable) {
		if (null != operationButton) {
			operationButton.setBackground(drawable);
		}
	}

	public void displayOperationButton() {
		if (null != operationButton) {
			operationButton.setVisibility(View.VISIBLE);
		}
	}

	public void hideOperationButton() {
		if (null != operationButton) {
			operationButton.setVisibility(View.INVISIBLE);
		}
	}

	public void setOperationButtonPressed(boolean flag) {
		if (null != operationButton) {
			operationButton.setPressed(flag);
		}
	}

	public void setTitleHeaderText(String title) {
		if (null != titleHeaderView) {
			titleHeaderView.setText(title);
		}
	}

	public void setMidTitleText(String title) {
		if (null != midTitleView) {
			midTitleView.setText(title);
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.left_click_area:
			backButton.setEnabled(true);
			finish();
			break;
		default:
			break;
		}
	}
}
