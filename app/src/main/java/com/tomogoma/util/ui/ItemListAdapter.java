package com.tomogoma.util.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.tomogoma.shoppinglistapp.R;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.ItemEntry;
import com.tomogoma.util.Formatter;

/**
 * Created by ogoma on 24/02/15.
 */
public class ItemListAdapter extends SimpleCursorAdapter
		implements SimpleCursorAdapter.ViewBinder {

	public static final String[] itemsProjection = new String[]{
			ItemEntry._ID,
			ItemEntry.COLUMN_NAME,
			ItemEntry.COLUMN_DESC,
			ItemEntry.COLUMN_PRICE,
			ItemEntry.COLUMN_QTTY,
			ItemEntry.COLUMN_MEAS_UNIT,
			ItemEntry.COLUMN_USEFUL_UNIT,
			ItemEntry.COLUMN_USEFUL_PER_MEAS,
			ItemEntry.COLUMN_IN_LIST
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
			ItemEntry.COLUMN_DESC,
			ItemEntry.COLUMN_QTTY,
			ItemEntry.COLUMN_MEAS_UNIT,
			ItemEntry.COLUMN_USEFUL_UNIT,
			ItemEntry.COLUMN_USEFUL_PER_MEAS,
			ItemEntry.COLUMN_PRICE,
			ItemEntry.COLUMN_IN_LIST,
			ItemEntry.COLUMN_PRICE,
	};
	private static final int[] itemViews = new int[]{
			R.id.title, R.id.description, R.id.quantity, R.id.measUnit, R.id.lastsForUnit, R.id.usefulQtty,
			R.id.unitPrice, R.id.shoppingListAdd, R.id.totalPrice
	};

	private Context context;

	public ItemListAdapter(Activity activity) {

		super(activity, R.layout.list_item, null, itemViewColumns, itemViews, 0);
		this.context = activity;
		setViewBinder(this);
	}

	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

		switch (columnIndex) {

			case ITEM_USEFUL_PER_MEAS_COL_INDEX: {

				float usefulPerAct = cursor.getFloat(columnIndex);
				float quantity = cursor.getFloat(ITEM_QTTY_COL_INDEX);
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

				long itemID = cursor.getLong(ITEM_ID_COL_INDEX);
				boolean isInList;
				try {
					int inList = cursor.getInt(columnIndex);
					if (inList == 1) {
						isInList = true;
					} else {
						isInList = false;
					}
				} catch (Exception e) {
					isInList = false;
				}
				CheckBox checkBox = (CheckBox) view;
				checkBox.setChecked(isInList);
				checkBox.setOnCheckedChangeListener(new OnCheckListener(itemID));
				return true;
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

	private class OnCheckListener implements OnCheckedChangeListener {

		private long itemID;

		OnCheckListener(long itemID) {
			this.itemID = itemID;
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

			String selection = ItemEntry._ID + " = ?";
			String[] selectionArgs = new String[] {String.valueOf(itemID)};
			Uri uri = ItemEntry.CONTENT_URI;

			int databaseEntry;
			if (isChecked) {
				databaseEntry = 1;
			} else {
				databaseEntry = 0;
			}

			ContentValues contentValues = new ContentValues();
			contentValues.put(ItemEntry.COLUMN_IN_LIST, databaseEntry);

			context.getContentResolver().update(uri, contentValues, selection, selectionArgs);
		}
	}

}
