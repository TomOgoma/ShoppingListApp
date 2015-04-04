package com.tomogoma.shoppinglistapp.items.manipulate.edit;


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.tomogoma.shoppinglistapp.R;
import com.tomogoma.shoppinglistapp.data.ContentLoader;
import com.tomogoma.shoppinglistapp.data.ContentLoader.OnLoadFinishedListener;
import com.tomogoma.shoppinglistapp.data.DBUpdateHelper;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.CategoryEntry;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.CurrencyEntry;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.ItemEntry;
import com.tomogoma.shoppinglistapp.data.Item;
import com.tomogoma.shoppinglistapp.items.list.ListingActivity;
import com.tomogoma.shoppinglistapp.items.manipulate.ManipulateItemFragment;
import com.tomogoma.shoppinglistapp.util.Formatter;
import com.tomogoma.shoppinglistapp.util.Preference;
import com.tomogoma.shoppinglistapp.util.UI;

public class EditItemFragment extends ManipulateItemFragment {

	private static final String LOG_TAG = EditItemFragment.class.getSimpleName();

	private long mItemID = -1;
	private long mItemLoaderID = -1;
	private long mPreferredCurrencyLoaderId = -1;
	private boolean mIsCursorsLoaded;
	private ContentLoader mLoader;
	private View mRootView;
	private Cursor mItemCursor;
	private Cursor mPreferredCurrencyCursor;

	private class OnItemLoadFinishedListener implements OnLoadFinishedListener {

		private boolean isOtherLoaded;

		@Override
		public void onLoadFinished(int id) {

			if (mItemLoaderID == id) {
				mItemCursor = mLoader.getLoadedCursor(id);
				populateFieldsWhenReady();
			}
			else if (mPreferredCurrencyLoaderId == id) {
				mPreferredCurrencyCursor = mLoader.getLoadedCursor(id);
				populateFieldsWhenReady();
			}
		}

		private synchronized void populateFieldsWhenReady() {
			if (isOtherLoaded) {

				if (mIsAutoTextViewAdaptersLoaded) {
					populateFields();
				} else {
					mIsCursorsLoaded = true;
				}
			} else {
				isOtherLoaded = true;
			}
		}
	}

