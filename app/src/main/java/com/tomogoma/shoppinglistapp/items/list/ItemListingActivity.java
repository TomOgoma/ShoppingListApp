package com.tomogoma.shoppinglistapp.items.list;

import android.os.Bundle;

import com.tomogoma.shoppinglistapp.R;

public class ItemListingActivity extends ListingActivity {

	//private static final String LOG_TAG = ItemListingActivity.class.getName();

	@Override
	protected Class<?> getParentActivity() {
		return CategoryListingActivity.class;
	}

	@Override
	protected void onSuperCreate(Bundle savedInstanceState) {

		super.onSuperCreate(savedInstanceState);
		setContentView(R.layout.activity_all_items);

		if (savedInstanceState == null) {
			packageCategoryDetails();
			ItemListingFragment itemListingFragment = new ItemListingFragment();
			itemListingFragment.setArguments(mArguments);
			addFragment(R.id.selectionContainer, itemListingFragment);
		}
	}

	@Override
	protected void showItems(Bundle arguments) {

		ItemListingFragment itemListingFragment = new ItemListingFragment();
		itemListingFragment.setArguments(arguments);
		replaceFragment(R.id.selectionContainer, itemListingFragment);
	}
}