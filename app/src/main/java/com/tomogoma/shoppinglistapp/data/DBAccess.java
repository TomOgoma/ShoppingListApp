package com.tomogoma.shoppinglistapp.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.tomogoma.shoppinglistapp.Category;
import com.tomogoma.shoppinglistapp.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ogoma on 01/03/15.
 */
public class DBAccess {

	DBHelper dbHelper;

	public DBAccess(Context context) {
		dbHelper = new DBHelper(context);
	}


	public void insertItem(Item item) {

		SQLiteDatabase db = dbHelper.getWritableDatabase();

		if (item.isInNewCategory()) {
			db.insert(DatabaseContract.CategoryEntry.TABLE_NAME, null, item.packageCategoryForDB());
		}

		db.insert(DatabaseContract.ItemEntry.TABLE_NAME, null, item.packageForDB());

		if (!item.isBranded()) {
			return;
		}

		db.insert(DatabaseContract.BrandEntry.TABLE_NAME, null, item.packageBrandForDB());

		if (item.brandHasVersion()) {
			db.insert(DatabaseContract.VersionEntry.TABLE_NAME, null, item.packageBrandVersionForDB());
		}

		db.close();
	}

	public List<Item> getAllItems() {

		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Log.d(getClass().getSimpleName(), "query db...");
		Cursor cursor = db.query(DatabaseContract.CategoryEntry.TABLE_NAME, null, null, null, null, null, null);

		List<Item> items = new ArrayList<>();

		if (!cursor.moveToFirst())
			return items;

		int columnIndex = cursor.getColumnIndex(DatabaseContract.CategoryEntry.COLUMN_NAME);

		do {

			String categoryName = cursor.getString(columnIndex);
			Log.d(this.getClass().getName(), "Found category: " + categoryName);
			List<Item> categoryItems = getAllItemsInCategory(categoryName);
			items.addAll(categoryItems);
		} while (cursor.moveToNext());

		cursor.close();
		db.close();
		return items;
	}

	public List<Item> getAllItemsInCategory(String categoryName) {

		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor cursor = db.query(DatabaseContract.ItemEntry.TABLE_NAME, null, DatabaseContract.ItemEntry.COLUMN_CAT_KEY + "=\"" +
				                         categoryName + "\"", null, null, null,
		                         null);

		List<Item> items = new ArrayList<>();
		if (!cursor.moveToFirst())
			return items;

		int nameIndex = cursor.getColumnIndex(DatabaseContract.ItemEntry.COLUMN_NAME);
		int priceIndex = cursor.getColumnIndex(DatabaseContract.ItemEntry.COLUMN_PRICE);
		int descIndex = cursor.getColumnIndex(DatabaseContract.ItemEntry.COLUMN_DESC);
		int qttyIndex = cursor.getColumnIndex(DatabaseContract.ItemEntry.COLUMN_QTTY);
		int measUnitIndex = cursor.getColumnIndex(DatabaseContract.ItemEntry.COLUMN_MEAS_UNIT);
		int usefulUnitIndex = cursor.getColumnIndex(DatabaseContract.ItemEntry.COLUMN_USEFUL_UNIT);
		int usefulPerActualIndex = cursor.getColumnIndex(DatabaseContract.ItemEntry.COLUMN_USEFUL_PER_MEAS);

		do {

			String itemName = cursor.getString(nameIndex);
			String measUnit = cursor.getString(measUnitIndex);
			String usefulUnit = cursor.getString(usefulUnitIndex);
			float usefulPerActual = cursor.getFloat(usefulPerActualIndex);

			Log.d(this.getClass().getName(), "Found item: " + itemName);

			List<Item> brandItems = getAllBrands(categoryName, itemName);

			//  TODO add measunit, useful unit etc to item

			if (brandItems.isEmpty()) {

				Log.d(this.getClass().getName(), "Item has no brands");
				Log.d(this.getClass().getName(), "Quantity: " + cursor.getFloat(qttyIndex));
				Log.d(this.getClass().getName(), "Price: " + cursor.getDouble(priceIndex));
				Log.d(this.getClass().getName(), "Description: " + cursor.getString(descIndex));
				Log.d(this.getClass().getName(), "Meas unit: " + measUnit);
				Log.d(this.getClass().getName(), "Useful unit: " + usefulUnit);
				Log.d(this.getClass().getName(), "Useful Per act: " + usefulPerActual);
				items.add(
						new Item(
								categoryName, itemName, null, null,
								cursor.getDouble(priceIndex),
								cursor.getFloat(qttyIndex),
								measUnit,
								usefulUnit,
								usefulPerActual,
								cursor.getString(descIndex)
						)
				);
			} else {
				Log.d(this.getClass().getName(), "Item has brands, count: " + brandItems.size());
				for (Item brandItem : brandItems) {
					brandItem.setMeasUnit(measUnit);
					brandItem.setUsefulUnit(usefulUnit);
					brandItem.setUsefulUnitsPerActual(usefulPerActual);
				}
				items.addAll(brandItems);
			}

		} while (cursor.moveToNext());

		cursor.close();
		db.close();
		return items;
	}

