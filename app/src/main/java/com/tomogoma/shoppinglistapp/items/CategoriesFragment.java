package com.tomogoma.shoppinglistapp.items;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.tomogoma.shoppinglistapp.R;
import com.tomogoma.shoppinglistapp.data.DatabaseContract;
import com.tomogoma.util.ui.CanReplaceFragment;
import com.tomogoma.util.ui.ContentLoader;

/**
 * Created by Tom Ogoma on 01/03/15.
 */
public class CategoriesFragment extends ListFragment {

	private static final String[] selectColumns = new String[]{
			DatabaseContract.CategoryEntry._ID,
			DatabaseContract.CategoryEntry.COLUMN_NAME
	};
	private static final int _ID_COL_INDEX = 0;
	private static final int NAME_COL_INDEX = 1;

	private SimpleCursorAdapter categoryAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		String sortOrder = DatabaseContract.CategoryEntry.COLUMN_NAME + " ASC";
		Uri categoryUri = DatabaseContract.CategoryEntry.CONTENT_URI;

		new ContentLoader(getActivity(), this)
				.loadContent(categoryUri, categoryAdapter, selectColumns, sortOrder);

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
			String categoryName = cursor.getString(NAME_COL_INDEX);
			openItemsFragment(categoryID, categoryName);
		}
	}

	private void openItemsFragment(long categoryID, String categoryName) {

		Intent activityIntent = getActivity().getIntent();
		activityIntent.putExtra(AllItemsActivity.EXTRA_String_CATEGORY_NAME, categoryName);
		activityIntent.putExtra(AllItemsActivity.EXTRA_long_CATEGORY_ID, categoryID);

		((CanReplaceFragment) getActivity()).replaceFragment(this, new ItemsFragment());
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

}
