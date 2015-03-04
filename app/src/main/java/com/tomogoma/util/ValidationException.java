package com.tomogoma.util;

import android.app.Service;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by ogoma on 24/02/15.
 */
public class ValidationException extends Exception {

	private View wrongValuedView;

	public ValidationException(View wrongValuedView) {
		this.wrongValuedView = wrongValuedView;
	}

	public void requestUserInput(Context context) {
		wrongValuedView.requestFocus();
		showSoftKeyboard(context);
	}

	public void showSoftKeyboard(Context context) {

		InputMethodManager imm = (InputMethodManager)
				context.getSystemService(Service.INPUT_METHOD_SERVICE);
		imm.showSoftInput(wrongValuedView, 0);
	}

	public View getWrongValuedView() {
		return wrongValuedView;
	}

	public void setWrongValuedView(View wrongValuedView) {
		this.wrongValuedView = wrongValuedView;
	}

}
