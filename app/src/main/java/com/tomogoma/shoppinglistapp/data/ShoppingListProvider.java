package com.tomogoma.shoppinglistapp.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.tomogoma.shoppinglistapp.data.DatabaseContract.CategoryEntry;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.CurrencyEntry;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.ItemEntry;
import com.tomogoma.shoppinglistapp.util.Preference;

import java.util.Arrays;

/**
 * Created by ogoma on 28/02/15.
 */
public class ShoppingListProvider extends ContentProvider {

	private static final String LOG_TAG = ShoppingListProvider.class.getSimpleName();
	private static final UriMatcher sUriMatcher = buildUriMatcher();

	private static final int CATEGORY = 100;
	private static final int CURRENCY = 200;
	private static final int CURRENCY_ID = 201;
	private static final int ITEM = 300;
	private static final int ITEM_ID = 301;

	private DBHelper dbHelper;

	private static UriMatcher buildUriMatcher() {

		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		final String authority = DatabaseContract.CONTENT_AUTHORITY;

		matcher.addURI(authority, DatabaseContract.PATH_ITEM, ITEM);
		matcher.addURI(authority, DatabaseContract.PATH_ITEM + "/#", ITEM_ID);

		matcher.addURI(authority, DatabaseContract.PATH_CATEGORY, CATEGORY);

		matcher.addURI(authority, DatabaseContract.PATH_CURRENCY, CURRENCY);
		matcher.addURI(authority, DatabaseContract.PATH_CURRENCY + "/#", CURRENCY_ID);

		return matcher;
	}

	private static final SQLiteQueryBuilder sITEM_CURRENCY_TABLES;

	static {
		sITEM_CURRENCY_TABLES = new SQLiteQueryBuilder();
		sITEM_CURRENCY_TABLES.setTables(
				ItemEntry.TABLE_NAME + " INNER JOIN "  + CurrencyEntry.TABLE_NAME +
						" ON " +
						ItemEntry.TABLE_NAME + "." + ItemEntry.COLUMN_CURRENCY_KEY +
						" = " +
						CurrencyEntry.TABLE_NAME  + "." + CurrencyEntry._ID
		);
	}

