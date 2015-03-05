package com.tomogoma.shoppinglistapp;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.tomogoma.shoppinglistapp.data.DatabaseContract.ItemEntry;

/**
 * Created by ogoma on 01/03/15.
 */
public class ItemsFragment extends Fragment {

	private ItemListAdapter itemsAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		Log.i(getClass().getSimpleName(), "OnCreate called");
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		Log.i(getClass().getSimpleName(), "OnCreateView called");
		View rootView = inflater.inflate(R.layout.default_list_layout, container, false);
		if (savedInstanceState == null) {
			Log.i(getClass().getSimpleName(), "Null saved instance state");
			initializeViews(rootView);
		}
		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.items_frag, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

			case R.id.action_add:
				((CanReplaceFragment) getActivity()).replaceFragment(this, new AddItemFragment());
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		Log.i(getClass().getSimpleName(), "OnActivityCreated called");
		long parentID = getActivity().getIntent()
		                             .getLongExtra(ShoppingCartActivity.EXTRA_long_CATEGORY_ID, 1);

		String itemSortOrder = ItemEntry.TABLE_NAME + "." + ItemEntry.COLUMN_NAME + " ASC";
		Uri itemUri = ItemEntry.buildItemsInCategoryUri(parentID);
		String[] itemProjection = ItemListAdapter.itemsProjection;

		new ContentLoader(getActivity(), this)
				.loadContent(itemUri, itemsAdapter, itemProjection, itemSortOrder);

		super.onActivityCreated(savedInstanceState);
	}

	private void initializeViews(View rootView) {

		Log.i(getClass().getSimpleName(), "initializeViews called");
		itemsAdapter = new ItemListAdapter(getActivity());
		ListView listView = (ListView) rootView.findViewById(android.R.id.list);
		listView.setAdapter(itemsAdapter);
	}

}
