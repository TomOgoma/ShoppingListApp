package com.tomogoma.shoppinglistapp;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

public class EditTextWithKeyBoardBackEvent extends EditText {

	private OnImeBackListener mOnImeCallBack;

	public EditTextWithKeyBoardBackEvent(Context context) {
		super(context);
	}

	public EditTextWithKeyBoardBackEvent(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public EditTextWithKeyBoardBackEvent(Context context, AttributeSet attrs,
	                                     int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onKeyPreIme(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_UP) {
			if (mOnImeCallBack != null) {
				mOnImeCallBack.onImeBack(this, this.getText().toString());
			}
		}
		return super.dispatchKeyEvent(event);
	}

	public void setOnEditTextImeBackListener(OnImeBackListener listener) {
		mOnImeCallBack = listener;
	}

	public static interface OnImeBackListener {

		public void onImeBack(View v, String text);
	}
}
