package com.tomogoma.shoppinglistapp.items.list;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.tomogoma.shoppinglistapp.R;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.CategoryEntry;
import com.tomogoma.shoppinglistapp.items.add.AddItemActivity;
import com.tomogoma.shoppinglistapp.items.list.CategoriesFragment.OnCategorySelectedListener;
import com.tomogoma.util.ui.UIUtils;


public class AllItemsActivity extends ActionBarActivity implements OnCategorySelectedListener {

	private static final int ADD_ITEM_REQ_CODE = 5034;

	private Fragment mActiveFragment;
	private boolean mIsTwoPane = false;
	private boolean mIsResumingFromAddItemResult = false;
	private long mCategoryID = CategoryEntry.DEFAULT_CATEGORY_ID;
	private String mCategoryName = CategoryEntry.DEFAULT_CATEGORY_NAME;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_all_items);
		if (findViewById(R.id.detailsContainer) != null) {
			mIsTwoPane = true;
		}

		if (savedInstanceState == null) {

			Fragment categoriesFragment = packageCategoriesFragment();
			addFragment(categoriesFragment);
			if (mIsTwoPane) {
				addFragment(new ItemsFragment());
			}
		}
	}

	@Override
	protected void onResume() {

		super.onResume();

		if (mIsResumingFromAddItemResult) {

			startItemsFragment();
			mIsResumingFromAddItemResult = false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.items, menu);
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case R.id.action_settings:
				//  TODO implement settings activity
				return true;
			case R.id.action_add:
				String categoryName = mCategoryName;
				Intent addItemIntent = new Intent(this, AddItemActivity.class);
				addItemIntent.putExtra(AddItemActivity.EXTRA_String_CATEGORY_NAME, categoryName);
				startActivityForResult(addItemIntent, ADD_ITEM_REQ_CODE);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {

		//  TODO what if mActiveFragment == null due to screen rotation && user is in categories frag?
		//  TODO user would have to press back one extra time in order to leave activity
		if (mActiveFragment instanceof CategoriesFragment) {
			super.onBackPressed();
			return;
		}

		Fragment categoriesFragment = packageCategoriesFragment();
		replaceFragment(categoriesFragment);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == ADD_ITEM_REQ_CODE) {

			if (resultCode == RESULT_OK) {

				//  ...to be continued in onResume (cannot replace fragment before that)
				mCategoryName = data.getStringExtra(AddItemActivity.EXTRA_String_CATEGORY_NAME);
				mCategoryID = data.getLongExtra(AddItemActivity.EXTRA_long_CATEGORY_ID, 1);
				mIsResumingFromAddItemResult = true;

				if (mIsTwoPane) {
					CategoriesFragment categoriesFragment = (CategoriesFragment)
							getSupportFragmentManager()
									.findFragmentByTag(CategoriesFragment.class.getName());
					categoriesFragment.updateCategoryID(mCategoryID);
				}
			} else {
				//  TODO act on bad result
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onCategorySelected(long categoryID, String categoryName) {

		mCategoryID = categoryID;
		mCategoryName = categoryName;
		startItemsFragment();
	}

	private void addFragment(Fragment fragment) {

		int containerID = getReplaceContainerID(fragment);
		getSupportFragmentManager()
				.beginTransaction()
				.add(containerID, fragment, fragment.getClass().getName())
				.commit();
		setActiveFragment(containerID, fragment);
	}

	protected void replaceFragment(Fragment withFragment) {

		UIUtils.hideKeyboard(this, getCurrentFocus());
		int containerID = getReplaceContainerID(withFragment);
		setTitle(getResources().getString(R.string.app_name));
		getSupportFragmentManager()
				.beginTransaction()
				.replace(containerID, withFragment, withFragment.getClass().getName())
				.commit();
		setActiveFragment(containerID, withFragment);
	}

	private void startItemsFragment() {

		Bundle arguments = new Bundle();
		arguments.putString(ItemsFragment.EXTRA_String_CATEGORY_NAME, mCategoryName);
		arguments.putLong(ItemsFragment.EXTRA_long_CATEGORY_ID, mCategoryID);

		Fragment itemsFragment = new ItemsFragment();
		itemsFragment.setArguments(arguments);
		replaceFragment(itemsFragment);
	}

	private CategoriesFragment packageCategoriesFragment() {

		CategoriesFragment categoriesFragment = new CategoriesFragment();
		Bundle arguments = new Bundle();
		arguments.putLong(CategoriesFragment.EXTRA_long_CATEGORY_ID, mCategoryID);
		categoriesFragment.setArguments(arguments);
		return categoriesFragment;
	}

	private void setActiveFragment (int containerID, Fragment fragment) {

		if (containerID == R.id.selectionContainer) {
			mActiveFragment = fragment;
		}
	}

	private int getReplaceContainerID(Fragment replaceFragment) {

		int containerID;
		//  instance of details fragment goes into the details container in two pane mode
		if (mIsTwoPane) {

			if (replaceFragment instanceof ItemsFragment) {
				containerID = R.id.detailsContainer;
			} else {
				containerID = R.id.selectionContainer;
			}
		}
		//  The selection container for single-pane mode
		else {
			containerID = R.id.selectionContainer;
		}

		return containerID;
	}

}