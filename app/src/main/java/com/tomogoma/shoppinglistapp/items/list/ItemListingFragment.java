package com.tomogoma.shoppinglistapp.items.list;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ListView;

import com.tomogoma.shoppinglistapp.R;
import com.tomogoma.shoppinglistapp.data.ContentLoader;
import com.tomogoma.shoppinglistapp.data.ContentLoader.OnLoadFinishedListener;
import com.tomogoma.shoppinglistapp.data.Currency;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.CategoryEntry;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.CurrencyEntry;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.ItemEntry;
import com.tomogoma.shoppinglistapp.items.list.ItemListAdapter.OnSelectionRetrievedListener;
import com.tomogoma.shoppinglistapp.items.manipulate.edit.EditItemActivity;
import com.tomogoma.shoppinglistapp.util.Preference;
import com.tomogoma.shoppinglistapp.util.UI;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by ogoma on 01/03/15.
 */
public class ItemListingFragment extends ListFragment
		implements OnLoadFinishedListener, OnSelectionRetrievedListener {

	private static final String LOG_TAG = ItemListingFragment.class.getSimpleName();

	private ItemListAdapter mItemsAdapter;
	private String mCategoryName;
	private ContentLoader mContentLoader;
	private long mCategoryID;
	private int mCurrencyLoaderID;
	private int mItemsLoaderID;
	private boolean mIsItemsLoaded;

	private ActionBarActivity mActivity;
	private ActionMode mSingleActionMode;
	private android.view.ActionMode mMultipleActionMode;
	private String mActionItemName;
	private long mActionItemID;
	private ActionMode.Callback mSingleActionModeCallback = new ActionMode.Callback() {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.single_item_context, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			mode.setTitle(mActionItemName);
			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

			switch (item.getItemId()) {
				case R.id.action_edit: {
					mode.finish();
					openEditActivity(mActionItemID);
					return true;
				}
				case R.id.action_delete: {
					mode.finish();
					try {
						performDelete(mActionItemID, mActionItemName, true);
					}catch (SQLException e) {
						Log.e(LOG_TAG, e.getMessage());
					}
					return true;
				}
				default: return false;
			}
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mSingleActionMode = null;
			updateSelection(-1);
		}
	};
	private MultiChoiceModeListener mMultiActionModeCallback = new MultiChoiceModeListener() {

		private final String PLACEHOLDER_NAME = "Item";

		private Menu menu;
		private ArrayList<Long> itemIDs;

		@Override
		public void onItemCheckedStateChanged(android.view.ActionMode mode,
		                                      int position, long id, boolean checked) {

			if (checked) {
				itemIDs.add(id);
			} else {
				itemIDs.remove(id);
			}

			int count = itemIDs.size();
			//  Cannot Edit when having more than one item at hand
			if (count == 2) {
				menu.removeItem(R.id.action_edit);
			}

			mode.setSubtitle("(" + count + " items)");
		}

		@Override
		public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.single_item_context, menu);
			this.menu = menu;
			mMultipleActionMode = mode;
			return true;
		}

		@Override
		public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
			mode.setTitle(mCategoryName);
			itemIDs = new ArrayList<>();
			return true;
		}

		@Override
		public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {

			switch(item.getItemId()) {

				case R.id.action_edit: {
					mode.finish();
					openEditActivity(itemIDs.get(0));
					return true;
				}
				case R.id.action_delete: {

					int failedCount = 0;
					for (long itemID : itemIDs) {
						try {
							performDelete(itemID, null, false);
						} catch (SQLException e) {
							Log.e(LOG_TAG, e.getMessage());
							failedCount++;
						}
					}
					String toastText;
					if (failedCount > 0) {
						toastText = getString(R.string.error_toast_db_multi_delete_fail);
					} else {
						toastText = getString(R.string.toast_successful_delete);
						toastText = String.format(toastText, PLACEHOLDER_NAME + "s");
					}
					UI.showToast(mActivity, toastText);
					mode.finish();
					return true;
				}
			}
			return false;
		}

		@Override
		public void onDestroyActionMode(android.view.ActionMode mode) {
			updateSelection(-1);
			mode.setSubtitle(null);
		}
	};

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = (ActionBarActivity) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setRetainInstance(true);

		Bundle arguments = getArguments();
		if (arguments == null) {
			Log.e(LOG_TAG, "Received null arguments; falling back to defaults");
			mCategoryID = CategoryEntry.DEFAULT_ID;
			mCategoryName = CategoryEntry.DEFAULT_NAME;
		} else {
			mCategoryID = arguments.getLong(ListingActivity.EXTRA_long_CATEGORY_ID,
			                                CategoryEntry.DEFAULT_ID);
			mCategoryName = arguments.getString(ListingActivity.EXTRA_String_CATEGORY_NAME,
			                                    CategoryEntry.DEFAULT_NAME);
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (savedInstanceState == null) {
			mItemsAdapter = new ItemListAdapter(getActivity());
			mItemsAdapter.setOnSelectionRetrievedListener(this);
			setListAdapter(mItemsAdapter);
			setEmptyText(getString(R.string.advice_empty_list));
			ListView listView = getListView();
			listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
			listView.setMultiChoiceModeListener(mMultiActionModeCallback);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		getActivity().setTitle(mCategoryName);

		String itemSortOrder = ItemEntry.TABLE_NAME + "." + ItemEntry.COLUMN_NAME + " ASC";
		Uri itemUri = ItemEntry.buildItemsInCategoryUri(mCategoryID, true);
		String[] itemProjection = ItemListAdapter.ITEMS_PROJECTION;

		long preferredCurrencyID = Preference.getPreferredCurrencyID(getActivity());
		Uri currencyUri = CurrencyEntry.buildCurrencyUri(preferredCurrencyID);
		String[] currencyProjection = new String[] {
				CurrencyEntry.COLUMN_SYMBOL,
				CurrencyEntry.COLUMN_CODE,
				CurrencyEntry.COLUMN_LAST_CONVERSION
		};

		mContentLoader = new ContentLoader(getActivity(), this);
		mContentLoader.setOnLoadFinishedListener(this);

		mCurrencyLoaderID = mContentLoader.loadContent(currencyUri, currencyProjection, null);
		mItemsLoaderID = mContentLoader.loadContent(itemUri, mItemsAdapter, itemProjection, itemSortOrder);

		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!mIsItemsLoaded) {
			setListShown(false);
		}
	}

	@Override
	public void onLoadFinished(int id) {

		if (id == mCurrencyLoaderID) {

			Currency currency;
			Cursor cursor = mContentLoader.getLoadedCursor(id);
			if (cursor.moveToFirst()) {

				double lastConversion = cursor.getDouble(cursor.getColumnIndex(CurrencyEntry.COLUMN_LAST_CONVERSION));
				String code = cursor.getString(cursor.getColumnIndex(CurrencyEntry.COLUMN_CODE));
				String symbol = cursor.getString(cursor.getColumnIndex(CurrencyEntry.COLUMN_SYMBOL));
				lastConversion = (lastConversion<=0)? 1d: lastConversion;
				currency = new Currency(code, symbol, null, null, lastConversion);
			}
			else {
				Log.e(LOG_TAG, "No currencies found, not even default; count=" + cursor.getCount());
				UI.showToast(getActivity(), getString(R.string.error_toast_fetch_preferred_currency_fail));
				currency = new Currency(
						CurrencyEntry.DEFAULT_CODE,
						CurrencyEntry.DEFAULT_SYMBOL,
						CurrencyEntry.DEFAULT_NAME,
						CurrencyEntry.DEFAULT_COUNTRY
				);
			}
			mItemsAdapter.setCurrency(currency);
			if (mIsItemsLoaded) {
				Log.i(LOG_TAG, "Items List loaded before currency, reloading...");
				mItemsAdapter.notifyDataSetChanged();
			}
		}
		else if (id == mItemsLoaderID) {
			mIsItemsLoaded = true;
			setListShown(true);
		}
	}

	@Override
	public void onListItemClick(ListView l, View view, int position, long id) {

		view.animate();
		if (position == mItemsAdapter.getSelectedPosition()) {
			if (mSingleActionMode != null) {
				mSingleActionMode.finish();
			}
		} else {
			updateSelection(position);
		}
	}

	@Override
	public void onSelectionRetrieved(long itemID, String name) {

		mActionItemID = itemID;
		mActionItemName = name;

		if (mSingleActionMode == null) {
			mSingleActionMode = mActivity.startSupportActionMode(mSingleActionModeCallback);
		}
		mSingleActionMode.invalidate();
	}

	@Override
	public void onStop() {
		if (mSingleActionMode !=null) {
			mSingleActionMode.finish();
		}
		if (mMultipleActionMode != null) {
			mMultipleActionMode.finish();
		}
		super.onStop();
	}

	private void updateSelection(int position) {

		mItemsAdapter.setSelectedPosition(position);
		//  TODO figure out how to use the following instead of re-querying the db:
		//	mItemsAdapter.getView(position, null, parent);
		//  passing null (instead of view) to ensure that newView() is called in order
		//  to re-inflate the view with the new layout.
		//  tried it debugged it, logged it -> goes through all expected method calls but
		//  does not update the actual view, Bummer!
		mItemsAdapter.notifyDataSetChanged();
	}

	private void openEditActivity(long itemID) {

		Bundle args = new Bundle();
		args.putString(ListingActivity.EXTRA_String_CATEGORY_NAME, mCategoryName);
		args.putLong(ListingActivity.EXTRA_long_CATEGORY_ID, mCategoryID);

		Intent editItemActivityIntent = new Intent(getActivity(), EditItemActivity.class);
		editItemActivityIntent.putExtra(ListingActivity.EXTRA_long_ITEM_ID, itemID);
		editItemActivityIntent.putExtra(EditItemActivity.EXTRA_Class_CALLING_ACTIVITY, getActivity().getClass());
		editItemActivityIntent.putExtra(ListingActivity.EXTRA_Bundle_CATEGORY_DETAILS, args);
		startActivity(editItemActivityIntent);
	}

	private void performDelete(long itemID, String itemName, boolean showToasts) throws SQLException {
		String whereClause = ItemEntry._ID + " = ?";
		String[] whereArgs = new String[] {String.valueOf(itemID)};
		ContentResolver contentResolver = getActivity().getContentResolver();
		int count = contentResolver.delete(ItemEntry.CONTENT_URI, whereClause, whereArgs);
		//  TODO do not delete immediately, archive and allow undo
		if (count==0) {
			if (showToasts) {
				UI.showToast(getActivity(), getActivity().getString(R.string.error_toast_db_delete_fail));
			}
			throw new SQLException("Failed to delete item in db; got update count of 0");
		} else if (count>1) {
			if (showToasts) {
				UI.showToast(getActivity(), getActivity().getString(R.string.error_toast_db_potential_data_corruption));
			}
			throw new SQLException("Deleted more than one items in a one-item-delete scenario;" +
					                       " total recorded deletes: " + count);
		} else if (showToasts) {
			String successfulMessage = getString(R.string.toast_successful_delete);
			UI.showToast(getActivity(), String.format(successfulMessage, itemName));
		}
	}

}
