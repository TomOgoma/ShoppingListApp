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
	public static final String PATH_BRAND = "brand";
	public static final String PATH_VERSION = "version";
	public static final String PATH_COMMON_ATTRIBUTES = "shoppingListItem";

	public static final class CategoryEntry implements BaseColumns {

		public static final Uri CONTENT_URI =
				BASE_CONTENT_URI.buildUpon().appendPath(PATH_CATEGORY).build();
		public static final String CONTENT_TYPE =
				"vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_CATEGORY;
		public static final String CONTENT_ITEM_TYPE =
				"vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_CATEGORY;

		public static final String DEFAULT_CATEGORY_NAME = "General";

		public static final String TABLE_NAME = "categories";
		public static final String COLUMN_NAME = "name";

		public static Uri buildCategoryUri(long id) {
			return ContentUris.withAppendedId(CONTENT_URI, id);
		}
	}

	public static final class CommonAttributesEntry implements BaseColumns {

		public static final Uri CONTENT_URI = BASE_CONTENT_URI
				.buildUpon()
				.appendPath(PATH_COMMON_ATTRIBUTES)
				.build();
		public static final String CONTENT_TYPE =
				"vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" +
						PATH_COMMON_ATTRIBUTES;
		public static final String CONTENT_ITEM_TYPE =
				"vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" +
						PATH_COMMON_ATTRIBUTES;

		public static final String TABLE_NAME = "common_attributes";

		public static final String COLUMN_PRICE = "price";
		public static final String COLUMN_QTTY = "quantity";
		public static final String COLUMN_DESC = "description";
		public static final String COLUMN_MEAS_UNIT = "meas_unit";
		public static final String COLUMN_USEFUL_UNIT = "useful_unit";
		public static final String COLUMN_USEFUL_PER_MEAS = "useful_per_meas";
		public static final String COLUMN_IN_LIST = "in_list";
		public static final String COLUMN_IN_CART = "in_cart";

		public static Uri buildCommonAttributesUri(long id) {
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

		public static Uri buildItemUri(long id) {
			return ContentUris.withAppendedId(CONTENT_URI, id);
		}

		public static Uri buildItemsInCategoryUri(long categoryID) {
			return CONTENT_URI.buildUpon()
			                  .appendPath(String.valueOf(categoryID))
			                  .build();
		}

		public static String getCategoryIDFromUri(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}

	public static final class BrandEntry implements BaseColumns {

		public static final Uri CONTENT_URI =
				BASE_CONTENT_URI.buildUpon().appendPath(PATH_BRAND).build();
		public static final String CONTENT_TYPE =
				"vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_BRAND;
		public static final String CONTENT_ITEM_TYPE =
				"vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_BRAND;

		public static final String TABLE_NAME = "itemBrands";

		public static final String COLUMN_ITEM_KEY = "item_id";
		public static final String COLUMN_COMMON_ATTRIBUTES_KEY = "common_attributes_id";
		public static final String COLUMN_NAME = "name";

		public static Uri buildBrandUri(long id) {
			return ContentUris.withAppendedId(CONTENT_URI, id);
		}

		public static Uri buildBrandWithVersionsUri(long itemID) {
			return buildBrandInItemUri(itemID)
					.buildUpon()
					.appendQueryParameter(PATH_VERSION, "1")
					.build();
		}

		public static Uri buildBrandInItemUri(long itemID) {
			return CONTENT_URI.buildUpon()
			                  .appendPath(String.valueOf(itemID))
			                  .build();
		}

		public static boolean isLoadVersions(Uri uri) {
			return uri.getQueryParameter(PATH_VERSION).equals("1");
		}

		public static String getItemIDFromUri(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}

	public static final class VersionEntry implements BaseColumns {

		public static final Uri CONTENT_URI =
				BASE_CONTENT_URI.buildUpon().appendPath(PATH_VERSION).build();
		public static final String CONTENT_TYPE =
				"vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_VERSION;
		public static final String CONTENT_ITEM_TYPE =
				"vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_VERSION;

		public static final String TABLE_NAME = "brandVersions";

		public static final String COLUMN_BRAND_KEY = "brand_id";
		public static final String COLUMN_COMMON_ATTRIBUTES_KEY = "common_attributes_id";
		public static final String COLUMN_NAME = "name";

		public static Uri buildVersionUri(long id) {
			return ContentUris.withAppendedId(CONTENT_URI, id);
		}

		public static Uri buildVersionBrandUri(long brandID) {
			return CONTENT_URI.buildUpon()
			                  .appendPath(String.valueOf(brandID))
			                  .build();
		}

		public static String getBrandIDFromUri(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}

}
