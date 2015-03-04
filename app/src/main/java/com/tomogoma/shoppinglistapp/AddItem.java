package com.tomogoma.shoppinglistapp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.widget.EditText;

import com.tomogoma.shoppinglistapp.data.DatabaseContract;

/**
 * Created by ogoma on 01/03/15.
 */
public class AddItem {

	public static long addCategory(Context context, EditText categoryName) {
		return addCategory(context, categoryName.getText().toString());
	}

	public static long addCategory(Context context, String categoryName) {

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
	                           EditText unitPrice, EditText quantity, EditText measUnit, EditText usefulUnit,
	                           EditText usefulUnitsPerActual, EditText description) {

		double price = 0d;
		float qtty = 0f, usePerAct = 0f;

		try {
			price = Double.parseDouble(unitPrice.getText().toString());
			qtty = Float.parseFloat(quantity.getText().toString());
			usePerAct = Float.parseFloat(usefulUnitsPerActual.getText().toString());
		} catch (Exception e) {
			//  Do nothing for number format exception (assume empty -> default 0)
		}

		return addItem(
				context,
				categoryID,
				itemName.getText().toString(),
				price,
				qtty,
				measUnit.getText().toString(),
				usefulUnit.getText().toString(),
				usePerAct,
				description.getText().toString()
		);
	}

	public static long addItem(Context context, long categoryID, String itemName,
	                           double unitPrice, float quantity, String measUnit, String usefulUnit,
	                           float usefulUnitsPerActual, String description) {

		ContentResolver contentResolver = context.getContentResolver();

		Cursor cursor = contentResolver.query(
				DatabaseContract.ItemEntry.CONTENT_URI,
				new String[]{DatabaseContract.ItemEntry._ID},
				DatabaseContract.ItemEntry.COLUMN_NAME + " = ?",
				new String[]{itemName},
				null);

		if (cursor.moveToFirst()) {
			int itemIdIndex = cursor.getColumnIndex(DatabaseContract.ItemEntry._ID);
			return cursor.getLong(itemIdIndex);
		}

		ContentValues contentValues = new ContentValues();
		contentValues.put(DatabaseContract.ItemEntry.COLUMN_CAT_KEY, categoryID);
		contentValues.put(DatabaseContract.ItemEntry.COLUMN_NAME, itemName);
		contentValues.put(DatabaseContract.ItemEntry.COLUMN_DESC, description);
		contentValues.put(DatabaseContract.ItemEntry.COLUMN_PRICE, unitPrice);
		contentValues.put(DatabaseContract.ItemEntry.COLUMN_QTTY, quantity);
		contentValues.put(DatabaseContract.ItemEntry.COLUMN_MEAS_UNIT, measUnit);
		contentValues.put(DatabaseContract.ItemEntry.COLUMN_USEFUL_UNIT, usefulUnit);
		contentValues.put(DatabaseContract.ItemEntry.COLUMN_USEFUL_PER_MEAS, usefulUnitsPerActual);

		Uri itemInsertUri =
				contentResolver.insert(DatabaseContract.ItemEntry.CONTENT_URI, contentValues);

		return ContentUris.parseId(itemInsertUri);
	}

	public static long addBrand(Context context, long itemID, EditText brandName,
	                            EditText unitPrice, EditText quantity, EditText description) {

		double price = 0d;
		float qtty = 0f;

		try {
			price = Double.parseDouble(unitPrice.getText().toString());
			qtty = Float.parseFloat(quantity.getText().toString());
		} catch (Exception e) {
			//  Do nothing for number format exception (assume empty -> default 0)
		}

		return addBrand(
				context,
				itemID,
				brandName.getText().toString(),
				price,
				qtty,
				description.getText().toString()
		);
	}

	public static long addBrand(Context context, long itemID, String brandName,
	                            double unitPrice, float quantity, String description) {

		ContentResolver contentResolver = context.getContentResolver();

		//  Only consider a duplicate if brandName is duplicate for the same item
		Cursor cursor = contentResolver.query(
				DatabaseContract.BrandEntry.CONTENT_URI,
				new String[]{DatabaseContract.BrandEntry._ID},
				DatabaseContract.BrandEntry.COLUMN_NAME + " = ?" +
						" AND " +
						DatabaseContract.BrandEntry.COLUMN_ITEM_KEY + "=?",
				new String[]{brandName, String.valueOf(itemID)},
				null);

		if (cursor.moveToFirst()) {
			int brandIdIndex = cursor.getColumnIndex(DatabaseContract.BrandEntry._ID);
			return cursor.getLong(brandIdIndex);
		}

		ContentValues contentValues = new ContentValues();
		contentValues.put(DatabaseContract.BrandEntry.COLUMN_ITEM_KEY, itemID);
		contentValues.put(DatabaseContract.BrandEntry.COLUMN_NAME, brandName);
		contentValues.put(DatabaseContract.BrandEntry.COLUMN_DESC, description);
		contentValues.put(DatabaseContract.BrandEntry.COLUMN_PRICE, unitPrice);
		contentValues.put(DatabaseContract.BrandEntry.COLUMN_QTTY, quantity);

		Uri brandInsertUri =
				contentResolver.insert(DatabaseContract.BrandEntry.CONTENT_URI, contentValues);

		return ContentUris.parseId(brandInsertUri);
	}

	public static long addVersion(Context context, long brandID, EditText versionName,
	                              EditText unitPrice, EditText quantity, EditText description) {

		double price = 0d;
		float qtty = 0f;

		try {
			price = Double.parseDouble(unitPrice.getText().toString());
			qtty = Float.parseFloat(quantity.getText().toString());
		} catch (Exception e) {
			//  Do nothing for number format exception (assume empty -> default 0)
		}

		return addVersion(
				context,
				brandID,
				versionName.getText().toString(),
				price,
				qtty,
				description.getText().toString()
		);
	}

	public static long addVersion(Context context, long brandID, String versionName,
	                              double unitPrice, float quantity, String description) {

		ContentResolver contentResolver = context.getContentResolver();

		//  Only consider a duplicate if brandName is duplicate for the same item
		Cursor cursor = contentResolver.query(
				DatabaseContract.VersionEntry.CONTENT_URI,
				new String[]{DatabaseContract.VersionEntry._ID},
				DatabaseContract.VersionEntry.COLUMN_NAME + " = ?" +
						" AND " +
						DatabaseContract.VersionEntry.COLUMN_BRAND_KEY + "=?",
				new String[]{versionName, String.valueOf(brandID)},
				null);

		if (cursor.moveToFirst()) {
			int versionIdIndex = cursor.getColumnIndex(DatabaseContract.VersionEntry._ID);
			return cursor.getLong(versionIdIndex);
		}

		ContentValues contentValues = new ContentValues();
		contentValues.put(DatabaseContract.VersionEntry.COLUMN_BRAND_KEY, brandID);
		contentValues.put(DatabaseContract.VersionEntry.COLUMN_NAME, versionName);
		contentValues.put(DatabaseContract.VersionEntry.COLUMN_DESC, description);
		contentValues.put(DatabaseContract.VersionEntry.COLUMN_PRICE, unitPrice);
		contentValues.put(DatabaseContract.VersionEntry.COLUMN_QTTY, quantity);

		Uri versionInsertUri =
				contentResolver.insert(DatabaseContract.VersionEntry.CONTENT_URI, contentValues);

		return ContentUris.parseId(versionInsertUri);
	}

}