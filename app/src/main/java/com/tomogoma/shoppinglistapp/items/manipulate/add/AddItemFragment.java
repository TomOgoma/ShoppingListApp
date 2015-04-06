package com.tomogoma.shoppinglistapp.items.manipulate.add;

import android.content.Intent;
import android.os.Bundle;

import com.tomogoma.shoppinglistapp.R;
import com.tomogoma.shoppinglistapp.data.DBUpdateHelper;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.CategoryEntry;
import com.tomogoma.shoppinglistapp.data.Item;
import com.tomogoma.shoppinglistapp.items.manipulate.ManipulateItemFragment;
import com.tomogoma.shoppinglistapp.util.UI;

public class AddItemFragment extends ManipulateItemFragment {

	public static final String EXTRA_boolean_FILL_CATEGORY_FIELD =
			AddItemFragment.class.getName() + "_extra.fill.category.field";

	private boolean mFillCategoryField = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		mFillCategoryField = (getArguments() == null) || getArguments()
				.getBoolean(EXTRA_boolean_FILL_CATEGORY_FIELD, true);
	}

	@Override
	protected void setCategoryNameField(String categoryName) {
		if (mFillCategoryField) {
			super.setCategoryNameField(categoryName);
		}
	}

	protected Intent processInput(String categoryName, String itemName) {

		long categoryID = DBUpdateHelper.addCategory(getActivity(), categoryName);

		if (itemName.isEmpty()) {
			String message = getString(R.string.toast_category_created);
			UI.showKeyboardToast(getActivity(), String.format(message, categoryName));
			return packageResultIntent(categoryID, categoryName);
		}

		long itemID = DBUpdateHelper.addItem(
				getActivity(),
				new Item(categoryID, mLastsForUnit, mAutoTvItemName, mEtUnitPrice,
				         mEtQuantity, mEtLastsFor, mEtActualMeasUnit, mEtDesc)
		);

		if (itemID == -1) {
			//  TODO edit the item?
			String message = getString(R.string.error_already_exists);
			UI.showKeyboardToast(getActivity(), String.format(message, itemName));
			return null;
		}

		String butPlaced = "";
		if (categoryName.isEmpty()) {
			categoryName = CategoryEntry.DEFAULT_NAME;
			butPlaced = getString(R.string.toast_but_placed);
		}

		String toastMessage =  String.format(getString(R.string.toast_item_created), itemName, butPlaced, categoryName);
		UI.showKeyboardToast(getActivity(), toastMessage);
		return packageResultIntent(categoryID, categoryName);
	}

}
