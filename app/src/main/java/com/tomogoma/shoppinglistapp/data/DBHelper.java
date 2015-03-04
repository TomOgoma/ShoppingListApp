package com.tomogoma.shoppinglistapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.tomogoma.shoppinglistapp.data.DatabaseContract.CategoryEntry;
import static com.tomogoma.shoppinglistapp.data.DatabaseContract.ItemEntry;

/**
 * Created by ogoma on 27/02/15.
 */
public class DBHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 2;
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

						"UNIQUE (" + CategoryEntry.COLUMN_NAME + ") ON CONFLICT REPLACE" +
						")";

		//  Duplicates of Item names not allowed (not even when under different categories)
		//  Duplicates name assumed as updates for the rest of the columns of the existing!
		final String SQL_CREATE_ITEM_TABLE =
				"CREATE TABLE " + ItemEntry.TABLE_NAME + " (" +

						ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
						ItemEntry.COLUMN_CAT_KEY + " INTEGER NOT NULL," +

						ItemEntry.COLUMN_NAME + " TEXT NOT NULL," +
						ItemEntry.COLUMN_PRICE + " REAL," +
						ItemEntry.COLUMN_QTTY + " REAL," +
						ItemEntry.COLUMN_MEAS_UNIT + " TEXT," +
						ItemEntry.COLUMN_USEFUL_UNIT + " TEXT," +
						ItemEntry.COLUMN_USEFUL_PER_MEAS + " REAL," +
						ItemEntry.COLUMN_DESC + " TEXT," +

						"FOREIGN KEY (" + ItemEntry.COLUMN_CAT_KEY + ") REFERENCES "
						+ CategoryEntry.TABLE_NAME + " (" + CategoryEntry._ID + ")" +
						"UNIQUE (" + ItemEntry.COLUMN_NAME + ") ON CONFLICT REPLACE" +
						")";

		//  Duplicate brand names acceptable - useful for different Items that share a brand
		final String SQL_CREATE_BRAND_TABLE =
				"CREATE TABLE " + DatabaseContract.BrandEntry.TABLE_NAME + " (" +

						DatabaseContract.BrandEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
						DatabaseContract.BrandEntry.COLUMN_ITEM_KEY + " INTEGER NOT NULL," +

						DatabaseContract.BrandEntry.COLUMN_NAME + " TEXT NOT NULL," +
						DatabaseContract.BrandEntry.COLUMN_PRICE + " REAL," +
						DatabaseContract.BrandEntry.COLUMN_QTTY + " REAL," +
						DatabaseContract.BrandEntry.COLUMN_DESC + " TEXT," +

						"FOREIGN KEY (" + DatabaseContract.BrandEntry.COLUMN_ITEM_KEY + ") REFERENCES "
						+ ItemEntry.TABLE_NAME + " (" + ItemEntry._ID + ")" +
						")";

		//  Duplicate version names acceptable - useful for different brands that share a version
		final String SQL_CREATE_BRAND_VERSION_TABLE =
				"CREATE TABLE " + DatabaseContract.VersionEntry.TABLE_NAME + " (" +

						CategoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
						DatabaseContract.VersionEntry.COLUMN_BRAND_KEY + " INTEGER NOT NULL," +

						DatabaseContract.VersionEntry.COLUMN_NAME + " TEXT NOT NULL," +
						DatabaseContract.VersionEntry.COLUMN_PRICE + " REAL," +
						DatabaseContract.VersionEntry.COLUMN_QTTY + " REAL," +
						DatabaseContract.VersionEntry.COLUMN_DESC + " TEXT," +

						"FOREIGN KEY (" + DatabaseContract.VersionEntry.COLUMN_BRAND_KEY + ") REFERENCES "
						+ DatabaseContract.BrandEntry.TABLE_NAME + " (" + DatabaseContract.BrandEntry._ID + ")" +
						")";

		db.execSQL(SQL_CREATE_CATEGORY_TABLE);
		db.execSQL(SQL_CREATE_ITEM_TABLE);
		db.execSQL(SQL_CREATE_BRAND_TABLE);
		db.execSQL(SQL_CREATE_BRAND_VERSION_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		Log.d(getClass().getSimpleName(), "on upgrade called");

		if (oldVersion == 1 && newVersion >= 2) {

			Log.d(getClass().getSimpleName(), "drop category table");
			db.execSQL("DROP TABLE IF EXISTS " + CategoryEntry.TABLE_NAME);
			Log.d(getClass().getSimpleName(), "drop item table");
			db.execSQL("DROP TABLE IF EXISTS " + ItemEntry.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.BrandEntry.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.VersionEntry.TABLE_NAME);
			onCreate(db);
			oldVersion++;
		}
	}
}