	public List<Item> getAllBrands(String categoryName, String itemName) {

		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor cursor = db.query(DatabaseContract.BrandEntry.TABLE_NAME, null, DatabaseContract.BrandEntry.COLUMN_ITEM_KEY + "=\"" +
				                         itemName + "\"", null, null,
		                         null, null);

		List<Item> items = new ArrayList<>();
		if (!cursor.moveToFirst())
			return items;

		int nameIndex = cursor.getColumnIndex(DatabaseContract.BrandEntry.COLUMN_NAME);
		int priceIndex = cursor.getColumnIndex(DatabaseContract.BrandEntry.COLUMN_PRICE);
		int descIndex = cursor.getColumnIndex(DatabaseContract.BrandEntry.COLUMN_DESC);
		int qttyIndex = cursor.getColumnIndex(DatabaseContract.BrandEntry.COLUMN_QTTY);

		do {

			String brandName = cursor.getString(nameIndex);
			Log.d(this.getClass().getName(), "Found brand: " + brandName);
			List<Item> brandVersions = getAllVersions(categoryName, itemName, brandName);

			if (brandVersions.isEmpty()) {
				Log.d(this.getClass().getName(), "Brand has no versions");
				Log.d(this.getClass().getName(), "Quantity: " + cursor.getFloat(qttyIndex));
				Log.d(this.getClass().getName(), "Price: " + cursor.getDouble(priceIndex));
				Log.d(this.getClass().getName(), "Description: " + cursor.getString(descIndex));
				items.add(
						new Item(
								categoryName, itemName, brandName,
								cursor.getString(nameIndex),
								cursor.getDouble(priceIndex),
								cursor.getFloat(qttyIndex),
								cursor.getString(descIndex)
						)
				);
				break;
			}

			Log.d(this.getClass().getName(), "Found versions, size: " + brandVersions.size());
			items.addAll(brandVersions);
		} while (cursor.moveToNext());

		cursor.close();
		db.close();
		return items;
	}

	public List<Item> getAllVersions(String categoryName, String itemName, String brandName) {

		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor cursor = db.query(DatabaseContract.VersionEntry.TABLE_NAME, null, DatabaseContract.VersionEntry.COLUMN_BRAND_KEY + "=\"" +
				                         brandName + "\"", null,
		                         null, null, null);

		List<Item> items = new ArrayList<>();
		if (!cursor.moveToFirst())
			return items;

		int nameIndex = cursor.getColumnIndex(DatabaseContract.VersionEntry.COLUMN_NAME);
		int priceIndex = cursor.getColumnIndex(DatabaseContract.VersionEntry.COLUMN_PRICE);
		int descIndex = cursor.getColumnIndex(DatabaseContract.VersionEntry.COLUMN_DESC);
		int qttyIndex = cursor.getColumnIndex(DatabaseContract.VersionEntry.COLUMN_QTTY);

		do {
			Log.d(this.getClass().getName(), "Found version: " + cursor.getString(nameIndex));
			Log.d(this.getClass().getName(), "Quantity: " + cursor.getFloat(qttyIndex));
			Log.d(this.getClass().getName(), "Price: " + cursor.getDouble(priceIndex));
			Log.d(this.getClass().getName(), "Description: " + cursor.getString(descIndex));
			items.add(
					new Item(
							categoryName, itemName, brandName,
							cursor.getString(nameIndex),
							cursor.getDouble(priceIndex),
							cursor.getFloat(qttyIndex),
							cursor.getString(descIndex)
					)
			);
		} while (cursor.moveToNext());

		cursor.close();
		db.close();
		return items;
	}

	public List<Category> getAllCategories() {

		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor cursor = db.query(DatabaseContract.CategoryEntry.TABLE_NAME, null, null, null, null, null, null);

		List<Category> categories = new ArrayList<>();

		if (!cursor.moveToFirst())
			return categories;

		int columnIndex = cursor.getColumnIndex(DatabaseContract.CategoryEntry.COLUMN_NAME);

		do {

			String categoryName = cursor.getString(columnIndex);
			categories.add(new Category(categoryName, false));
		} while (cursor.moveToNext());

		cursor.close();
		db.close();
		return categories;
	}
}
