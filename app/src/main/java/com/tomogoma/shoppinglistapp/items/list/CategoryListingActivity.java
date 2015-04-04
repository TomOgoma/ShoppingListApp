package com.tomogoma.shoppinglistapp.items.list;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.tomogoma.shoppinglistapp.R;
import com.tomogoma.shoppinglistapp.items.list.CategoryListingFragment.OnCategorySelectedListener;
import com.tomogoma.shoppinglistapp.items.manipulate.add.AddItemActivity;


public class CategoryListingActivity extends ListingActivity implements OnCategorySelectedListener {

	private static final String SAVED_STATE_IS_CATEGORY_SELECTED = "saved.is.category.selected";

	private boolean mIsTwoPane = false;
	private boolean mIsCategorySelected = false;

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

			CategoryListingFragment categoryListingFragment = new CategoryListingFragment();
			mArguments.putBoolean(CategoryListingFragment.EXTRA_boolean_IS_ONLY_PANE, !mIsTwoPane);
			categoryListingFragment.setArguments(mArguments);
			addFragment(R.id.selectionContainer, categoryListingFragment);

			if (mIsTwoPane) {
				ItemListingFragment itemListingFragment = new ItemListingFragment();
				itemListingFragment.setArguments(mArguments);
				addFragment(R.id.detailsContainer, itemListingFragment);
			}
		}
		else {
			mIsCategorySelected = savedInstanceState.getBoolean(SAVED_STATE_IS_CATEGORY_SELECTED, false);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {

		outState.putBoolean(SAVED_STATE_IS_CATEGORY_SELECTED, mIsCategorySelected);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onCategorySelected(long categoryID, String categoryName) {

		Bundle categoryDetails = packageCategoryDetails(categoryID, categoryName);
		showItems(categoryDetails);
		if (mIsTwoPane) {
			mIsCategorySelected = true;
		}
	}

	@Override
	protected void showItems(Bundle arguments) {

		if(mIsTwoPane) {
			showItemsForCategory(arguments);
		} else {
			startItemListingActivity(arguments);
		}
	}

	@Override
	protected void startAddItemActivityForResult(Intent addItemIntent) {
		addItemIntent.putExtra(AddItemActivity.EXTRA_boolean_FILL_CATEGORY_FIELD, mIsCategorySelected);
		super.startAddItemActivityForResult(addItemIntent);
	}

	protected void startItemListingActivity(Bundle arguments) {

		Intent itemListingActivity = new Intent(this, ItemListingActivity.class);
		itemListingActivity.putExtra(ItemListingActivity.EXTRA_Bundle_CATEGORY_DETAILS, arguments);
		startActivity(itemListingActivity);
	}

	private void showItemsForCategory(Bundle arguments) {

		Fragment itemsFragment = new ItemListingFragment();
		itemsFragment.setArguments(arguments);
		replaceFragment(R.id.detailsContainer, itemsFragment);
	}

}