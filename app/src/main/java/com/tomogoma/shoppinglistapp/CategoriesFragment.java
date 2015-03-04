package com.tomogoma.shoppinglistapp;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.tomogoma.shoppinglistapp.data.DatabaseContract;

/**
 * Created by ogoma on 01/03/15.
 */
public class CategoriesFragment extends ListFragment implements LoaderCallbacks<Cursor> {

	private static final String[] selectColumns = new String[]{
			DatabaseContract.CategoryEntry._ID,
			DatabaseContract.CategoryEntry.COLUMN_NAME
	};
	private static final int _ID_COL_INDEX = 0;
	private static final int NAME_COL_INDEX = 1;

	private static final int CATEGORY_LOADER = 1234;

	private SimpleCursorAdapter categoryAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		getLoaderManager().initLoader(CATEGORY_LOADER, null, this);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		//  TODO add option menu for adding a category
		if (menu.findItem(R.id.action_add) == null) {
			inflater.inflate(R.menu.items_frag, menu);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {


		switch (item.getItemId()) {

			case R.id.action_add:
				//  TODO fragment for adding item
				((CanReplaceFragment) getActivity())
						.replaceFragment(this, new AddItemFragment());
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState == null) {
			initializeViews();
		}
		return inflater.inflate(R.layout.default_list_layout, container, false);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		super.onListItemClick(l, v, position, id);
		v.animate();
		Cursor cursor = categoryAdapter.getCursor();
		if (cursor != null && cursor.moveToPosition(position)) {
			long categoryID = cursor.getLong(_ID_COL_INDEX);
			openItemsFragment(categoryID);
		}
	}

	private void openItemsFragment(long categoryID) {

		Bundle arguments = new Bundle();
		arguments.putLong(ItemsFragment.EXTRA_HIERARCHICAL_PARENT_ID, categoryID);

		Fragment itemsFrag = new ItemsFragment();
		itemsFrag.setArguments(arguments);

		((CanReplaceFragment) getActivity()).replaceFragment(this, itemsFrag);
	}

	private void initializeViews() {

		categoryAdapter = new SimpleCursorAdapter(
				getActivity(),
				R.layout.list_category,
				null,
				new String[]{DatabaseContract.CategoryEntry.COLUMN_NAME},
				new int[]{R.id.categoryName},
				0
		);

		categoryAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

				switch (columnIndex) {

					case NAME_COL_INDEX: {

						String categoryName = cursor.getString(columnIndex);
						TextView categoryNameView = (TextView) view;
						categoryNameView.setText(categoryName);
						return true;
					}
				}
				return false;
			}
		});

		setListAdapter(categoryAdapter);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		String sortOrder = DatabaseContract.CategoryEntry.COLUMN_NAME + " ASC";
		Uri categoryUri = DatabaseContract.CategoryEntry.CONTENT_URI;

		return new CursorLoader(
				getActivity(),
				categoryUri,
				selectColumns,
				null,
				null,
				sortOrder
		);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		categoryAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		categoryAdapter.swapCursor(null);
	}
}
