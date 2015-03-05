package com.tomogoma.shoppinglistapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.tomogoma.shoppinglistapp.data.DatabaseContract.BrandEntry;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.CategoryEntry;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.CommonAttributesEntry;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.ItemEntry;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.VersionEntry;

/**
 * Created by ogoma on 28/02/15.
 */
public class ShoppingListProvider extends ContentProvider {

	private static final UriMatcher sUriMatcher = buildUriMatcher();
	private static final int CATEGORY = 100;
	private static final int ITEM = 200;
	private static final int ITEM_IN_CATEGORY = 201;
	private static final int BRAND = 300;
	private static final int BRAND_IN_ITEM = 302;
	private static final int VERSION = 400;
	private static final int VERSION_IN_BRAND = 403;
	private static final int COMMON_ATTRIBUTES = 5432;
	private DBHelper dbHelper;

	private static final String ITEM_SELECTION_TABLE =
			ItemEntry.TABLE_NAME + " LEFT JOIN " + CommonAttributesEntry.TABLE_NAME +
					" ON " +
					ItemEntry.TABLE_NAME + "." + ItemEntry.COLUMN_COMMON_ATTRIBUTES_KEY +
					" = " +
					CommonAttributesEntry.TABLE_NAME + "." + CommonAttributesEntry._ID;

	private static UriMatcher buildUriMatcher() {

		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		final String authority = DatabaseContract.CONTENT_AUTHORITY;

		matcher.addURI(authority, DatabaseContract.PATH_VERSION, VERSION);
		matcher.addURI(authority, DatabaseContract.PATH_VERSION + "/#", VERSION_IN_BRAND);

		matcher.addURI(authority, DatabaseContract.PATH_BRAND, BRAND);
		matcher.addURI(authority, DatabaseContract.PATH_BRAND + "/#", BRAND_IN_ITEM);

		matcher.addURI(authority, DatabaseContract.PATH_ITEM, ITEM);
		matcher.addURI(authority, DatabaseContract.PATH_ITEM + "/#", ITEM_IN_CATEGORY);

		matcher.addURI(authority, DatabaseContract.PATH_CATEGORY, CATEGORY);

		matcher.addURI(authority, DatabaseContract.PATH_COMMON_ATTRIBUTES, COMMON_ATTRIBUTES);

		return matcher;
	}

	private Uri insertItem(SQLiteDatabase db, Uri uri, ContentValues values) {

		ContentValues commonValues = extractCommonAttributes(values);
		ContentValues itemValues = extractItemValues(values);

		db.beginTransaction();
		try {

			long commonID = db.insert(CommonAttributesEntry.TABLE_NAME, null, commonValues);
			if (commonID <= 0)
				throw new android.database.SQLException("Failed to insert row into " + uri);

			itemValues.put(ItemEntry.COLUMN_COMMON_ATTRIBUTES_KEY, commonID);
			long _id = db.insert(ItemEntry.TABLE_NAME, null, itemValues);
			if (_id <= 0) {
				throw new android.database.SQLException("Failed to insert row into " + uri);
			}

			db.setTransactionSuccessful();
			return ItemEntry.buildItemUri(_id);
		} finally {
			db.endTransaction();
		}
	}

