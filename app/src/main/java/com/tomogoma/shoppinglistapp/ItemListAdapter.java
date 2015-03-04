package com.tomogoma.shoppinglistapp;

import android.app.Activity;
import android.database.Cursor;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;

import com.tomogoma.shoppinglistapp.data.DatabaseContract.BrandEntry;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.ItemEntry;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.VersionEntry;
import com.tomogoma.util.Formatter;

import java.util.HashMap;

/**
 * Created by ogoma on 24/02/15.
 */
public class ItemListAdapter extends SimpleCursorTreeAdapter
		implements SimpleCursorTreeAdapter.ViewBinder {


	public static final String[] itemsProjection = new String[]{
			ItemEntry.TABLE_NAME + "." + ItemEntry._ID,
			ItemEntry.TABLE_NAME + "." + ItemEntry.COLUMN_NAME,
			ItemEntry.TABLE_NAME + "." + ItemEntry.COLUMN_DESC,
			ItemEntry.TABLE_NAME + "." + ItemEntry.COLUMN_PRICE,
			ItemEntry.TABLE_NAME + "." + ItemEntry.COLUMN_QTTY,
			ItemEntry.TABLE_NAME + "." + ItemEntry.COLUMN_MEAS_UNIT,
			ItemEntry.TABLE_NAME + "." + ItemEntry.COLUMN_USEFUL_UNIT,
			ItemEntry.TABLE_NAME + "." + ItemEntry.COLUMN_USEFUL_PER_MEAS
	};
	public static final String[] brandProjection = new String[]{
			BrandEntry.TABLE_NAME + "." + BrandEntry._ID,
			BrandEntry.TABLE_NAME + "." + BrandEntry.COLUMN_NAME,
			BrandEntry.TABLE_NAME + "." + BrandEntry.COLUMN_DESC,
			BrandEntry.TABLE_NAME + "." + BrandEntry.COLUMN_PRICE,
			BrandEntry.TABLE_NAME + "." + BrandEntry.COLUMN_QTTY,
			VersionEntry.TABLE_NAME + "." + VersionEntry._ID,
			VersionEntry.TABLE_NAME + "." + VersionEntry.COLUMN_NAME,
			VersionEntry.TABLE_NAME + "." + VersionEntry.COLUMN_DESC,
			VersionEntry.TABLE_NAME + "." + VersionEntry.COLUMN_PRICE,
			VersionEntry.TABLE_NAME + "." + VersionEntry.COLUMN_QTTY
	};
	private static final int ITEM_ID_COL_INDEX = 0;
	private static final int ITEM_NAME_COL_INDEX = 1;
	private static final int ITEM_DESC_COL_INDEX = 2;
	private static final int ITEM_PRICE_COL_INDEX = 3;
	private static final int ITEM_QTTY_COL_INDEX = 4;
	private static final int ITEM_MEAS_UNIT_COL_INDEX = 5;
	private static final int ITEM_USEFUL_UNIT_COL_INDEX = 6;
	private static final int ITEM_USEFUL_PER_MEAS_COL_INDEX = 7;
	private static final int BRAND_ID_COL_INDEX = 0;
	private static final int BRAND_NAME_COL_INDEX = 1;
	private static final int BRAND_DESC_COL_INDEX = 2;
	private static final int BRAND_PRICE_COL_INDEX = 3;
	private static final int BRAND_QTTY_COL_INDEX = 4;
	private static final int VERSION_ID_COL_INDEX = 5;
	private static final int VERSION_NAME_COL_INDEX = 6;
	private static final int VERSION_DESC_COL_INDEX = 7;
	private static final int VERSION_PRICE_COL_INDEX = 8;
	private static final int VERSION_QTTY_COL_INDEX = 9;

	private static final String[] itemViewColumns = new String[]{
			ItemEntry.TABLE_NAME + "." + ItemEntry.COLUMN_NAME,
			//  TODO include desc
			ItemEntry.TABLE_NAME + "." + ItemEntry.COLUMN_QTTY,
			ItemEntry.TABLE_NAME + "." + ItemEntry.COLUMN_MEAS_UNIT,
			ItemEntry.TABLE_NAME + "." + ItemEntry.COLUMN_USEFUL_PER_MEAS,
			ItemEntry.TABLE_NAME + "." + ItemEntry.COLUMN_USEFUL_UNIT,
			ItemEntry.TABLE_NAME + "." + ItemEntry.COLUMN_PRICE,
			//  TODO create shoppinglisttable with FKs for itemIDs
			ItemEntry.TABLE_NAME + "." + ItemEntry.COLUMN_PRICE,
	};
	private static final int[] itemViews = new int[]{
			R.id.title, R.id.quantity, R.id.measUnit, R.id.usefulQtty, R.id.usefulUnit,
			R.id.unitPrice, R.id.totalPrice
	};

	private static final String[] brandViewColumns = new String[]{
			BrandEntry.TABLE_NAME + "." + BrandEntry.COLUMN_NAME,
			VersionEntry.TABLE_NAME + "." + VersionEntry.COLUMN_NAME,
			//  TODO include desc
			VersionEntry.TABLE_NAME + "." + VersionEntry.COLUMN_QTTY,
			VersionEntry.TABLE_NAME + "." + VersionEntry.COLUMN_PRICE,
			//  TODO create shoppinglisttable with FKs for itemIDs
			VersionEntry.TABLE_NAME + "." + VersionEntry.COLUMN_PRICE,
	};
	private static final int[] brandViews = new int[]{
			R.id.title, R.id.version, R.id.quantity, R.id.unitPrice, R.id.totalPrice
	};


	private String measUnit, usefulUnit;
	private float usefulPerMeas;
	private ItemsFragment fragment;

	private HashMap<Integer, Integer> groupMap;

	public ItemListAdapter(Activity activity, Cursor cursor, ItemsFragment fragment) {

		super(
				activity,
				cursor,
				R.layout.list_item,
				R.layout.list_item,
				itemViewColumns,
				itemViews,
				R.layout.list_item,
				brandViewColumns,
				brandViews);

		this.fragment = fragment;
		setViewBinder(this);
	}

	public HashMap<Integer, Integer> getGroupMap() {
		return groupMap;
	}

	@Override
	protected Cursor getChildrenCursor(Cursor groupCursor) {

		Log.d(getClass().getSimpleName(), "getchildrencursor called");

		measUnit = groupCursor.getString(ITEM_USEFUL_UNIT_COL_INDEX);
		usefulUnit = groupCursor.getString(ITEM_MEAS_UNIT_COL_INDEX);
		usefulPerMeas = groupCursor.getFloat(ITEM_USEFUL_PER_MEAS_COL_INDEX);
		int itemID = groupCursor.getInt(ITEM_ID_COL_INDEX);

		Loader<Cursor> loader = fragment.getLoaderManager().getLoader(itemID);
		if (loader != null && !loader.isReset()) {
			fragment.getLoaderManager().restartLoader(itemID, null, fragment);
		} else {
			fragment.getLoaderManager().initLoader(itemID, null, fragment);
		}

		return null;
	}

	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

		switch (columnIndex) {

			case ITEM_USEFUL_PER_MEAS_COL_INDEX: {

				float usefulPerAct = cursor.getFloat(columnIndex);
				float quantity = cursor.getFloat(ITEM_QTTY_COL_INDEX);
				String usefulQuantity = Formatter.formatQuantity(usefulPerAct * quantity);
				((TextView) view).setText(usefulQuantity);
				return true;
			}
			case ITEM_QTTY_COL_INDEX: {

				float quantity = cursor.getFloat(columnIndex);
				setTextViewQuantity(view, quantity);
				return true;
			}
			case ITEM_PRICE_COL_INDEX: {

				double price = cursor.getDouble(columnIndex);
				float quantity = cursor.getFloat(ITEM_QTTY_COL_INDEX);
				return setTextViewPrice(view, price, quantity);
			}
			case BRAND_NAME_COL_INDEX: {

				String brandName = cursor.getString(columnIndex);
				((TextView) view).setText("(" + brandName + ")");
				return true;
			}
			case VERSION_NAME_COL_INDEX: {

				//  TODO add meas unit etc
				String versionName = cursor.getString(columnIndex);
				if (versionName != null && !versionName.isEmpty()) {
					((TextView) view).setText("(" + versionName + ")");
				}
				return true;
			}
			case VERSION_QTTY_COL_INDEX: {

				float quantity = cursor.getFloat(columnIndex);
				if (quantity == 0) {
					quantity = cursor.getFloat(BRAND_QTTY_COL_INDEX);
				}
				setTextViewQuantity(view, quantity);
				return true;
			}
			case VERSION_PRICE_COL_INDEX: {

				float quantity;
				double price;
				String versionName = cursor.getString(VERSION_NAME_COL_INDEX);
				if (versionName == null || versionName.isEmpty()) {
					price = cursor.getDouble(BRAND_PRICE_COL_INDEX);
					quantity = cursor.getFloat(BRAND_QTTY_COL_INDEX);
				} else {
					price = cursor.getDouble(columnIndex);
					quantity = cursor.getFloat(VERSION_QTTY_COL_INDEX);
				}
				return setTextViewPrice(view, price, quantity);
			}
		}//Switch...
		return false;
	}

	private void setTextViewQuantity(View view, float quantity) {

		String quantityStr = Formatter.formatQuantity(quantity);
		((TextView) view).setText(quantityStr);
	}

	private boolean setTextViewPrice(View view, double price, float quantity) {

		if (view.getId() == R.id.unitPrice) {

			String priceStr = Formatter.formatUnitPrice(price);
			((TextView) view).setText(priceStr);
			return true;
		} else if (view.getId() == R.id.totalPrice) {

			String totalPriceStr = Formatter.formatPrice(price, quantity);
			((TextView) view).setText(totalPriceStr);
			return true;
		}
		return false;
	}

}
