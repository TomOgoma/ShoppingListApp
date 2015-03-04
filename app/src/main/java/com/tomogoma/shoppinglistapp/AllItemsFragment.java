package com.tomogoma.shoppinglistapp;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class AllItemsFragment extends ListFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			initializeViews();
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.items_frag, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

			case R.id.action_add:
				openAddItemFragment();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void openAddItemFragment() {

		getFragmentManager()
				.beginTransaction()
				.replace(R.id.container, new AddItemFragment())
				.addToBackStack("allItemsFrag")
				.commit();
	}

	private void initializeViews() {

		Log.d(getClass().getSimpleName(), "Initialize views...");
		setRetainInstance(true);
		setHasOptionsMenu(true);

//	    List<Item> items = new DBAccess(getActivity()).getAllItems();
//        ItemListAdapter allItemsAdapter  = new ItemListAdapter(getActivity(),  items);
//        setListAdapter(allItemsAdapter);
//
//        if (getArguments() != null) {
//            Item newItem = (Item) getArguments().getSerializable(Item.BEAN_ID);
//            if (newItem != null) {
//                allItemsAdapter.clear();
//                allItemsAdapter.add(newItem);
//            }
//        }//if getArgs != null
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.default_list_layout, container, false);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
	}

}
