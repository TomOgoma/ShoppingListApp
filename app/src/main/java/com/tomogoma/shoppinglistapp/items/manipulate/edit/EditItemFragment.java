package com.tomogoma.shoppinglistapp.items.manipulate.edit;


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.tomogoma.shoppinglistapp.R;
import com.tomogoma.shoppinglistapp.data.ContentLoader;
import com.tomogoma.shoppinglistapp.data.ContentLoader.OnLoadFinishedListener;
import com.tomogoma.shoppinglistapp.data.DBUpdateHelper;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.CategoryEntry;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.ItemEntry;
import com.tomogoma.shoppinglistapp.data.Item;
import com.tomogoma.shoppinglistapp.items.manipulate.ManipulateItemFragment;
import com.tomogoma.shoppinglistapp.util.UI;

public class EditItemFragment extends ManipulateItemFragment {

	public static final String EXTRA_long_ITEM_ID = EditItemFragment.class.getName() + "_extra.category.id";

	private long mItemID = -1;
	private long mItemLoaderID = -1;
	private ContentLoader mLoader;
	private View mRootView;

	private class OnItemLoadFinishedListener implements OnLoadFinishedListener {

		@Override
		public void onLoadFinished(int id) {
			if (mItemLoaderID == id) {
				populateFields(mLoader.getLoadedCursor(id));
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle arguments = getArguments();
		if (arguments != null) {
			mItemID = arguments.getLong(EXTRA_long_ITEM_ID, -1);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {

		if (mItemID <= 0) {
			UI.showToast(getActivity(), "An error occured loading data, please try again");
		}
		mRootView = super.onCreateView(inflater, container, savedInstanceState);
		return mRootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		Uri itemUri = ItemEntry.buildItemUri(mItemID);
		mLoader = new ContentLoader(getActivity(), this);
		mLoader.setOnLoadFinishedListener(new OnItemLoadFinishedListener());
		mItemLoaderID = mLoader.loadContent(itemUri, null, null);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {

		super.onViewStateRestored(savedInstanceState);
		if (savedInstanceState != null) {
			processDependentViewVisibility(etActualMeasUnit);
		}
	}

	@Override
	protected Intent processInput(String categoryName, String itemName) {

		long categoryID = DBUpdateHelper.addCategory(getActivity(), categoryName);

		if (itemName.isEmpty()) {
			UI.showToast(getActivity(), "Cannot modify to an empty item name");
			autoTvItemName.setError(getString(R.string.missing_item_error_view));
			return null;
		}

		int updateCount = DBUpdateHelper.updateItem(
				getActivity(),
				new Item(categoryID, mItemID, mLastsForUnit, autoTvItemName, etUnitPrice, etQuantity, etLastsFor, etActualMeasUnit, etDesc)
		);

		if (updateCount <0) {
			UI.showToast(getActivity(), "An error occurred  try again");
			return null;
		} else if (updateCount == 0) {
			//  TODO edit the item?
			UI.showToast(getActivity(), itemName + " not updated try again");
			return null;
		} else if (updateCount > 1) {
			//  TODO log this event
			throw new RuntimeException("Unexpected scenario");
		}

		String toastMessage =  itemName + " successfully updated";

		if (categoryName.isEmpty()) {
			categoryName = CategoryEntry.DEFAULT_NAME;
			toastMessage += " but placed";
		}

		toastMessage += " in the " + categoryName + " category";
		UI.showToast(getActivity(), toastMessage);
		return packageResultIntent(categoryID, categoryName);
	}

	private void populateFields(Cursor itemCursor) {

		itemCursor.moveToFirst();
		RadioButton rgLastsDays = (RadioButton) mRootView.findViewById(R.id.lastsDays);
		RadioButton rgLastsWeeks = (RadioButton) mRootView.findViewById(R.id.lastsWeeks);
		RadioButton rgLastsMonths = (RadioButton) mRootView.findViewById(R.id.lastsMonths);
		RadioButton rgLastsYears = (RadioButton) mRootView.findViewById(R.id.lastsYears);

		autoTvItemName.setText(itemCursor.getString(itemCursor.getColumnIndex(ItemEntry.COLUMN_NAME)));
		etUnitPrice.setText(itemCursor.getString(itemCursor.getColumnIndex(ItemEntry.COLUMN_PRICE)));
		etQuantity.setText(itemCursor.getString(itemCursor.getColumnIndex(ItemEntry.COLUMN_QUANTITY)));
		etActualMeasUnit.setText(itemCursor.getString(itemCursor.getColumnIndex(ItemEntry.COLUMN_MEAS_UNIT)));
		etLastsFor.setText(itemCursor.getString(itemCursor.getColumnIndex(ItemEntry.COLUMN_LASTS_FOR_UNIT)));
		etDesc.setText(itemCursor.getString(itemCursor.getColumnIndex(ItemEntry.COLUMN_DESC)));

		processDependentViewVisibility(etActualMeasUnit);

		UI.hideKeyboard(getActivity(), autoTvCategoryName);

		String lastsForUnit = itemCursor.getString(itemCursor.getColumnIndex(ItemEntry.COLUMN_LASTS_FOR));
		if (lastsForUnit.equals(getString(R.string.lasts_day_text))) {
			rgLastsDays.setChecked(true);
		} else if  (lastsForUnit.equals(getString(R.string.lasts_week_text))) {
			rgLastsWeeks.setChecked(true);
		} else if  (lastsForUnit.equals(getString(R.string.lasts_month_text))) {
			rgLastsMonths.setChecked(true);
		} else if  (lastsForUnit.equals(getString(R.string.lasts_year_text))) {
			rgLastsYears.setChecked(true);
		}
	}

}
