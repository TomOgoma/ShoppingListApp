package com.tomogoma.shoppinglistapp.items;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.tomogoma.shoppinglistapp.R;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.ItemEntry;
import com.tomogoma.util.ui.CanReplaceFragment;
import com.tomogoma.util.ui.ContentLoader;
import com.tomogoma.util.ui.ItemListAdapter;

/**
 * Created by ogoma on 01/03/15.
 */
public class ItemsFragment extends Fragment {

	private SimpleCursorAdapter itemsAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.default_list_layout, container, false);
		if (savedInstanceState == null) {
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

		long parentID = getActivity().getIntent()
		                             .getLongExtra(AllItemsActivity.EXTRA_long_CATEGORY_ID, 1);

		String itemSortOrder = ItemEntry.TABLE_NAME + "." + ItemEntry.COLUMN_NAME + " ASC";
		Uri itemUri = ItemEntry.buildItemsInCategoryUri(parentID);
		String[] itemProjection = ItemListAdapter.itemsProjection;

		new ContentLoader(getActivity(), this)
				.loadContent(itemUri, itemsAdapter, itemProjection, itemSortOrder);

		super.onActivityCreated(savedInstanceState);
	}

	private void initializeViews(View rootView) {

		itemsAdapter = new ItemListAdapter(getActivity());
		ListView listView = (ListView) rootView.findViewById(android.R.id.list);
		listView.setAdapter(itemsAdapter);
	}

}
