package com.tomogoma.shoppinglistapp;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView;

import com.tomogoma.shoppinglistapp.data.DatabaseContract.BrandEntry;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.ItemEntry;

import java.util.HashMap;

/**
 * Created by ogoma on 01/03/15.
 */
public class ItemsFragment extends Fragment implements LoaderCallbacks<Cursor>, OnItemClickListener {

	public static final String EXTRA_HIERARCHICAL_PARENT_ID = ItemsFragment.class.getName() + "_extra.category.id";

	private static final int ITEM_LOADER_ID = -90210;

	private ItemListAdapter itemsAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.default_expandable_list_layout, container, false);
		if (savedInstanceState == null) {
			initializeViews(rootView);
		}
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		Log.d(getClass().getSimpleName(), "Activity created...");
		getLoaderManager().initLoader(ITEM_LOADER_ID, getArguments(), this);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (menu.findItem(R.id.action_add) == null) {
			inflater.inflate(R.menu.items_frag, menu);
		}
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

	private void initializeViews(View rootView) {

		itemsAdapter = new ItemListAdapter(getActivity(), null, this);
		ExpandableListView listView = (ExpandableListView) rootView.findViewById(R.id.expandableList);
		listView.setAdapter(itemsAdapter);
		listView.setOnItemClickListener(this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		Log.d(getClass().getSimpleName(), "onCreateLoader....");
		String sortOrder;
		Uri uri;
		String[] projection;
		long parentID = args.getLong(EXTRA_HIERARCHICAL_PARENT_ID);

		if (id == ITEM_LOADER_ID) {

			Log.d(getClass().getSimpleName(), "Loader for Items...");
			sortOrder = ItemEntry.TABLE_NAME + "." + ItemEntry.COLUMN_NAME + " ASC";
			uri = ItemEntry.buildItemsInCategoryUri(parentID);
			projection = ItemListAdapter.itemsProjection;
		} else {


			Log.d(getClass().getSimpleName(), "Loader for children (brands/versions)...");
			sortOrder = BrandEntry.TABLE_NAME + "." + BrandEntry.COLUMN_NAME + " ASC";
			uri = BrandEntry.buildBrandWithVersionsUri(parentID);
			projection = ItemListAdapter.brandProjection;
		}

		return new CursorLoader(getActivity(), uri, projection, null, null, sortOrder);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

		Log.d(getClass().getSimpleName(), "Loading finished...");
		int loaderID = loader.getId();
		if (loaderID == ITEM_LOADER_ID) {
			Log.d(getClass().getSimpleName(), "Setting group Cursor...");
			itemsAdapter.setGroupCursor(data);
		} else if (!data.isClosed()) {
			Log.d(getClass().getSimpleName(), "Setting children Cursor...");
			setChildrenCursor(loaderID, data);
		}//if...else if...
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

		Log.d(getClass().getSimpleName(), "Loader reset...");
		int loaderID = loader.getId();
		if (loaderID == ITEM_LOADER_ID) {
			Log.d(getClass().getSimpleName(), "Resetting group loader...");
			itemsAdapter.setGroupCursor(null);
		} else {
			Log.d(getClass().getSimpleName(), "Resetting children loader...");
			setChildrenCursor(loaderID, null);
		}//if..else
	}

	private void setChildrenCursor(int loaderID, Cursor data) {

		Log.d(getClass().getSimpleName(), "Setting child cursor...");
		try {
			HashMap<Integer, Integer> groupMap = itemsAdapter.getGroupMap();
			int groupPos = groupMap.get(loaderID);
			itemsAdapter.setChildrenCursor(groupPos, data);
		} catch (NullPointerException e) {
			Log.w(getClass().getName(), "Adapter expired: " + e.getMessage());
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Log.d(getClass().getSimpleName(), "List Item clicked; position: " + position + "; id: " + id);
		getLoaderManager().initLoader(position, null, this);
	}
}
