package com.tomogoma.shoppinglistapp.items.list;

import android.content.Intent;
import android.os.Bundle;

import com.tomogoma.shoppinglistapp.R;
import com.tomogoma.shoppinglistapp.items.manipulate.ManipulateItemActivity;

public class ItemListingActivity extends ListingActivity {

	@Override
	protected Class<?> getParentActivity() {
		return CategoryListingActivity.class;
	}

	@Override
	protected void onSuperCreate(Bundle savedInstanceState) {

		super.onSuperCreate(savedInstanceState);
		setContentView(R.layout.activity_all_items);

		if (savedInstanceState == null) {
			ItemListingFragment itemListingFragment = new ItemListingFragment();
			itemListingFragment.setArguments(packageCategoryDetails());
			addFragment(R.id.selectionContainer, itemListingFragment);
		}
	}

	@Override
	protected void modifyAddItemIntent(Intent addItemIntent) {
		mArguments.putSerializable(ManipulateItemActivity.EXTRA_Class_CALLING_ACTIVITY, this.getClass());
		addItemIntent.putExtra(ListingActivity.EXTRA_Bundle_CATEGORY_DETAILS, mArguments);
	}

	@Override
	protected void showItems(Bundle arguments) {

		ItemListingFragment itemListingFragment = new ItemListingFragment();
		itemListingFragment.setArguments(arguments);
		replaceFragment(R.id.selectionContainer, itemListingFragment);
	}
}