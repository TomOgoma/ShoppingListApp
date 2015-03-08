package com.tomogoma.util.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class ItemListAdapter extends CursorAdapter {

	public static final String[] S_ITEMS_PROJECTION = new String[]{
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
	private static final int ITEM_QUANTITY_COL_INDEX = 4;
	private static final int ITEM_MEAS_UNIT_COL_INDEX = 5;
	private static final int ITEM_LASTS_FOR_UNIT_COL_INDEX = 6;
	private static final int ITEM_LASTS_FOR_COL_INDEX = 7;
	private static final int ITEM_IN_LIST_COL_INDEX = 8;

	private class ViewHolder {

		private TextView title;
		private TextView quantity;
		private TextView measUnit;
//		private TextView openingBracket;
//		private TextView lastsFor;
//		private TextView lastsForUnit;
//		private TextView closingBracket;
		private CheckBox isInList;
		private TextView unitPrice;
		private TextView totalPrice;

		private ViewHolder(View listItemView) {

			title = (TextView) listItemView.findViewById(R.id.title);
			quantity = (TextView) listItemView.findViewById(R.id.quantity);
			measUnit = (TextView) listItemView.findViewById(R.id.measUnit);
//			openingBracket = (TextView) listItemView.findViewById(R.id.openingParenthesis);
//			lastsFor = (TextView) listItemView.findViewById(R.id.lastsFor);
//			lastsForUnit = (TextView) listItemView.findViewById(R.id.lastsForUnit);
//			closingBracket = (TextView) listItemView.findViewById(R.id.closingParenthesis);
			isInList = (CheckBox) listItemView.findViewById(R.id.shoppingListAdd);
			unitPrice = (TextView) listItemView.findViewById(R.id.unitPrice);
			totalPrice = (TextView) listItemView.findViewById(R.id.totalPrice);
		}
	}

	private Context context;

	public ItemListAdapter(Activity activity) {

		super(activity, null, 0);
		this.context = activity;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {

		View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
		ViewHolder viewHolder = new ViewHolder(view);
		view.setTag(viewHolder);
		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		ViewHolder viewHolder = (ViewHolder) view.getTag();

		long itemID = cursor.getLong(ITEM_ID_COL_INDEX);
		int inList = cursor.getInt(ITEM_IN_LIST_COL_INDEX);
		float lastsFor = cursor.getFloat(ITEM_LASTS_FOR_COL_INDEX);
		float quantity = cursor.getFloat(ITEM_QUANTITY_COL_INDEX);
		double price = cursor.getDouble(ITEM_PRICE_COL_INDEX);
		String name = cursor.getString(ITEM_NAME_COL_INDEX);
		String measUnit = cursor.getString(ITEM_MEAS_UNIT_COL_INDEX);
		String lastsForUnit = cursor.getString(ITEM_LASTS_FOR_UNIT_COL_INDEX);

		viewHolder.title.setText(name);
		setQuantity(viewHolder, measUnit, lastsForUnit, quantity, lastsFor);
		setPrice(viewHolder, price, quantity);
		setInList(viewHolder, itemID, inList);
	}

	private void setQuantity(TextView view, float quantity) {

		String quantityStr = Formatter.formatQuantity(quantity);
		view.setText(quantityStr);
	}

	private void setPrice(ViewHolder viewHolder, double price, float quantity) {

		//  if no price to show, hide the price view
		if (price == 0d) {

			viewHolder.unitPrice.setVisibility(View.GONE);
			viewHolder.totalPrice.setVisibility(View.GONE);
			return;
		}

		viewHolder.unitPrice.setVisibility(View.VISIBLE);
		viewHolder.totalPrice.setVisibility(View.VISIBLE);

		//  assume total price is for one item if quantity not set
		quantity = (quantity == 0f)? 1f: quantity;

		String unitPriceStr = Formatter.formatUnitPrice(price);
		String totalPriceStr = Formatter.formatPrice(price, quantity);

		viewHolder.unitPrice.setText(unitPriceStr);
		viewHolder.totalPrice.setText(totalPriceStr);
	}

	private void setQuantity (ViewHolder viewHolder, String measUnit, String lastsForUnit, float quantity, float lastsFor) {

		//  Hide quantity views if no quantity present to display
		if (quantity == 0) {
			viewHolder.quantity.setVisibility(View.GONE);
			viewHolder.measUnit.setVisibility(View.GONE);
//			viewHolder.openingBracket.setVisibility(View.GONE);
//			viewHolder.lastsFor.setVisibility(View.GONE);
//			viewHolder.lastsForUnit.setVisibility(View.GONE);
//			viewHolder.closingBracket.setVisibility(View.GONE);
			return;
		}

		// hide last for views if last for not set
//		if (lastsFor == 0) {
//
//			viewHolder.openingBracket.setVisibility(View.GONE);
//			viewHolder.lastsFor.setVisibility(View.GONE);
//			viewHolder.lastsForUnit.setVisibility(View.GONE);
//			viewHolder.closingBracket.setVisibility(View.GONE);
//		} else {
//
//			viewHolder.openingBracket.setVisibility(View.VISIBLE);
//			viewHolder.lastsFor.setVisibility(View.VISIBLE);
//			viewHolder.lastsForUnit.setVisibility(View.VISIBLE);
//			viewHolder.closingBracket.setVisibility(View.VISIBLE);
//
//			setQuantity(viewHolder.lastsFor, lastsFor * quantity);
//			viewHolder.lastsForUnit.setText(lastsForUnit);
//		}

		viewHolder.quantity.setVisibility(View.VISIBLE);
		viewHolder.measUnit.setVisibility(View.VISIBLE);

		measUnit = Formatter.formatMeasUnit(measUnit);

		setQuantity(viewHolder.quantity, quantity);
		viewHolder.measUnit.setText(measUnit);
	}

	private void setInList(ViewHolder viewHolder, long itemID, int inList) {

		boolean isInList;
		try {
			if (inList == 1) {
				isInList = true;
			} else {
				isInList = false;
			}
		} catch (Exception e) {
			isInList = false;
		}

		viewHolder.isInList.setChecked(isInList);
		viewHolder.isInList.setOnCheckedChangeListener(new OnCheckListener(itemID));
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
