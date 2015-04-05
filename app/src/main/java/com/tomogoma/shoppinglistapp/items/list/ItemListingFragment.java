package com.tomogoma.shoppinglistapp.items.list;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.tomogoma.shoppinglistapp.R;
import com.tomogoma.shoppinglistapp.data.ContentLoader;
import com.tomogoma.shoppinglistapp.data.ContentLoader.OnLoadFinishedListener;
import com.tomogoma.shoppinglistapp.data.Currency;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.CategoryEntry;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.CurrencyEntry;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.ItemEntry;
import com.tomogoma.shoppinglistapp.items.list.ItemListAdapter.OnSelectionRetrievedListener;
import com.tomogoma.shoppinglistapp.util.Preference;
import com.tomogoma.shoppinglistapp.util.UI;

/**
 * Created by ogoma on 01/03/15.
 */
public class ItemListingFragment extends ListFragment
		implements OnLoadFinishedListener, OnSelectionRetrievedListener {

	private static final String LOG_TAG = ItemListingFragment.class.getSimpleName();

	private OnRequestPopulateActionsListener mOnItemSelectedCallback;
	private ItemListAdapter mItemsAdapter;
	private String mCategoryName;
	private ContentLoader mContentLoader;
	private long mCategoryID;
	private int mCurrencyLoaderID;
	private int mItemsLoaderID;
	private boolean mIsItemsLoaded;

	public interface OnRequestPopulateActionsListener {
		public void onRequestPopulateActions(long itemID, String itemName);
		public void onRequestDepopulateActions();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mOnItemSelectedCallback = (OnRequestPopulateActionsListener) activity;
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.default_list_layout, container, false);
		if (savedInstanceState == null) {
			mItemsAdapter = new ItemListAdapter(getActivity());
			mItemsAdapter.setOnSelectionIDRetrievedListener(this);
			setListAdapter(mItemsAdapter);
		}
		return rootView;
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
	public void onListItemClick(ListView l, View view, int position, long id) {

		if (position == mItemsAdapter.getSelectedPosition()) {
			mItemsAdapter.setSelectedPosition(-1);
			mOnItemSelectedCallback.onRequestDepopulateActions();
		} else {
			mItemsAdapter.setSelectedPosition(position);
		}

		//  TODO figure out how to use the following instead of re-querying the db:
//		mItemsAdapter.getView(position, null, parent);
		//  passing null (instead of view) to ensure that newView() is called in order
		//  to re-inflate the view with the new layout.
		//  tried it debugged it, logged it -> goes through all expected method calls but
		//  does not update the actual view, Bummer!
		mItemsAdapter.notifyDataSetChanged();
	}

	@Override
	public void onSelectionRetrieved(long itemID, String name) {
		mOnItemSelectedCallback.onRequestPopulateActions(itemID, name);
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
		}
	}

}
