
 /*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：删除对话框、无耳机对话框的共用对话框
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
*********************************************/

package com.prize.boot;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
  
/**
 *
 * Create custom Dialog windows for your application
 * Custom dialogs rely on custom layouts wich allow you to
 * create and use your own look & feel.
 *
 */
public class CustomDialog extends Dialog {
  
    public CustomDialog(Context context, int theme) {
        super(context, theme);
    }
  
    public CustomDialog(Context context) {
        super(context);
    }
    
    /**
     * Helper class for creating a custom dialog
     */
    public static class Builder {
  
        private Context mContext;
        private String mTitleStr;
        private View mContentView;
        private EditText mPwdEditText;
        private IPassword mPassword;
        private InputMethodManager imm;
        public interface IPassword {
    		void connect(String pwd);
    	}
  
        private DialogInterface.OnClickListener
                        mPositiveButtonClickListener,
                        mNegativeButtonClickListener;
  
        public Builder(Context context, IPassword ipwd) {
        	mPassword = ipwd;
            this.mContext = context;
        }
  
        /**
         * Set the Dialog title from resource
         * @param title
         * @return
         */
        public Builder setTitle(int title) {
            this.mTitleStr = (String) mContext.getText(title);
            return this;
        }
  
        /**
         * Set the Dialog title from String
         * @param title
         * @return
         */
        public Builder setTitle(String title) {
            this.mTitleStr = title;
            return this;
        }
  
        /**
         * Set a custom content view for the Dialog.
         * If a message is set, the contentView is not
         * added to the Dialog...
         * @param v
         * @return
         */
        public Builder setContentView(View v) {
            this.mContentView = v;
            return this;
        }
  
        /**
         * Create the custom dialog
         */
        public CustomDialog create() {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // instantiate the dialog with the custom Theme
            final CustomDialog dialog = new CustomDialog(mContext, R.style.common_custom_dialog);
            View layout = inflater.inflate(R.layout.dialog_input_pwd, null);
            dialog.addContentView(layout, new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            // set the dialog title
            ((TextView) layout.findViewById(R.id.tv_title)).setText(mTitleStr);
            mPwdEditText = (EditText) layout.findViewById(R.id.password);
            imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(mPwdEditText, 0); //显示软键盘
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY); 
            // set the confirm button
            ((Button) layout.findViewById(R.id.btn_right))
            .setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                	if (mPositiveButtonClickListener != null) {
                		mPositiveButtonClickListener.onClick(
                                dialog,
                                DialogInterface.BUTTON_POSITIVE);
                	}
                	if (mPassword != null) {
                		mPassword.connect(mPwdEditText.getText().toString());
                	}
                	hideInput();
                	dialog.dismiss();
                }
            });
            // set the cancel button
            ((Button) layout.findViewById(R.id.btn_left))
            .setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                	if (mNegativeButtonClickListener != null) {
                		mNegativeButtonClickListener.onClick(
                                dialog,
                                DialogInterface.BUTTON_NEGATIVE);
                	}
                	hideInput();
                	dialog.dismiss();
                }
            });
            dialog.setContentView(layout);
            WindowManager.LayoutParams params = 
    				dialog.getWindow().getAttributes();
    				params.width = (int) mContext.getResources().getDimension(R.dimen.custom_dialog_width);
    				params.height = (int) mContext.getResources().getDimension(R.dimen.custom_dialog_height);
    				dialog.getWindow().setAttributes(params);
    				dialog.setCanceledOnTouchOutside(false);
            return dialog;
        }
        
        private void hideInput() {
        	imm.hideSoftInputFromWindow(mPwdEditText.getWindowToken(), 0);
        }
  
    }
  
}

