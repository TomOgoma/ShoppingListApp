package com.tomogoma.shoppinglistapp.util;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

/**
 * Created by Tom Ogoma on 05/03/15.
 */
public class UI {

	public static void hideKeyboard(Context context, View v) {

		if (v == null) {
			return;
		}

		InputMethodManager imm = (InputMethodManager)context.getSystemService(
				Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	public static void showToast(Context context, String message) {

		Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
}
