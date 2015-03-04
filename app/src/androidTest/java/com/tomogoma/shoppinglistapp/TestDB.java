package com.tomogoma.shoppinglistapp;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.tomogoma.shoppinglistapp.data.DBHelper;
import com.tomogoma.shoppinglistapp.data.DatabaseContract;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.ItemEntry;

import static com.tomogoma.shoppinglistapp.data.DatabaseContract.CategoryEntry;

/**
 * Created by ogoma on 27/02/15.
 */
public class TestDB extends AndroidTestCase {

	public static final Item testItem = new Item("category name", "name", "brand", "version", 500d, 4f, "meas unit", "useful unit", 3f,
	                                             "description");
	private DBHelper dbHelper;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		dbHelper = new DBHelper(mContext);
	}

	public void testCategoryTableCreatedProper() {

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.delete(CategoryEntry.TABLE_NAME, null, null);

		ContentValues testValues = testItem.packageCategoryForDB();

		long rowID = db.insert(CategoryEntry.TABLE_NAME, null, testValues);
		assertFalse(rowID == -1);

		Cursor cursor = db.query(CategoryEntry.TABLE_NAME, null, null, null, null, null, null);
		assertTrue("No items in db", cursor.moveToFirst());

		validateCategoryCursor(cursor, testItem);

		cursor.close();
		db.delete(CategoryEntry.TABLE_NAME, null, null);
		db.close();
	}

	public static void validateCategoryCursor(Cursor cursor, Item testItem) {

		int columnIndex = cursor.getColumnIndex(CategoryEntry.COLUMN_NAME);
		assertFalse("column does not exist", columnIndex == -1);
		assertEquals(testItem.getCategoryName(), cursor.getString(columnIndex));
		assertFalse("table has more items than expected >1", cursor.moveToNext());
	}

	public void testItemTableCreatedProper() {

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.delete(ItemEntry.TABLE_NAME, null, null);

		ContentValues testValues = testItem.packageForDB();

		long rowID = db.insert(ItemEntry.TABLE_NAME, null, testValues);
		assertFalse(rowID == -1);

		Cursor cursor = db.query(ItemEntry.TABLE_NAME, null, null, null, null, null, null);
		assertTrue("No items in db", cursor.moveToFirst());

		validateItemCursor(cursor, testItem);

		cursor.close();
		db.delete(ItemEntry.TABLE_NAME, null, null);
		db.close();
	}

	public static void validateItemCursor(Cursor cursor, Item testItem) {

		int columnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_MEAS_UNIT);
		assertFalse("column does not exist", columnIndex == -1);
		assertEquals(testItem.getMeasUnit(), cursor.getString(columnIndex));
		assertFalse("table has more items than expected >1", cursor.moveToNext());
	}

	public void testBrandTableCreatedProper() {

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.delete(DatabaseContract.BrandEntry.TABLE_NAME, null, null);

		ContentValues testValues = testItem.packageBrandForDB();

		long rowID = db.insert(DatabaseContract.BrandEntry.TABLE_NAME, null, testValues);
		assertFalse(rowID == -1);

		Cursor cursor = db.query(DatabaseContract.BrandEntry.TABLE_NAME, null, null, null, null, null, null);
		assertTrue("No items in db", cursor.moveToFirst());

		int columnIndex = cursor.getColumnIndex(DatabaseContract.BrandEntry.COLUMN_ITEM_KEY);
		assertFalse("column does not exist", columnIndex == -1);
		assertEquals(testItem.getItemName(), cursor.getString(columnIndex));
		assertFalse("table has more items than expected >1", cursor.moveToNext());

		cursor.close();
		db.delete(DatabaseContract.BrandEntry.TABLE_NAME, null, null);
		db.close();
	}

	public void testBrandVersionTableCreatedProper() {

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.delete(DatabaseContract.VersionEntry.TABLE_NAME, null, null);

		ContentValues testValues = testItem.packageBrandVersionForDB();

		long rowID = db.insert(DatabaseContract.VersionEntry.TABLE_NAME, null, testValues);
		assertFalse(rowID == -1);

		Cursor cursor = db.query(DatabaseContract.VersionEntry.TABLE_NAME, null, null, null, null, null, null);
		assertTrue("No items in db", cursor.moveToFirst());

		int columnIndex = cursor.getColumnIndex(DatabaseContract.VersionEntry.COLUMN_PRICE);
		assertFalse("column does not exist", columnIndex == -1);
		assertEquals(testItem.getUnitPrice(), cursor.getDouble(columnIndex));
		assertFalse("table has more items than expected >1", cursor.moveToNext());

		cursor.close();
		db.delete(DatabaseContract.VersionEntry.TABLE_NAME, null, null);
		db.close();
	}

}
