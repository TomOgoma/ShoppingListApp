package com.tomogoma.shoppinglistapp.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.tomogoma.shoppinglistapp.data.DatabaseContract.CategoryEntry;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.ItemEntry;

/**
 * Created by ogoma on 01/03/15.
 */
public class DBUpdateHelper {

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

	public static long addItem(Context context, Item item) {

		if (item.mItemName == null || item.mItemName.isEmpty()) {
			return -1;
		}

		ContentResolver contentResolver = context.getContentResolver();

		Cursor cursor = contentResolver.query(
				DatabaseContract.ItemEntry.CONTENT_URI,
				new String[]{ItemEntry.TABLE_NAME + "." + ItemEntry._ID},
				DatabaseContract.ItemEntry.COLUMN_NAME + " = ?",
				new String[]{item.mItemName},
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
		contentValues.put(DatabaseContract.ItemEntry.COLUMN_CAT_KEY, item.mCategoryID);
		contentValues.put(DatabaseContract.ItemEntry.COLUMN_NAME, item.mItemName);
		contentValues.put(ItemEntry.COLUMN_DESC, item.mDescription);
		contentValues.put(ItemEntry.COLUMN_PRICE, item.mPrice);
		contentValues.put(ItemEntry.COLUMN_QTTY, item.mQuantity);
		contentValues.put(ItemEntry.COLUMN_MEAS_UNIT, item.mMeasUnit);
		contentValues.put(ItemEntry.COLUMN_USEFUL_UNIT, item.mLastsForUnit);
		contentValues.put(ItemEntry.COLUMN_USEFUL_PER_MEAS, item.mLastsFor);

		Uri itemInsertUri =
				contentResolver.insert(DatabaseContract.ItemEntry.CONTENT_URI, contentValues);

		return ContentUris.parseId(itemInsertUri);
	}

	public static int updateItem(Context context, Item item) {

		if (item.mItemName == null || item.mItemName.isEmpty() || item.mItemID == -1) {
			return -1;
		}

		ContentValues contentValues = new ContentValues();
		contentValues.put(DatabaseContract.ItemEntry.COLUMN_CAT_KEY, item.mCategoryID);
		contentValues.put(DatabaseContract.ItemEntry.COLUMN_NAME, item.mItemName);
		contentValues.put(ItemEntry.COLUMN_DESC, item.mDescription);
		contentValues.put(ItemEntry.COLUMN_PRICE, item.mPrice);
		contentValues.put(ItemEntry.COLUMN_QTTY, item.mQuantity);
		contentValues.put(ItemEntry.COLUMN_MEAS_UNIT, item.mMeasUnit);
		contentValues.put(ItemEntry.COLUMN_USEFUL_UNIT, item.mLastsForUnit);
		contentValues.put(ItemEntry.COLUMN_USEFUL_PER_MEAS, item.mLastsFor);

		String whereClause = ItemEntry._ID + " = ?";
		String[] whereArgs = new String[]{String.valueOf(item.mItemID)};
		ContentResolver contentResolver = context.getContentResolver();

		return contentResolver.update(DatabaseContract.ItemEntry.CONTENT_URI, contentValues, whereClause, whereArgs);
	}

}