	@Override
	public String getType(Uri uri) {

		switch (sUriMatcher.match(uri)) {
			case CATEGORY:
				return CategoryEntry.CONTENT_TYPE;
			case CURRENCY:
				return CurrencyEntry.CONTENT_TYPE;
			case CURRENCY_ID:
				return CurrencyEntry.CONTENT_ITEM_TYPE;
			case ITEM:
				return ItemEntry.CONTENT_TYPE;
			case ITEM_ID:
				return ItemEntry.CONTENT_ITEM_TYPE;
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}

	@Override
	public boolean onCreate() {
		dbHelper = new DBHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

		Cursor retCursor;
		switch (sUriMatcher.match(uri)) {

			case CATEGORY: {
				retCursor = dbHelper.getReadableDatabase().query(
						CategoryEntry.TABLE_NAME,
						projection,
						selection,
						selectionArgs,
						null,
						null,
						sortOrder
				);
				break;
			}
			case ITEM: {
				retCursor = getItems(uri, projection, selection, selectionArgs, sortOrder);
				break;
			}
			case ITEM_ID: {
				retCursor = getItem(uri, projection, selection, selectionArgs, sortOrder);
				break;
			}
			case CURRENCY: {
					retCursor = dbHelper.getReadableDatabase()
					                    .query(CurrencyEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
				break;
			}
			case CURRENCY_ID: {
				String idSelection = CurrencyEntry._ID + " = ?";
				if (selection == null || selection.isEmpty()) {
					selection = idSelection;
					selectionArgs = new String[]{CurrencyEntry.getCurrencyIDFromUri(uri)};
				} else {
					selection += " AND " + idSelection;
					int originalArgsLength = selectionArgs.length;
					selectionArgs = Arrays.copyOf(selectionArgs, originalArgsLength + 1);
					selectionArgs[originalArgsLength] = CurrencyEntry.getCurrencyIDFromUri(uri);
				}
				retCursor = dbHelper.getReadableDatabase().query(
						CurrencyEntry.TABLE_NAME,
						projection,
						selection,
						selectionArgs,
						null,
						null,
						sortOrder
				);
				break;
			}
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}

		retCursor.setNotificationUri(getContext().getContentResolver(), uri);
		return retCursor;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {

		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		final int match = sUriMatcher.match(uri);
		Uri returnUri;

		switch (match) {

			case ITEM: {

				addCurrencyIDToItemValues(values);
				long _id = db.insert(ItemEntry.TABLE_NAME, null, values);
				if (_id > 0)
					returnUri = ItemEntry.buildItemUri(_id);
				else
					throw new android.database.SQLException("Failed to insert row into " + uri);
				break;
			}
			case CATEGORY: {

				long _id = db.insert(CategoryEntry.TABLE_NAME, null, values);
				if (_id > 0)
					returnUri = CategoryEntry.buildCategoryUri(_id);
				else
					throw new android.database.SQLException("Failed to insert row into " + uri);
				break;
			}
			case CURRENCY: {
				try {
					returnUri = insertCurrency(db, values);
					getContext().getContentResolver().notifyChange(DatabaseContract.BASE_CONTENT_URI, null);
				} catch (SQLException e) {
					throw new SQLException(e.getMessage() + uri);
				}
				break;
			}
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return returnUri;
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {

		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		final int match = sUriMatcher.match(uri);

		switch (match) {
			case CURRENCY: {

				db.beginTransaction();
				int returnCount = 0;

				try {
					for (ContentValues value : values) {

						try {
							Uri insertUri = insertCurrency(db, value);
							long insertID = Long.parseLong(CurrencyEntry.getCurrencyIDFromUri(insertUri));
							if (insertID > 0) {
								returnCount ++;
							}
						} catch(SQLException e) {
							Log.e(LOG_TAG, e.getMessage());
						}
					}
					db.setTransactionSuccessful();
				} finally {
					db.endTransaction();
				}

				getContext().getContentResolver().notifyChange(DatabaseContract.BASE_CONTENT_URI, null);
				return returnCount;
			}
			default:
				return super.bulkInsert(uri, values);
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {

		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		final int match = sUriMatcher.match(uri);
		int rowsDeleted;

		switch (match) {

			case ITEM:
				rowsDeleted = db.delete(ItemEntry.TABLE_NAME, selection, selectionArgs);
				break;
			case CATEGORY:
				rowsDeleted = db.delete(CategoryEntry.TABLE_NAME, selection, selectionArgs);
				break;
			case CURRENCY:
				rowsDeleted = db.delete(CurrencyEntry.TABLE_NAME, selection, selectionArgs);
				getContext().getContentResolver().notifyChange(DatabaseContract.BASE_CONTENT_URI, null);
				break;
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}

		if (selection == null || rowsDeleted != 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}

		return rowsDeleted;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		final int match = sUriMatcher.match(uri);
		int rowsUpdated;

		switch (match) {

			case ITEM:
				addCurrencyIDToItemValues(values);
				rowsUpdated = db.update(ItemEntry.TABLE_NAME, values, selection, selectionArgs);
				break;
			case CATEGORY:
				rowsUpdated = db.update(CategoryEntry.TABLE_NAME, values, selection, selectionArgs);
				break;
			case CURRENCY:
				rowsUpdated = db.update(CurrencyEntry.TABLE_NAME, values, selection, selectionArgs);
				getContext().getContentResolver().notifyChange(DatabaseContract.BASE_CONTENT_URI, null);
				break;
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}

		if (selection == null || rowsUpdated != 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}

		return rowsUpdated;
	}

	private Cursor getItems(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

		SQLiteQueryBuilder queryBuilder;

		if (ItemEntry.isToInnerJoinCurrency(uri)) {
			queryBuilder =sITEM_CURRENCY_TABLES;
		} else {
			queryBuilder = new SQLiteQueryBuilder();
			queryBuilder.setTables(ItemEntry.TABLE_NAME);
		}

		queryBuilder.setDistinct(ItemEntry.isToSelectDistinct(uri));

		String categoryID = ItemEntry.getCategoryIDFromUri(uri);
		if (categoryID == null || categoryID.isEmpty()) {

			return queryBuilder.query(
					dbHelper.getReadableDatabase(),
					projection, selection,
					selectionArgs,
					null, null,
					sortOrder
				);
		}
		else {
			return getItemsInCategory(queryBuilder, projection, selection, selectionArgs, sortOrder, categoryID);
		}
	}

	private Cursor getItem(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

		String idSelection = ItemEntry._ID + " = ?";

		if (selection == null || selection.isEmpty()) {

			selection = idSelection;
			selectionArgs = new String[]{ItemEntry.getItemIDFromUri(uri)};
		}
		else {

			selection += " AND " + idSelection;
			int originalArgsLength = selectionArgs.length;
			selectionArgs = Arrays.copyOf(selectionArgs, originalArgsLength + 1);
			selectionArgs[originalArgsLength] = ItemEntry.getItemIDFromUri(uri);
		}

		return dbHelper.getReadableDatabase().query(
				ItemEntry.TABLE_NAME,
				projection,
				selection,
				selectionArgs,
				null,
				null,
				sortOrder
			);
	}

	private void addCurrencyIDToItemValues(ContentValues values) {

		if (!values.containsKey(ItemEntry.COLUMN_CURRENCY_KEY)) {
			long preferredCurrencyID = Preference.getPreferredCurrencyID(getContext());
			values.put(ItemEntry.COLUMN_CURRENCY_KEY, preferredCurrencyID);
		}
	}

	private Uri insertCurrency(SQLiteDatabase db, ContentValues values) {

		long _id;
		Cursor cursor = null;

		try {

			cursor = db.query(
					CurrencyEntry.TABLE_NAME,
					new String[]{CurrencyEntry._ID},
					CurrencyEntry.COLUMN_COUNTRY + " =? ",
					new String[]{values.getAsString(CurrencyEntry.COLUMN_COUNTRY)},
					null,
					null,
					null
			);

			if (cursor.moveToFirst()) {

				int columnIndex = cursor.getColumnIndex(CurrencyEntry._ID);
				_id = cursor.getLong(columnIndex);
				int count = db.update(CurrencyEntry.TABLE_NAME, values, CurrencyEntry._ID + " =?",
				                      new String[]{String.valueOf(_id)});
				if (count != 1) {
					throw new SQLException("Updated the wrong row(s): count = " + count);
				}
			} else {
				_id = db.insert(CurrencyEntry.TABLE_NAME, null, values);
			}
		} finally {
			if (cursor!=null)
				cursor.close();
		}

		if (_id > 0)
			return CurrencyEntry.buildCurrencyUri(_id);
		else
			throw new SQLException("Failed to insert row into ");
	}

	private Cursor getItemsInCategory(SQLiteQueryBuilder queryBuilder, String[] projection,String selection, String[] selectionArgs,
	                                  String sortOrder, String categoryID) {

		String categorySelection = ItemEntry.COLUMN_CAT_KEY + " =? ";

		if (selection == null || selection.isEmpty()) {

			selection = categorySelection;
			selectionArgs = new String[]{categoryID};
		}
		else {

			selection += " AND " +categorySelection;
			int originalArgsLength = selectionArgs.length;
			selectionArgs = Arrays.copyOf(selectionArgs, originalArgsLength + 1);
			selectionArgs[originalArgsLength] = String.valueOf(categoryID);
		}

		return queryBuilder.query(
				dbHelper.getReadableDatabase(),
				projection,
				selection,
				selectionArgs,
				null,
				null,
				sortOrder);
	}

}
