package com.tomogoma.shoppinglistapp.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.tomogoma.shoppinglistapp.data.DatabaseContract.CategoryEntry;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.ItemEntry;

/**
 * Created by ogoma on 28/02/15.
 */
public class ShoppingListProvider extends ContentProvider {

	private static final UriMatcher sUriMatcher = buildUriMatcher();

	private static final int CATEGORY = 100;
	private static final int ITEM = 200;
	private static final int ITEM_IN_CATEGORY = 201;

	private DBHelper dbHelper;

	private static UriMatcher buildUriMatcher() {

		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		final String authority = DatabaseContract.CONTENT_AUTHORITY;

		matcher.addURI(authority, DatabaseContract.PATH_ITEM, ITEM);
		matcher.addURI(authority, DatabaseContract.PATH_ITEM + "/#", ITEM_IN_CATEGORY);

		matcher.addURI(authority, DatabaseContract.PATH_CATEGORY, CATEGORY);

		return matcher;
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

			case CATEGORY:
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

			case ITEM:
				retCursor = dbHelper.getReadableDatabase().query(
						ItemEntry.TABLE_NAME,
						projection,
						selection,
						selectionArgs,
						null,
						null,
						sortOrder
				);
				break;

			case ITEM_IN_CATEGORY:
				retCursor = getItemsInCategory(uri, projection, sortOrder);
				break;

			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}

		retCursor.setNotificationUri(getContext().getContentResolver(), uri);
		return retCursor;
	}

	@Override
	public String getType(Uri uri) {

		switch (sUriMatcher.match(uri)) {
			case CATEGORY:
				return DatabaseContract.CategoryEntry.CONTENT_TYPE;
			case ITEM:
			case ITEM_IN_CATEGORY:
				return DatabaseContract.ItemEntry.CONTENT_TYPE;
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {

		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		final int match = sUriMatcher.match(uri);
		Uri returnUri;

		switch (match) {

			case ITEM: {

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

			case ITEM: {

				db.beginTransaction();
				int returnCount = 0;

				try {
					for (ContentValues value : values) {
						long _id = db.insert(ItemEntry.TABLE_NAME, null, value);
						if (_id != -1) {
							returnCount++;
						}
					}
					db.setTransactionSuccessful();
				} finally {
					db.endTransaction();
				}

				getContext().getContentResolver().notifyChange(uri, null);
				return returnCount;
			}
			case CATEGORY: {

				db.beginTransaction();
				int returnCount = 0;

				try {
					for (ContentValues value : values) {
						long _id = db.insert(CategoryEntry.TABLE_NAME, null, value);
						if (_id != -1) {
							returnCount++;
						}
					}
					db.setTransactionSuccessful();
				} finally {
					db.endTransaction();
				}

				getContext().getContentResolver().notifyChange(uri, null);
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
				rowsUpdated = db.update(ItemEntry.TABLE_NAME, values, selection, selectionArgs);
				break;
			case CATEGORY:
				rowsUpdated = db.update(CategoryEntry.TABLE_NAME, values, selection, selectionArgs);
				break;
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}

		if (selection == null || rowsUpdated != 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}

		return rowsUpdated;
	}

	private Cursor getItemsInCategory(Uri uri, String[] projection, String sortOrder) {

		String categoryID = ItemEntry.getCategoryIDFromUri(uri);
		String categorySelection = ItemEntry.COLUMN_CAT_KEY + "=?";

		return dbHelper.getReadableDatabase().query(
				ItemEntry.TABLE_NAME,
				projection,
				categorySelection,
				new String[]{categoryID},
				null,
				null,
				sortOrder
		);
	}

}
