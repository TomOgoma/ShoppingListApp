package com.tomogoma.shoppinglistapp;

import android.app.Activity;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.tomogoma.shoppinglistapp.data.DatabaseContract.CommonAttributesEntry;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.ItemEntry;
import com.tomogoma.util.Formatter;

/**
 * Created by ogoma on 24/02/15.
 */
public class ItemListAdapter extends SimpleCursorAdapter
		implements SimpleCursorAdapter.ViewBinder {

	public static final String[] itemsProjection = new String[]{
			ItemEntry.TABLE_NAME + "." + ItemEntry._ID,
			ItemEntry.COLUMN_NAME,
			CommonAttributesEntry.COLUMN_DESC,
			CommonAttributesEntry.COLUMN_PRICE,
			CommonAttributesEntry.COLUMN_QTTY,
			CommonAttributesEntry.COLUMN_MEAS_UNIT,
			CommonAttributesEntry.COLUMN_USEFUL_UNIT,
			CommonAttributesEntry.COLUMN_USEFUL_PER_MEAS,
			CommonAttributesEntry.COLUMN_IN_LIST
	};
	private static final int ITEM_ID_COL_INDEX = 0;
	private static final int ITEM_NAME_COL_INDEX = 1;
	private static final int ITEM_DESC_COL_INDEX = 2;
	private static final int ITEM_PRICE_COL_INDEX = 3;
	private static final int ITEM_QTTY_COL_INDEX = 4;
	private static final int ITEM_MEAS_UNIT_COL_INDEX = 5;
	private static final int ITEM_USEFUL_UNIT_COL_INDEX = 6;
	private static final int ITEM_USEFUL_PER_MEAS_COL_INDEX = 7;
	private static final int ITEM_IN_LIST_COL_INDEX = 8;

	private static final String[] itemViewColumns = new String[]{
			ItemEntry.COLUMN_NAME,
			CommonAttributesEntry.COLUMN_DESC,
			CommonAttributesEntry.COLUMN_QTTY,
			CommonAttributesEntry.COLUMN_MEAS_UNIT,
			CommonAttributesEntry.COLUMN_USEFUL_UNIT,
			CommonAttributesEntry.COLUMN_USEFUL_PER_MEAS,
			CommonAttributesEntry.COLUMN_PRICE,
			CommonAttributesEntry.COLUMN_IN_LIST,
			CommonAttributesEntry.COLUMN_PRICE,
	};
	private static final int[] itemViews = new int[]{
			R.id.title, R.id.description, R.id.quantity, R.id.measUnit, R.id.lastsForUnit, R.id.usefulQtty,
			R.id.unitPrice, R.id.shoppingListAdd, R.id.totalPrice
	};

	public ItemListAdapter(Activity activity) {

		super(activity, R.layout.list_item, null, itemViewColumns, itemViews, 0);
		setViewBinder(this);
	}

	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

		switch (columnIndex) {

			case ITEM_USEFUL_PER_MEAS_COL_INDEX: {

				float usefulPerAct = cursor.getFloat(columnIndex);
				float quantity = cursor.getFloat(ITEM_QTTY_COL_INDEX);
				Log.d(getClass().getSimpleName(), "usefulPerAct: " + usefulPerAct);
				Log.d(getClass().getSimpleName(), "quantity: " + quantity);
				setTextViewQuantity(view, usefulPerAct * quantity);
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
			case ITEM_IN_LIST_COL_INDEX: {

				boolean isInList;
				try {
					int inList = cursor.getInt(columnIndex);
					if (inList == 1) {
						isInList = true;
					} else {
						isInList = false;
					}
				} catch (Exception e) {
					Log.d(getClass().getSimpleName(), "caught exception  checking if is in list: " + e.getMessage());
					isInList = false;
				}
				((CheckBox) view).setChecked(isInList);
			}
		}//Switch...
		return false;
	}

	private void setTextViewQuantity(View view, float quantity) {

		Log.d(getClass().getSimpleName(), "Setting quantity: " + quantity);
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