	private int updateItem(SQLiteDatabase db, ContentValues values, String selection, String[] selectionArgs) {

		ContentValues commonValues = extractCommonAttributes(values);
		ContentValues itemValues = extractItemValues(values);
		String commonAttrSelection = CommonAttributesEntry._ID + " = ?";
		String itemHaving = ItemEntry.COLUMN_COMMON_ATTRIBUTES_KEY + " != \"\"";

		int rowsAffected;
		db.beginTransaction();
		try {
			//  get common attribute IDs from affected Items (where common attribute IDs exist)
			Cursor cursor = db.query(ItemEntry.TABLE_NAME,
			                         new String[]{ItemEntry.COLUMN_COMMON_ATTRIBUTES_KEY},
			                         selection, selectionArgs, null, itemHaving, null);
			//  for all items, update corresponding attributes
			if (cursor.moveToFirst()) {
				do {
					String commonAttrID = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_COMMON_ATTRIBUTES_KEY));
					db.update(CommonAttributesEntry.TABLE_NAME, commonValues, commonAttrSelection, new String[]{commonAttrID});
				} while (cursor.moveToNext());
			}
			cursor.close();
			rowsAffected = db.update(ItemEntry.TABLE_NAME, itemValues, selection, selectionArgs);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		return rowsAffected;
	}

	private ContentValues extractCommonAttributes(ContentValues values) {

		ContentValues commonValues = new ContentValues();

		commonValues.put(CommonAttributesEntry._ID,
		                 values.getAsLong(CommonAttributesEntry._ID));
		commonValues.put(CommonAttributesEntry.COLUMN_PRICE,
		                 values.getAsDouble(CommonAttributesEntry.COLUMN_PRICE));
		commonValues.put(CommonAttributesEntry.COLUMN_QTTY,
		                 values.getAsFloat(CommonAttributesEntry.COLUMN_QTTY));
		commonValues.put(CommonAttributesEntry.COLUMN_DESC,
		                 values.getAsString(CommonAttributesEntry.COLUMN_DESC));
		commonValues.put(CommonAttributesEntry.COLUMN_MEAS_UNIT,
		                 values.getAsString(CommonAttributesEntry.COLUMN_MEAS_UNIT));
		commonValues.put(CommonAttributesEntry.COLUMN_USEFUL_UNIT,
		                 values.getAsString(CommonAttributesEntry.COLUMN_USEFUL_UNIT));
		commonValues.put(CommonAttributesEntry.COLUMN_USEFUL_PER_MEAS,
		                 values.getAsFloat(CommonAttributesEntry.COLUMN_USEFUL_PER_MEAS));
		Log.d(getClass().getSimpleName(), "UsefulPerMeas: " + values.getAsFloat(CommonAttributesEntry.COLUMN_USEFUL_PER_MEAS));
		commonValues.put(CommonAttributesEntry.COLUMN_IN_LIST,
		                 values.getAsInteger(CommonAttributesEntry.COLUMN_IN_LIST));
		commonValues.put(CommonAttributesEntry.COLUMN_IN_CART,
		                 values.getAsInteger(CommonAttributesEntry.COLUMN_IN_CART));

		return commonValues;
	}

	private ContentValues extractItemValues(ContentValues values) {

		ContentValues itemValues = new ContentValues();

		itemValues.put(ItemEntry._ID, values.getAsLong(ItemEntry._ID));
		itemValues.put(ItemEntry.COLUMN_COMMON_ATTRIBUTES_KEY, values.getAsLong(ItemEntry.COLUMN_COMMON_ATTRIBUTES_KEY));
		itemValues.put(ItemEntry.COLUMN_CAT_KEY, values.getAsLong(ItemEntry.COLUMN_CAT_KEY));
		itemValues.put(ItemEntry.COLUMN_NAME, values.getAsString(ItemEntry.COLUMN_NAME));

		return itemValues;
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
						ITEM_SELECTION_TABLE,
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

			case BRAND:
				retCursor = dbHelper.getReadableDatabase().query(
						BrandEntry.TABLE_NAME,
						projection,
						selection,
						selectionArgs,
						null,
						null,
						sortOrder
				);
				break;

			case BRAND_IN_ITEM:
				retCursor = getBrandsInItem(uri, projection, sortOrder);
				break;

			case VERSION:
				retCursor = dbHelper.getReadableDatabase().query(
						VersionEntry.TABLE_NAME,
						projection,
						selection,
						selectionArgs,
						null,
						null,
						sortOrder
				);
				break;

			case VERSION_IN_BRAND:
				retCursor = getVersionsInBrand(uri, projection, sortOrder);
				break;

			case COMMON_ATTRIBUTES:
				retCursor = dbHelper.getReadableDatabase().query(
						CommonAttributesEntry.TABLE_NAME,
						projection,
						selection,
						selectionArgs,
						null,
						null,
						sortOrder
				);
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
			case BRAND:
			case BRAND_IN_ITEM:
				return DatabaseContract.BrandEntry.CONTENT_TYPE;
			case VERSION:
			case VERSION_IN_BRAND:
				return DatabaseContract.VersionEntry.CONTENT_TYPE;
			case COMMON_ATTRIBUTES:
				return CommonAttributesEntry.CONTENT_ITEM_TYPE;
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
			case VERSION: {
				long _id = db.insert(VersionEntry.TABLE_NAME, null, values);
				if (_id > 0)
					returnUri = VersionEntry.buildVersionUri(_id);
				else
					throw new android.database.SQLException("Failed to insert row into " + uri);
				break;
			}
			case BRAND: {
				long _id = db.insert(BrandEntry.TABLE_NAME, null, values);
				if (_id > 0)
					returnUri = BrandEntry.buildBrandUri(_id);
				else
					throw new android.database.SQLException("Failed to insert row into " + uri);
				break;
			}
			case ITEM: {
				returnUri = insertItem(db, uri, values);
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
			case COMMON_ATTRIBUTES: {
				long _id = db.insert(CommonAttributesEntry.TABLE_NAME, null, values);
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
			case VERSION: {

				db.beginTransaction();
				int returnCount = 0;

				try {
					for (ContentValues value : values) {
						long _id = db.insert(VersionEntry.TABLE_NAME, null, value);
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
			case BRAND: {

				db.beginTransaction();
				int returnCount = 0;

				try {
					for (ContentValues value : values) {
						long _id = db.insert(BrandEntry.TABLE_NAME, null, value);
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
			case ITEM: {

				db.beginTransaction();
				int returnCount = 0;

				try {
					for (ContentValues value : values) {
						long _id = ContentUris.parseId(insertItem(db, uri, value));
//						long _id = db.insert(ItemEntry.TABLE_NAME, null, value);
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
			case VERSION:
				rowsDeleted = db.delete(VersionEntry.TABLE_NAME, selection, selectionArgs);
				break;
			case BRAND:
				rowsDeleted = db.delete(BrandEntry.TABLE_NAME, selection, selectionArgs);
				break;
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
			case VERSION:
				rowsUpdated = db.update(VersionEntry.TABLE_NAME, values, selection, selectionArgs);
				break;
			case BRAND:
				rowsUpdated = db.update(BrandEntry.TABLE_NAME, values, selection, selectionArgs);
				break;
			case ITEM:
				rowsUpdated = updateItem(db, values, selection, selectionArgs);
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
				ITEM_SELECTION_TABLE,
				projection,
				categorySelection,
				new String[]{categoryID},
				null,
				null,
				sortOrder
		);
	}

	private Cursor getBrandsInItem(Uri uri, String[] projection, String sortOrder) {

		String itemID = BrandEntry.getItemIDFromUri(uri);
		String itemSelection = BrandEntry.COLUMN_ITEM_KEY + "=?";

		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		if (BrandEntry.isLoadVersions(uri)) {
			queryBuilder.setTables(
					BrandEntry.TABLE_NAME + " LEFT JOIN " + VersionEntry.TABLE_NAME +
							" ON " +
							BrandEntry.TABLE_NAME + "." + BrandEntry._ID +
							"=" +
							VersionEntry.TABLE_NAME + "." + VersionEntry.COLUMN_BRAND_KEY
			);
		} else {
			queryBuilder.setTables(BrandEntry.TABLE_NAME);
		}

		return queryBuilder.query(
				dbHelper.getReadableDatabase(),
				projection,
				itemSelection,
				new String[]{itemID},
				null,
				null,
				sortOrder
		);
	}

	private Cursor getVersionsInBrand(Uri uri, String[] projection, String sortOrder) {

		String brandName = DatabaseContract.VersionEntry.getBrandIDFromUri(uri);
		String brandSelection = VersionEntry.COLUMN_BRAND_KEY + "=?";

		return dbHelper.getReadableDatabase().query(
				VersionEntry.TABLE_NAME,
				projection,
				brandSelection,
				new String[]{brandName},
				null,
				null,
				sortOrder
		);
	}

}
