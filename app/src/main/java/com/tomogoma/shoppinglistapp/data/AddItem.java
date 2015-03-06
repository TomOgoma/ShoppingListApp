package com.tomogoma.shoppinglistapp.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.EditText;

import com.tomogoma.shoppinglistapp.data.DatabaseContract.CategoryEntry;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.ItemEntry;

/**
 * Created by ogoma on 01/03/15.
 */
public class AddItem {

	public static long addCategory(Context context, EditText categoryName) {
		return addCategory(context, categoryName.getText().toString());
	}

	public static long addCategory(Context context, String categoryName) {

		if (categoryName == null || categoryName.isEmpty()) {
			categoryName = CategoryEntry.DEFAULT_CATEGORY_NAME;
		}

		ContentResolver contentResolver = context.getContentResolver();

		Cursor cursor = contentResolver.query(
				DatabaseContract.CategoryEntry.CONTENT_URI,
				new String[]{DatabaseContract.CategoryEntry._ID},
				DatabaseContract.CategoryEntry.COLUMN_NAME + " = ?",
				new String[]{categoryName},
				null);

		if (cursor.moveToFirst()) {
			int categoryIdIndex = cursor.getColumnIndex(DatabaseContract.CategoryEntry._ID);
			return cursor.getLong(categoryIdIndex);
		}

		ContentValues contentValues = new ContentValues();
		contentValues.put(DatabaseContract.CategoryEntry.COLUMN_NAME, categoryName);

		Uri categoryInsertUri =
				contentResolver.insert(DatabaseContract.CategoryEntry.CONTENT_URI, contentValues);

		return ContentUris.parseId(categoryInsertUri);
	}

	public static long addItem(Context context, long categoryID, EditText itemName,
	                           EditText unitPrice, EditText quantity, EditText lastsFor,
	                           EditText measUnit, String lastsForUnit, EditText description) {


		double price = 0d;
		float qtty = 0f, usePerAct = 0f;

		Log.d(AddItem.class.getSimpleName(), "edittext usefulperMeas: " + lastsFor.getText().toString());

		//  Multiple trys to avoid short-circuiting
		//  Do nothing for number format exceptions (assume empty -> default 0)
		try {
			price = Double.parseDouble(unitPrice.getText().toString());
		}
		catch (Exception e) {}

		try {
			qtty = Float.parseFloat(quantity.getText().toString());
		}
		catch (Exception e) {}

		try {
			usePerAct = Float.parseFloat(lastsFor.getText().toString());
		}
		catch (Exception e) {}

		return addItem(context, categoryID, itemName.getText().toString(), price, qtty,
		               measUnit.getText().toString(), lastsForUnit, usePerAct, description.getText().toString());
	}

	public static long addItem(Context context, long categoryID, String itemName,
	                           double unitPrice, float quantity, String measUnit, String usefulUnit,
	                           float usefulUnitsPerActual, String description) {

		if (itemName == null || itemName.isEmpty()) {
			return -1;
		}

		ContentResolver contentResolver = context.getContentResolver();

		Cursor cursor = contentResolver.query(
				DatabaseContract.ItemEntry.CONTENT_URI,
				new String[]{ItemEntry.TABLE_NAME + "." + ItemEntry._ID},
				DatabaseContract.ItemEntry.COLUMN_NAME + " = ?",
				new String[]{itemName},
				null);

		if (cursor.moveToFirst()) {
			// TODO figure out how to return error message together with id
			// in order to allow user to edit the item
			// throw an Exception instead?
			// int itemIdIndex = cursor.getColumnIndex(DatabaseContract.ItemEntry._ID);
			// return cursor.getLong(itemIdIndex);
			return -1;
		}

		ContentValues contentValues = new ContentValues();
		contentValues.put(DatabaseContract.ItemEntry.COLUMN_CAT_KEY, categoryID);
		contentValues.put(DatabaseContract.ItemEntry.COLUMN_NAME, itemName);
		contentValues.put(ItemEntry.COLUMN_DESC, description);
		contentValues.put(ItemEntry.COLUMN_PRICE, unitPrice);
		contentValues.put(ItemEntry.COLUMN_QTTY, quantity);
		contentValues.put(ItemEntry.COLUMN_MEAS_UNIT, measUnit);
		contentValues.put(ItemEntry.COLUMN_USEFUL_UNIT, usefulUnit);
		contentValues.put(ItemEntry.COLUMN_USEFUL_PER_MEAS, usefulUnitsPerActual);

		Uri itemInsertUri =
				contentResolver.insert(DatabaseContract.ItemEntry.CONTENT_URI, contentValues);

		return ContentUris.parseId(itemInsertUri);
	}

}