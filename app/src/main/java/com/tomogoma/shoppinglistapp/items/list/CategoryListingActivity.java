package com.tomogoma.shoppinglistapp.items.list;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.tomogoma.shoppinglistapp.R;
import com.tomogoma.shoppinglistapp.items.list.CategoriesFragment.OnCategorySelectedListener;


public class CategoryListingActivity extends ItemsActivity implements OnCategorySelectedListener {

	private boolean mIsTwoPane = false;

	@Override
	protected Class<?> getParentActivity() {
		return null;
	}

	@Override
	protected void onSuperCreate(Bundle savedInstanceState) {

		super.onSuperCreate(savedInstanceState);
		setContentView(R.layout.activity_all_items);

		if (findViewById(R.id.detailsContainer) != null) {
			mIsTwoPane = true;
		}

		if (savedInstanceState == null) {

			CategoriesFragment categoriesFragment = new CategoriesFragment();
			Bundle arguments = new Bundle();
			arguments.putLong(CategoriesFragment.EXTRA_long_CATEGORY_ID, mCategoryID);
			categoriesFragment.setArguments(arguments);
			addFragment(R.id.selectionContainer, categoriesFragment);

			if (mIsTwoPane) {
				addFragment(R.id.detailsContainer, new ItemsFragment());
			}
		}
	}

	@Override
	public void onCategorySelected(long categoryID, String categoryName) {

		Bundle categoryDetails = packageCategoryDetails(categoryID, categoryName);
		showItems(categoryDetails);
	}

	@Override
	protected void showItems(Bundle arguments) {

		if(mIsTwoPane) {
			showItemsForCategory(arguments);
		} else {
			startItemListingActivity(arguments);
		}
	}

	protected void startItemListingActivity(Bundle arguments) {

		Intent itemListingActivity = new Intent(this, ItemListingActivity.class);
		itemListingActivity.putExtra(ItemListingActivity.EXTRA_Bundle_CATEGORY_DETAILS, arguments);
		startActivity(itemListingActivity);
	}

	private void showItemsForCategory(Bundle arguments) {

		Fragment itemsFragment = new ItemsFragment();
		itemsFragment.setArguments(arguments);
		replaceFragment(R.id.detailsContainer, itemsFragment);
	}

}