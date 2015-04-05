package com.tomogoma.shoppinglistapp.items.list;

import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.tomogoma.shoppinglistapp.R;
import com.tomogoma.shoppinglistapp.ShoppingListAppActivity;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.CategoryEntry;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.ItemEntry;
import com.tomogoma.shoppinglistapp.items.list.ItemListingFragment.OnRequestPopulateActionsListener;
import com.tomogoma.shoppinglistapp.items.manipulate.add.AddItemActivity;
import com.tomogoma.shoppinglistapp.items.manipulate.edit.EditItemActivity;
import com.tomogoma.shoppinglistapp.util.UI;


public abstract class ListingActivity extends ShoppingListAppActivity
		implements OnRequestPopulateActionsListener {

	public static final String EXTRA_Bundle_CATEGORY_DETAILS =
			ListingActivity.class.getName() + "_extra.category.details";
	public static final String EXTRA_long_CATEGORY_ID =
			ListingActivity.class.getName() + "_extra.category.id";
	public static final String EXTRA_String_CATEGORY_NAME =
			ListingActivity.class.getName() + "_extra.category.name";
	public static final String EXTRA_long_ITEM_ID =
			ListingActivity.class.getName() + "_extra.item.id";

	protected boolean mIsResumingFromAddItemResult = false;
	protected long mCategoryID = CategoryEntry.DEFAULT_ID;
	protected String mCategoryName = CategoryEntry.DEFAULT_NAME;
	protected Bundle mArguments;

	private static final String LOG_TAG = ListingActivity.class.getName();
	private static final int ADD_ITEM_REQ_CODE = 5034;
	private static final int EDIT_MENU_ITEM_ID = 5023;
	private static final int DELETE_MENU_ITEM_ID = 5024;

	private ActionMode mActionMode;
	private Menu mMenu;
	private String mActionItemName;
	private long mActionItemID;

	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.single_item_context, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
				case R.id.action_edit: {
					removeSelectionIcons();
					openEditActivity();
					return true;
				}
				case R.id.action_delete: {
					removeSelectionIcons();
					performDelete();
					return true;
				}
				default:
					return false;
			}
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
		}
	};

	protected abstract void showItems(Bundle arguments);

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
			showItems(packageCategoryDetails());
			mIsResumingFromAddItemResult = false;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBundle(EXTRA_Bundle_CATEGORY_DETAILS, mArguments);
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.items, menu);
		mMenu = menu;
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case R.id.action_add: {

				Bundle args = packageCategoryDetails();
				Intent addItemIntent = new Intent(this, AddItemActivity.class);
				addItemIntent.putExtra(EXTRA_Bundle_CATEGORY_DETAILS, args);
				modifyAddItemIntent(addItemIntent);
				startAddItemActivityForResult(addItemIntent);
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onRequestPopulateActions(long itemID, String itemName) {

		mActionItemID = itemID;
		mActionItemName = itemName;

		if (mActionMode == null) {
			mActionMode = startSupportActionMode(mActionModeCallback);
		}
	}

	@Override
	public void onRequestDepopulateActions() {
		mActionMode.finish();
	}

	/**
	 * Override this to add extras to your add Item intent for the
	 * {@link com.tomogoma.shoppinglistapp.items.manipulate.add.AddItemActivity}.
	 * @param addItemIntent
	 */
	protected void modifyAddItemIntent(Intent addItemIntent) {}

	protected void startAddItemActivityForResult(Intent addItemIntent) {
		startActivityForResult(addItemIntent, ADD_ITEM_REQ_CODE);
	}

	protected Bundle packageCategoryDetails(long categoryID, String categoryName) {

		mCategoryID = categoryID;
		mCategoryName = categoryName;
		return packageCategoryDetails();
	}

	protected Bundle packageCategoryDetails() {

		mArguments = new Bundle();
		mArguments.putString(EXTRA_String_CATEGORY_NAME, mCategoryName);
		mArguments.putLong(EXTRA_long_CATEGORY_ID, mCategoryID);
		return mArguments;
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

	private void openEditActivity() {

		Bundle args = new Bundle();
		args.putString(ListingActivity.EXTRA_String_CATEGORY_NAME, mCategoryName);
		args.putLong(ListingActivity.EXTRA_long_CATEGORY_ID, mCategoryID);
		args.putSerializable(EditItemActivity.EXTRA_Class_CALLING_ACTIVITY, getClass());

		Intent editItemActivityIntent = new Intent(this, EditItemActivity.class);
		editItemActivityIntent.putExtra(ListingActivity.EXTRA_long_ITEM_ID, mActionItemID);
		editItemActivityIntent.putExtra(ListingActivity.EXTRA_Bundle_CATEGORY_DETAILS, args);
		startActivity(editItemActivityIntent);
	}

	private void performDelete() {
		String whereClause = ItemEntry._ID + " = ?";
		String[] whereArgs = new String[] {String.valueOf(mActionItemID)};
		ContentResolver contentResolver = getContentResolver();
		int count = contentResolver.delete(ItemEntry.CONTENT_URI, whereClause, whereArgs);
		//  TODO do not delete immediately, archive and allow undo
		if (count==0) {
			UI.showToast(this, getString(R.string.error_toast_db_delete_fail));
		} else if (count>1) {
			UI.showToast(this, getString(R.string.error_toast_db_potential_data_corruption));
		} else {
			String successfulMessage = getString(R.string.toast_successful_delete);
			UI.showToast(this, String.format(successfulMessage, mActionItemName));
		}
	}

	private void removeSelectionIcons() {
		mMenu.removeItem(EDIT_MENU_ITEM_ID);
		mMenu.removeItem(DELETE_MENU_ITEM_ID);
	}
}