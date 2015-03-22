package com.tomogoma.shoppinglistapp.items.manipulate.add;

import android.content.Intent;
import android.os.Bundle;

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
			UI.showToast(getActivity(), "Category: " + categoryName + " is now in place");
			return packageResultIntent(categoryID, categoryName);
		}

		long itemID = DBUpdateHelper.addItem(
				getActivity(),
				new Item(categoryID, mLastsForUnit, autoTvItemName, etUnitPrice,
				         etQuantity, etLastsFor, etActualMeasUnit, etDesc)
		);

		if (itemID == -1) {
			//  TODO edit the item?
			UI.showToast(getActivity(), itemName + " already exists");
			return null;
		}

		String toastMessage =  itemName + " successfully created";

		if (categoryName.isEmpty()) {
			categoryName = CategoryEntry.DEFAULT_NAME;
			toastMessage += " but placed";
		}

		toastMessage += " in the " + categoryName + " category";
		UI.showToast(getActivity(), toastMessage);
		return packageResultIntent(categoryID, categoryName);
	}

}
