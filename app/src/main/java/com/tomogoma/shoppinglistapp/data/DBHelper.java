package com.tomogoma.shoppinglistapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.tomogoma.shoppinglistapp.data.DatabaseContract.CategoryEntry;
import static com.tomogoma.shoppinglistapp.data.DatabaseContract.ItemEntry;

/**
 * Created by ogoma on 27/02/15.
 */
public class DBHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 4;
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

		//  Duplicates of Item names not allowed (not even when under different categories)
		//  Duplicates name assumed as updates for the rest of the columns of the existing!
		final String SQL_CREATE_ITEM_TABLE =
				"CREATE TABLE " + ItemEntry.TABLE_NAME + " (" +

						ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
						ItemEntry.COLUMN_CAT_KEY + " INTEGER NOT NULL," +
						ItemEntry.COLUMN_COMMON_ATTRIBUTES_KEY + " INTEGER," +

						ItemEntry.COLUMN_NAME + " TEXT NOT NULL," +

						ItemEntry.COLUMN_PRICE + " REAL," +
						ItemEntry.COLUMN_QTTY + " REAL," +
						ItemEntry.COLUMN_DESC + " TEXT," +
						ItemEntry.COLUMN_MEAS_UNIT + " TEXT," +
						ItemEntry.COLUMN_USEFUL_UNIT + " TEXT," +
						ItemEntry.COLUMN_USEFUL_PER_MEAS + " REAL," +
						ItemEntry.COLUMN_IN_LIST + " INTEGER," +
						ItemEntry.COLUMN_IN_CART + " INTEGER," +

						"FOREIGN KEY (" + ItemEntry.COLUMN_CAT_KEY + ") REFERENCES " +
						CategoryEntry.TABLE_NAME + " (" + CategoryEntry._ID + ")," +
						"UNIQUE (" + ItemEntry.COLUMN_NAME + ") ON CONFLICT REPLACE," +
						" CHECK(" + ItemEntry.COLUMN_NAME + " <> '')" +
						")";

		final String SQL_INSERT_GENERAL_CATEGORY =
				"INSERT INTO " + CategoryEntry.TABLE_NAME +
						" (" + CategoryEntry.COLUMN_NAME + ") " +
						"VALUES (\"" + CategoryEntry.DEFAULT_CATEGORY_NAME + "\")";

		db.beginTransaction();
		try {

			db.execSQL(SQL_CREATE_CATEGORY_TABLE);
			db.execSQL(SQL_CREATE_ITEM_TABLE);
			db.execSQL(SQL_INSERT_GENERAL_CATEGORY);

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		if (oldVersion == 3 && newVersion >= 4) {

			db.execSQL("DROP TABLE IF EXISTS " + CategoryEntry.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + ItemEntry.TABLE_NAME);
			oldVersion++;
		}
		onCreate(db);
	}
}
