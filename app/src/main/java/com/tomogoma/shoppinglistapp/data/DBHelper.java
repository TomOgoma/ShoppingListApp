package com.tomogoma.shoppinglistapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tomogoma.shoppinglistapp.data.DatabaseContract.BrandEntry;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.CommonAttributesEntry;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.VersionEntry;

import static com.tomogoma.shoppinglistapp.data.DatabaseContract.CategoryEntry;
import static com.tomogoma.shoppinglistapp.data.DatabaseContract.ItemEntry;

/**
 * Created by ogoma on 27/02/15.
 */
public class DBHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 3;
	private static final String DATABASE_NAME = "shoppingList.db";

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		//  Duplicates of Category names not allowed
		final String SQL_CREATE_CATEGORY_TABLE =
				"CREATE TABLE " + CategoryEntry.TABLE_NAME + " (" +

						CategoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
						CategoryEntry.COLUMN_NAME + " TEXT NOT NULL," +

						"UNIQUE (" + CategoryEntry.COLUMN_NAME + ") ON CONFLICT IGNORE," +
						" CHECK(" + CategoryEntry.COLUMN_NAME + " <> '')" +
						")";

		final String SQL_CREATE_COMMON_ATTRIBUTES_TABLE =
				"CREATE TABLE " + CommonAttributesEntry.TABLE_NAME + " (" +

						CommonAttributesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

						CommonAttributesEntry.COLUMN_PRICE + " REAL," +
						CommonAttributesEntry.COLUMN_QTTY + " REAL," +
						CommonAttributesEntry.COLUMN_DESC + " TEXT," +
						CommonAttributesEntry.COLUMN_MEAS_UNIT + " TEXT," +
						CommonAttributesEntry.COLUMN_USEFUL_UNIT + " TEXT," +
						CommonAttributesEntry.COLUMN_USEFUL_PER_MEAS + " REAL," +
						CommonAttributesEntry.COLUMN_IN_LIST + " INTEGER," +
						CommonAttributesEntry.COLUMN_IN_CART + " INTEGER" +
						")";

		//  Duplicates of Item names not allowed (not even when under different categories)
		//  Duplicates name assumed as updates for the rest of the columns of the existing!
		final String SQL_CREATE_ITEM_TABLE =
				"CREATE TABLE " + ItemEntry.TABLE_NAME + " (" +

						ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
						ItemEntry.COLUMN_CAT_KEY + " INTEGER NOT NULL," +
						ItemEntry.COLUMN_COMMON_ATTRIBUTES_KEY + " INTEGER," +

						ItemEntry.COLUMN_NAME + " TEXT NOT NULL," +

						"FOREIGN KEY (" + ItemEntry.COLUMN_CAT_KEY + ") REFERENCES " +
						CategoryEntry.TABLE_NAME + " (" + CategoryEntry._ID + ")," +
						"FOREIGN KEY (" + ItemEntry.COLUMN_COMMON_ATTRIBUTES_KEY + ") REFERENCES " +
						CommonAttributesEntry.TABLE_NAME + " (" + CommonAttributesEntry._ID + ")," +
						"UNIQUE (" + ItemEntry.COLUMN_NAME + ") ON CONFLICT REPLACE," +
						" CHECK(" + ItemEntry.COLUMN_NAME + " <> '')" +
						")";

		//  Duplicate brand names acceptable - useful for different Items that share a brand
		final String SQL_CREATE_BRAND_TABLE =
				"CREATE TABLE " + BrandEntry.TABLE_NAME + " (" +

						BrandEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
						BrandEntry.COLUMN_ITEM_KEY + " INTEGER NOT NULL," +
						BrandEntry.COLUMN_COMMON_ATTRIBUTES_KEY + " INTEGER," +

						BrandEntry.COLUMN_NAME + " TEXT NOT NULL," +

						"FOREIGN KEY (" + BrandEntry.COLUMN_ITEM_KEY + ") REFERENCES " +
						ItemEntry.TABLE_NAME + " (" + ItemEntry._ID + ")," +
						"FOREIGN KEY (" + BrandEntry.COLUMN_COMMON_ATTRIBUTES_KEY + ") REFERENCES " +
						CommonAttributesEntry.TABLE_NAME + " (" + CommonAttributesEntry._ID + ")," +
						" CHECK(" + BrandEntry.COLUMN_NAME + " <> '')" +
						")";

		//  Duplicate version names acceptable - useful for different brands that share a version
		final String SQL_CREATE_BRAND_VERSION_TABLE =
				"CREATE TABLE " + VersionEntry.TABLE_NAME + " (" +

						VersionEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
						VersionEntry.COLUMN_BRAND_KEY + " INTEGER NOT NULL," +
						VersionEntry.COLUMN_COMMON_ATTRIBUTES_KEY + " INTEGER," +

						VersionEntry.COLUMN_NAME + " TEXT NOT NULL," +

						"FOREIGN KEY (" + VersionEntry.COLUMN_BRAND_KEY + ") REFERENCES " +
						BrandEntry.TABLE_NAME + " (" + BrandEntry._ID + ")," +
						"FOREIGN KEY (" + VersionEntry.COLUMN_COMMON_ATTRIBUTES_KEY + ") REFERENCES " +
						CommonAttributesEntry.TABLE_NAME + " (" + CommonAttributesEntry._ID + ")," +
						" CHECK(" + VersionEntry.COLUMN_NAME + " <> '')" +
						")";

		final String SQL_INSERT_GENERAL_CATEGORY =
				"INSERT INTO " + CategoryEntry.TABLE_NAME +
						" (" + CategoryEntry.COLUMN_NAME + ") " +
						"VALUES (\"" + CategoryEntry.DEFAULT_CATEGORY_NAME + "\")";

		db.beginTransaction();
		try {

			db.execSQL(SQL_CREATE_CATEGORY_TABLE);
			db.execSQL(SQL_CREATE_COMMON_ATTRIBUTES_TABLE);
			db.execSQL(SQL_CREATE_ITEM_TABLE);
			db.execSQL(SQL_CREATE_BRAND_TABLE);
			db.execSQL(SQL_CREATE_BRAND_VERSION_TABLE);
			db.execSQL(SQL_INSERT_GENERAL_CATEGORY);

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		if (oldVersion == 2 && newVersion >= 3) {

			db.execSQL("DROP TABLE IF EXISTS " + CategoryEntry.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + ItemEntry.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.BrandEntry.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.VersionEntry.TABLE_NAME);
			oldVersion++;
		}
		onCreate(db);
	}
}
