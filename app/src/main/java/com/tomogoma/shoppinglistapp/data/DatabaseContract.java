package com.tomogoma.shoppinglistapp.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by ogoma on 27/02/15.
 */
public class DatabaseContract {

	public static final String CONTENT_AUTHORITY = "com.tomogoma.shoppinglistapp";

	public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

	public static final String PATH_CATEGORY = "category";
	public static final String PATH_ITEM = "item";

	public static final class CategoryEntry implements BaseColumns {

		public static final Uri CONTENT_URI =
				BASE_CONTENT_URI.buildUpon().appendPath(PATH_CATEGORY).build();
		public static final String CONTENT_TYPE =
				"vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_CATEGORY;
		public static final String CONTENT_ITEM_TYPE =
				"vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_CATEGORY;

		public static final String DEFAULT_CATEGORY_NAME = "General";
		public static final Long DEFAULT_CATEGORY_ID = 1L;

		public static final String TABLE_NAME = "categories";
		public static final String COLUMN_NAME = "name";

		public static Uri buildCategoryUri(long id) {
			return ContentUris.withAppendedId(CONTENT_URI, id);
		}
	}

	public static final class ItemEntry implements BaseColumns {

		public static final Uri CONTENT_URI =
				BASE_CONTENT_URI.buildUpon().appendPath(PATH_ITEM).build();
		public static final String CONTENT_TYPE =
				"vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_ITEM;
		public static final String CONTENT_ITEM_TYPE =
				"vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_ITEM;

		public static final String TABLE_NAME = "items";

		public static final String COLUMN_CAT_KEY = "category_id";
		public static final String COLUMN_COMMON_ATTRIBUTES_KEY = "common_attributes_id";
		public static final String COLUMN_NAME = "name";

		public static final String COLUMN_PRICE = "price";
		public static final String COLUMN_QTTY = "quantity";
		public static final String COLUMN_DESC = "description";
		public static final String COLUMN_MEAS_UNIT = "meas_unit";
		public static final String COLUMN_USEFUL_UNIT = "useful_unit";
		public static final String COLUMN_USEFUL_PER_MEAS = "useful_per_meas";
		public static final String COLUMN_IN_LIST = "in_list";
		public static final String COLUMN_IN_CART = "in_cart";

		public static Uri buildItemUri(long id) {
			return ContentUris.withAppendedId(CONTENT_URI, id);
		}

		public static Uri buildItemsInCategoryUri(long categoryID) {
			return CONTENT_URI.buildUpon()
					.appendQueryParameter(PATH_CATEGORY, String.valueOf(categoryID))
			        .build();
		}

		public static String getItemIDFromUri(Uri uri) {
			return uri.getPathSegments().get(1);
		}

		public static String getCategoryIDFromUri(Uri uri) {
			return uri.getQueryParameter(PATH_CATEGORY);
		}
	}

}