	@Override
	protected void onSuperAdaptersLoaded() {
		if (mIsCursorsLoaded) {
			populateFields();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle arguments = getArguments();
		if (arguments != null) {
			mItemID = arguments.getLong(ListingActivity.EXTRA_long_ITEM_ID, -1);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {

		if (mItemID <= 0) {
			Log.e(LOG_TAG, "Failed to fetch the item ID to be edited");
			UI.showKeyboardToast(getActivity(), getString(R.string.error_toast_loading_data));
		}
		mRootView = super.onCreateView(inflater, container, savedInstanceState);
		return mRootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		long preferredCurrencyID = Preference.getPreferredCurrencyID(getActivity());
		Uri currencyUri = CurrencyEntry.buildCurrencyUri(preferredCurrencyID);
		String[] currencyProjection = new String[] {
				CurrencyEntry.COLUMN_CODE,
				CurrencyEntry.COLUMN_LAST_CONVERSION
			};

		Uri itemUri = ItemEntry.buildItemCurrencyUri(mItemID);
		String[] itemProjection = new String[] {
				ItemEntry.TABLE_NAME + ".*",
				CurrencyEntry.COLUMN_CODE,
				CurrencyEntry.COLUMN_LAST_CONVERSION
			};
		mLoader = new ContentLoader(getActivity(), this);
		mLoader.setOnLoadFinishedListener(new OnItemLoadFinishedListener());
		mItemLoaderID = mLoader.loadContent(itemUri, itemProjection, null);
		mPreferredCurrencyLoaderId = mLoader.loadContent(currencyUri, currencyProjection, null);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {

		super.onViewStateRestored(savedInstanceState);
		if (savedInstanceState != null) {
			processDependentViewVisibility(mEtActualMeasUnit);
		}
	}

	@Override
	protected Intent processInput(String categoryName, String itemName) {

		long categoryID = DBUpdateHelper.addCategory(getActivity(), categoryName);

		if (itemName.isEmpty()) {
			UI.showKeyboardToast(getActivity(), getString(R.string.error_toast_missing_item_name));
			mAutoTvItemName.setError(getString(R.string.error_inputViewErr_missing_item));
			return null;
		}

		int updateCount = DBUpdateHelper.updateItem(
				getActivity(),
				new Item(categoryID, mItemID, mLastsForUnit, mAutoTvItemName, mEtUnitPrice, mEtQuantity, mEtLastsFor, mEtActualMeasUnit, mEtDesc)
		);

		if (updateCount <0) {
			Log.e(LOG_TAG, "Error updating db");
			String message = getString(R.string.error_toast_db_update_fail);
			UI.showKeyboardToast(getActivity(), String.format(message, itemName));
			return null;
		} else if (updateCount == 0) {
			//  TODO edit the item?
			Log.e(LOG_TAG, "Error updating db");
			String message = getString(R.string.error_toast_db_update_fail);
			UI.showKeyboardToast(getActivity(), String.format(message, itemName));
			return null;
		} else if (updateCount > 1) {
			Log.e(LOG_TAG, "Updated more than one item; expected single update; count=" + updateCount);
			UI.showKeyboardToast(getActivity(), getString(R.string.error_toast_db_potential_data_corruption));
			throw new RuntimeException("Updated more items than expected in db" + updateCount);
		}
		String butPlaced = "";
		if (categoryName.isEmpty()) {
			categoryName = CategoryEntry.DEFAULT_NAME;
			butPlaced = getString(R.string.toast_but_placed);
		}

		String toastMessage = getString(R.string.toast_item_updated);
		toastMessage = String.format(toastMessage, itemName, butPlaced, categoryName);
		UI.showKeyboardToast(getActivity(), toastMessage);
		return packageResultIntent(categoryID, categoryName);
	}

	private void populateFields() {

		Double unitPrice, boundConversion, preferredConversion, price;
		String boundCode, lastsForUnit, preferredCode, itemName, itemQuantity,
			measUnit, lastsFor, description;
		try {

			mItemCursor.moveToFirst();
			int colIndex = mItemCursor.getColumnIndex(ItemEntry.COLUMN_PRICE);
				unitPrice = mItemCursor.getDouble(colIndex);
			colIndex = mItemCursor.getColumnIndex(ItemEntry.COLUMN_NAME);
				itemName = mItemCursor.getString(colIndex);
			colIndex = mItemCursor.getColumnIndex(ItemEntry.COLUMN_QUANTITY);
				itemQuantity = mItemCursor.getString(colIndex);
			colIndex = mItemCursor.getColumnIndex(ItemEntry.COLUMN_MEAS_UNIT);
				measUnit = mItemCursor.getString(colIndex);
			colIndex = mItemCursor.getColumnIndex(ItemEntry.COLUMN_LASTS_FOR_UNIT);
				lastsFor = mItemCursor.getString(colIndex);
			colIndex = mItemCursor.getColumnIndex(ItemEntry.COLUMN_DESC);
				description = mItemCursor.getString(colIndex);
			colIndex = mItemCursor.getColumnIndex(ItemEntry.COLUMN_LASTS_FOR);
				lastsForUnit = mItemCursor.getString(colIndex);

			colIndex = mItemCursor.getColumnIndex(CurrencyEntry.COLUMN_CODE);
				boundCode = mItemCursor.getString(colIndex);
			colIndex = mItemCursor.getColumnIndex(CurrencyEntry.COLUMN_LAST_CONVERSION);
				boundConversion = mItemCursor.getDouble(colIndex);

			mPreferredCurrencyCursor.moveToFirst();
			colIndex = mPreferredCurrencyCursor.getColumnIndex(CurrencyEntry.COLUMN_CODE);
				preferredCode = mPreferredCurrencyCursor.getString(colIndex);
			colIndex = mPreferredCurrencyCursor.getColumnIndex(CurrencyEntry.COLUMN_LAST_CONVERSION);
				preferredConversion = mPreferredCurrencyCursor.getDouble(colIndex);
		} finally {
			mItemCursor.close();
			mPreferredCurrencyCursor.close();
		}

		RadioButton rgLastsDays = (RadioButton) mRootView.findViewById(R.id.lastsDays);
		RadioButton rgLastsWeeks = (RadioButton) mRootView.findViewById(R.id.lastsWeeks);
		RadioButton rgLastsMonths = (RadioButton) mRootView.findViewById(R.id.lastsMonths);
		RadioButton rgLastsYears = (RadioButton) mRootView.findViewById(R.id.lastsYears);

		price = Formatter.convertPrice(unitPrice, boundCode, boundConversion, preferredCode, preferredConversion);

		mAutoTvItemName.setText(itemName);
		mEtUnitPrice.setText(String.valueOf(price));
		mEtQuantity.setText(itemQuantity);
		mEtActualMeasUnit.setText(measUnit);
		mEtLastsFor.setText(lastsFor);
		mEtDesc.setText(description);

		processDependentViewVisibility(mEtActualMeasUnit);

		if (lastsForUnit.equals(getString(R.string.lasts_day_text))) {
			rgLastsDays.setChecked(true);
		} else if  (lastsForUnit.equals(getString(R.string.lasts_week_text))) {
			rgLastsWeeks.setChecked(true);
		} else if  (lastsForUnit.equals(getString(R.string.lasts_month_text))) {
			rgLastsMonths.setChecked(true);
		} else if  (lastsForUnit.equals(getString(R.string.lasts_year_text))) {
			rgLastsYears.setChecked(true);
		}

		mEtQuantity.requestFocus();
	}

}
