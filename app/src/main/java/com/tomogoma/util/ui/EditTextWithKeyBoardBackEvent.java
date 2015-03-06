package com.tomogoma.util.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

public class EditTextWithKeyBoardBackEvent extends EditText {

	private OnImeBackListener onImeCallBack;

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
			if (onImeCallBack != null) {
				onImeCallBack.onImeBack(this, this.getText().toString());
			}
		}
		return super.dispatchKeyEvent(event);
	}

	public void setOnEditTextImeBackListener(OnImeBackListener listener) {
		onImeCallBack = listener;
	}

	public static interface OnImeBackListener {

		public void onImeBack(View v, String text);
	}
}
