package com.tomogoma.shoppinglistapp.items.list;

import android.os.Bundle;

import com.tomogoma.shoppinglistapp.R;


public class ItemListingActivity extends ItemsActivity {

	@Override
	protected Class<?> getParentActivity() {
		return CategoryListingActivity.class;
	}

	@Override
	protected void onSuperCreate(Bundle savedInstanceState) {

		super.onSuperCreate(savedInstanceState);
		setContentView(R.layout.activity_all_items);

		if (savedInstanceState == null) {
			ItemsFragment itemsFragment = new ItemsFragment();
			itemsFragment.setArguments(packageCategoryDetails());
			addFragment(R.id.selectionContainer, itemsFragment);
		}
	}

	@Override
	protected void showItems(Bundle arguments) {

		ItemsFragment itemsFragment = new ItemsFragment();
		itemsFragment.setArguments(arguments);
		replaceFragment(R.id.selectionContainer, itemsFragment);
	}
}