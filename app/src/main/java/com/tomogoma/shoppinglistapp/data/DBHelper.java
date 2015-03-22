package com.tomogoma.shoppinglistapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tomogoma.shoppinglistapp.data.DatabaseContract.CurrencyEntry;

import static com.tomogoma.shoppinglistapp.data.DatabaseContract.CategoryEntry;
import static com.tomogoma.shoppinglistapp.data.DatabaseContract.ItemEntry;

/**
 * Created by ogoma on 27/02/15.
 */
public class DBHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 9;
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

		//  Duplicates of currency codes not allowed
		final String SQL_CREATE_CURRENCY_TABLE =
				"CREATE TABLE " + CurrencyEntry.TABLE_NAME + " (" +

						CurrencyEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
						CurrencyEntry.COLUMN_COUNTRY + " TEXT NOT NULL," +
						CurrencyEntry.COLUMN_CODE + " TEXT NOT NULL," +
						CurrencyEntry.COLUMN_NAME + " TEXT NOT NULL," +
						CurrencyEntry.COLUMN_SYMBOL + " TEXT," +
						CurrencyEntry.COLUMN_LAST_CONVERSION + " REAL," +

						"UNIQUE (" + CurrencyEntry.COLUMN_COUNTRY + ") ON CONFLICT FAIL," +
						" CHECK(" + CurrencyEntry.COLUMN_CODE + " <> '')," +
						" CHECK(" + CurrencyEntry.COLUMN_NAME + " <> '')," +
						" CHECK(" + CurrencyEntry.COLUMN_COUNTRY + " <> '')" +
						")";

		//  Duplicates of Item names not allowed (not even when under different categories)
		//  Duplicates name assumed as updates for the rest of the columns of the existing!
		final String SQL_CREATE_ITEM_TABLE =
				"CREATE TABLE " + ItemEntry.TABLE_NAME + " (" +

						ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
						ItemEntry.COLUMN_CAT_KEY + " INTEGER NOT NULL," +
						ItemEntry.COLUMN_CURRENCY_KEY + " INTEGER NOT NULL," +

						ItemEntry.COLUMN_NAME + " TEXT NOT NULL," +

						ItemEntry.COLUMN_PRICE + " REAL," +
						ItemEntry.COLUMN_QUANTITY + " REAL," +
						ItemEntry.COLUMN_DESC + " TEXT," +
						ItemEntry.COLUMN_MEAS_UNIT + " TEXT," +
						ItemEntry.COLUMN_LASTS_FOR + " REAL," +
						ItemEntry.COLUMN_LASTS_FOR_UNIT + " TEXT," +
						ItemEntry.COLUMN_IN_LIST + " INTEGER," +
						ItemEntry.COLUMN_IN_CART + " INTEGER," +

						"FOREIGN KEY (" + ItemEntry.COLUMN_CAT_KEY + ") REFERENCES " +
						CategoryEntry.TABLE_NAME + " (" + CategoryEntry._ID + ")," +
						"FOREIGN KEY (" + ItemEntry.COLUMN_CURRENCY_KEY + ") REFERENCES " +
						CurrencyEntry.TABLE_NAME + " (" + CurrencyEntry._ID + ")," +
						"UNIQUE (" + ItemEntry.COLUMN_NAME + ") ON CONFLICT FAIL," +
						" CHECK(" + ItemEntry.COLUMN_NAME + " <> '')" +
						")";

		final String SQL_INSERT_GENERAL_CATEGORY =
				"INSERT INTO " + CategoryEntry.TABLE_NAME +
						" (" + CategoryEntry.COLUMN_NAME + ") " +
						"VALUES (\"" + CategoryEntry.DEFAULT_NAME + "\")";

		final String SQL_INSERT_DEFAULT_CURRENCY =
				"INSERT INTO " + CurrencyEntry.TABLE_NAME +
						" (" +
							CurrencyEntry.COLUMN_CODE + ", " +
							CurrencyEntry.COLUMN_SYMBOL + ", " +
							CurrencyEntry.COLUMN_NAME + ", " +
							CurrencyEntry.COLUMN_COUNTRY +
						") " +
						"VALUES (" +
							"\"" + CurrencyEntry.DEFAULT_CODE + "\"," +
							"\"" + CurrencyEntry.DEFAULT_SYMBOL + "\"," +
							"\"" + CurrencyEntry.DEFAULT_NAME + "\"," +
							"\"" + CurrencyEntry.DEFAULT_COUNTRY + "\"" +
						")";

		db.beginTransaction();
		try {

			db.execSQL(SQL_CREATE_CATEGORY_TABLE);
			db.execSQL(SQL_CREATE_CURRENCY_TABLE);
			db.execSQL(SQL_CREATE_ITEM_TABLE);

			db.execSQL(SQL_INSERT_GENERAL_CATEGORY);
			db.execSQL(SQL_INSERT_DEFAULT_CURRENCY);

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		db.execSQL("DROP TABLE IF EXISTS " + CategoryEntry.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + CurrencyEntry.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + ItemEntry.TABLE_NAME);
		onCreate(db);
	}
}
