package com.tomogoma.shoppinglistapp.items.list;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.tomogoma.shoppinglistapp.R;
import com.tomogoma.shoppinglistapp.ShoppingListAppActivity;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.CategoryEntry;
import com.tomogoma.shoppinglistapp.items.manipulate.add.AddItemActivity;


public abstract class ListingActivity extends ShoppingListAppActivity {

	public static final String EXTRA_Bundle_CATEGORY_DETAILS =
			ListingActivity.class.getName() + "_extra.category.details";
	public static final String EXTRA_long_CATEGORY_ID =
			ListingActivity.class.getName() + "_extra.category.id";
	public static final String EXTRA_String_CATEGORY_NAME =
			ListingActivity.class.getName() + "_extra.category.name";
	public static final String EXTRA_long_ITEM_ID =
			ListingActivity.class.getName() + "_extra.item.id";

	//private static final String LOG_TAG = ListingActivity.class.getName();
	private static final int ADD_ITEM_REQ_CODE = 5034;

	protected boolean mIsResumingFromAddItemResult = false;
	protected long mCategoryID = CategoryEntry.DEFAULT_ID;
	protected String mCategoryName = CategoryEntry.DEFAULT_NAME;
	protected Bundle mArguments;

	/**
	 * The activity is resuming and requesting that the fragment listing the its items
	 * be reloaded.
	 * @param arguments
	 */
	protected abstract void showItems(Bundle arguments);

	/**
	 * Override this to add extras to your add Item intent for the
	 * {@link com.tomogoma.shoppinglistapp.items.manipulate.add.AddItemActivity}.
	 * @param addItemIntent
	 */
	protected void modifyAddItemIntent(Intent addItemIntent) {}

	@Override
	protected void onSuperCreate(Bundle savedInstanceState) {

		if (savedInstanceState == null) {
			mArguments = getIntent().getBundleExtra(EXTRA_Bundle_CATEGORY_DETAILS);
		} else {
			mArguments = savedInstanceState.getBundle(EXTRA_Bundle_CATEGORY_DETAILS);
		}

		if (mArguments != null) {
			mCategoryID = mArguments.getLong(EXTRA_long_CATEGORY_ID, CategoryEntry.DEFAULT_ID);
			mCategoryName = mArguments.getString(EXTRA_String_CATEGORY_NAME,  CategoryEntry.DEFAULT_NAME);
		} else {
			packageCategoryDetails();
		}
	}

	@Override
	protected void onResume() {

		super.onResume();
		if (mIsResumingFromAddItemResult) {
			packageCategoryDetails();
			showItems(mArguments);
			mIsResumingFromAddItemResult = false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.items, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case R.id.action_add: {

				Intent addItemIntent = new Intent(this, AddItemActivity.class);
				addItemIntent.putExtra(EXTRA_Bundle_CATEGORY_DETAILS, mArguments);
				addItemIntent.putExtra(EXTRA_Class_CALLING_ACTIVITY, getClass());
				modifyAddItemIntent(addItemIntent);
				startAddItemActivityForResult(addItemIntent);
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void addToUpActionIntent(Intent upIntent) {
		upIntent.putExtra(EXTRA_Bundle_CATEGORY_DETAILS, mArguments);
	}

	@Override
	protected void addToSettingsActivityIntent(Intent settingsIntent) {
		settingsIntent.putExtra(EXTRA_Bundle_CATEGORY_DETAILS, mArguments);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBundle(EXTRA_Bundle_CATEGORY_DETAILS, mArguments);
		super.onSaveInstanceState(outState);
	}

	protected void startAddItemActivityForResult(Intent addItemIntent) {
		startActivityForResult(addItemIntent, ADD_ITEM_REQ_CODE);
	}

	protected Bundle packageCategoryDetails(long categoryID, String categoryName) {

		mCategoryID = categoryID;
		mCategoryName = categoryName;
		packageCategoryDetails();
		return mArguments;
	}

	protected void packageCategoryDetails() {

		mArguments = new Bundle();
		mArguments.putString(EXTRA_String_CATEGORY_NAME, mCategoryName);
		mArguments.putLong(EXTRA_long_CATEGORY_ID, mCategoryID);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == ADD_ITEM_REQ_CODE) {

			if (resultCode == RESULT_OK) {

				//  ...to be continued in onResume (cannot replace fragment before that)
				mCategoryName = data.getStringExtra(EXTRA_String_CATEGORY_NAME);
				mCategoryID = data.getLongExtra(EXTRA_long_CATEGORY_ID, 1);
				mIsResumingFromAddItemResult = true;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}