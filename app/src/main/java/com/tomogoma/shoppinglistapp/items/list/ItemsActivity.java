package com.tomogoma.shoppinglistapp.items.list;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.tomogoma.shoppinglistapp.R;
import com.tomogoma.shoppinglistapp.ShoppingListAppActivity;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.CategoryEntry;
import com.tomogoma.shoppinglistapp.items.manipulate.add.AddItemActivity;


public abstract class ItemsActivity extends ShoppingListAppActivity {

	public static final String EXTRA_Bundle_CATEGORY_DETAILS = ItemsActivity.class.getName() + "_extra.category.details";

	private static final int ADD_ITEM_REQ_CODE = 5034;

	protected boolean mFillCategorySectionOnAdd = true;
	protected boolean mIsResumingFromAddItemResult = false;
	protected long mCategoryID = CategoryEntry.DEFAULT_ID;
	protected String mCategoryName = CategoryEntry.DEFAULT_NAME;

	protected abstract void showItems(Bundle arguments);

	@Override
	protected void onSuperCreate(Bundle savedInstanceState) {

		Bundle arguments = getIntent().getBundleExtra(EXTRA_Bundle_CATEGORY_DETAILS);
		Log.d(getClass().getSimpleName(), "Arguments is empty? " + (arguments == null));
		if (arguments != null) {
			mCategoryID = arguments.getLong(ItemsFragment.EXTRA_long_CATEGORY_ID,
			                                CategoryEntry.DEFAULT_ID);
			mCategoryName = arguments.getString(ItemsFragment.EXTRA_String_CATEGORY_NAME,
			                                    CategoryEntry.DEFAULT_NAME);
			Log.d(getClass().getSimpleName(), "Got category name: " + mCategoryName);
		}
	}

	@Override
	protected void onResume() {

		super.onResume();
		if (mIsResumingFromAddItemResult) {
			showItems(packageCategoryDetails());
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

				String categoryName = mCategoryName;
				Intent addItemIntent = new Intent(this, AddItemActivity.class);
				addItemIntent.putExtra(AddItemActivity.EXTRA_String_CATEGORY_NAME, categoryName);
				startAddItemActivityForResult(addItemIntent);
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	protected void startAddItemActivityForResult(Intent addItemIntent) {
		startActivityForResult(addItemIntent, ADD_ITEM_REQ_CODE);
	}

	protected Bundle packageCategoryDetails(long categoryID, String categoryName) {

		mCategoryID = categoryID;
		mCategoryName = categoryName;
		return packageCategoryDetails();
	}

	protected Bundle packageCategoryDetails() {

		Bundle arguments = new Bundle();
		arguments.putString(ItemsFragment.EXTRA_String_CATEGORY_NAME, mCategoryName);
		arguments.putLong(ItemsFragment.EXTRA_long_CATEGORY_ID, mCategoryID);
		return arguments;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == ADD_ITEM_REQ_CODE) {

			if (resultCode == RESULT_OK) {

				//  ...to be continued in onResume (cannot replace fragment before that)
				mCategoryName = data.getStringExtra(AddItemActivity.EXTRA_String_CATEGORY_NAME);
				mCategoryID = data.getLongExtra(AddItemActivity.EXTRA_long_CATEGORY_ID, 1);
				mIsResumingFromAddItemResult = true;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}