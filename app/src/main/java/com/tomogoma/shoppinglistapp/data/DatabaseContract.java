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
	public static final String PATH_CURRENCY = "currency";
	public static final String PATH_ITEM = "item";

	public static final String PARAM_DISTINCT = "distinct";

	public static final class CategoryEntry implements BaseColumns {

		public static final Uri CONTENT_URI =
				BASE_CONTENT_URI.buildUpon().appendPath(PATH_CATEGORY).build();
		public static final String CONTENT_TYPE =
				"vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_CATEGORY;

		public static final String DEFAULT_NAME = "General";
		public static final Long DEFAULT_ID = 1L;

		public static final String TABLE_NAME = "categories";
		public static final String COLUMN_NAME = "name";

		public static Uri buildCategoryUri(long id) {
			return ContentUris.withAppendedId(CONTENT_URI, id);
		}
	}

	public static final class CurrencyEntry implements BaseColumns {

		public static final Uri CONTENT_URI =
				BASE_CONTENT_URI.buildUpon().appendPath(PATH_CURRENCY).build();
		public static final String CONTENT_TYPE =
				"vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_CURRENCY;
		public static final String CONTENT_ITEM_TYPE =
				"vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_CURRENCY;

		public static final Long DEFAULT_ID = 1L;
		public static final String DEFAULT_CODE = "KES";
		public static final String DEFAULT_SYMBOL = "KSh";
		public static final String DEFAULT_NAME = "Kenyan shilling";
		public static final String DEFAULT_COUNTRY = "Kenya";

		public static final String TABLE_NAME = "currencies";

		public static final String COLUMN_CODE = "code"; // e.g. "KES" for Kenyan Shillings, or "USD" for United States Dollar
		public static final String COLUMN_SYMBOL = "symbol"; // e.g. "KSh" for Kenyan Shillings, or "$" for United States Dollar
		public static final String COLUMN_NAME = "name"; //  e.g. "Kenyan Shilling" or "United States Dollar"
		public static final String COLUMN_COUNTRY = "country";
		public static final String COLUMN_LAST_CONVERSION = "last_conversion"; //  the latest conversion done from default to given currency

		public static Uri buildCurrencyUri(long id) {
			return ContentUris.withAppendedId(CONTENT_URI, id);
		}

		public static String getCurrencyIDFromUri(Uri uri) {
			return uri.getPathSegments().get(1);
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
		public static final String COLUMN_CURRENCY_KEY = "currency_id";

		public static final String COLUMN_NAME = "name";
		public static final String COLUMN_PRICE = "price";
		public static final String COLUMN_QUANTITY = "quantity";
		public static final String COLUMN_DESC = "description";
		public static final String COLUMN_MEAS_UNIT = "meas_unit";
		public static final String COLUMN_LASTS_FOR = "lasts_for";
		public static final String COLUMN_LASTS_FOR_UNIT = "lasts_for_unit";
		public static final String COLUMN_IN_LIST = "in_list";
		public static final String COLUMN_IN_CART = "in_cart";

		public static Uri buildItemUri(long id) {
			return ContentUris.withAppendedId(CONTENT_URI, id);
		}

		public static String getItemIDFromUri(Uri uri) {
			return uri.getPathSegments().get(1);
		}

		public static Uri buildItemsInCategoryUri(long categoryID, boolean leftJoinCurrency) {
			return CONTENT_URI.buildUpon()
			                  .appendQueryParameter(PATH_CATEGORY, String.valueOf(categoryID))
			                  .appendQueryParameter(PATH_CURRENCY, leftJoinCurrency ? "1" : "0")
			                  .build();
		}

		public static Uri buildItemsCurrenciesUri() {
			return CONTENT_URI.buildUpon()
			                  .appendQueryParameter(PATH_CURRENCY,  "1")
			                  .appendQueryParameter(PARAM_DISTINCT,  "1")
			                  .build();
		}

		public static String getCategoryIDFromUri(Uri uri) {
			return uri.getQueryParameter(PATH_CATEGORY);
		}

		public static boolean isToInnerJoinCurrency(Uri uri) {
			String param = uri.getQueryParameter(PATH_CURRENCY);
			if (param == null)
				return false;
			return param.equals("1");
		}

		public static boolean isToSelectDistinct(Uri uri) {
			String param = uri.getQueryParameter(PARAM_DISTINCT);
			if (param == null)
				return false;
			return param.equals("1");
		}
	}

}
