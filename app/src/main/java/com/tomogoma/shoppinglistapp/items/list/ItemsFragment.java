package com.tomogoma.shoppinglistapp.items.list;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.tomogoma.shoppinglistapp.R;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.CategoryEntry;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.ItemEntry;
import com.tomogoma.util.ui.ContentLoader;
import com.tomogoma.util.ui.ItemListAdapter;

/**
 * Created by ogoma on 01/03/15.
 */
public class ItemsFragment extends Fragment {

	public static final String EXTRA_long_CATEGORY_ID = ItemsFragment.class.getName() + "_extra.category.id";
	public static final String EXTRA_String_CATEGORY_NAME = ItemsFragment.class.getName() + "_extra.category.name";

	private CursorAdapter itemsAdapter;
	private long mCategoryID;
	private String mCategoryName;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setRetainInstance(true);

		Bundle arguments = getArguments();
		if (arguments == null) {
			mCategoryID = CategoryEntry.DEFAULT_CATEGORY_ID;
			mCategoryName = CategoryEntry.DEFAULT_CATEGORY_NAME;
		} else {
			mCategoryID = arguments.getLong(EXTRA_long_CATEGORY_ID,
			                                CategoryEntry.DEFAULT_CATEGORY_ID);
			mCategoryName = arguments.getString(EXTRA_String_CATEGORY_NAME,
			                                    CategoryEntry.DEFAULT_CATEGORY_NAME);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.default_list_layout, container, false);
		ListView listView = (ListView) rootView.findViewById(android.R.id.list);
		if (savedInstanceState == null) {
			itemsAdapter = new ItemListAdapter(getActivity());
		}
		listView.setAdapter(itemsAdapter);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		getActivity().setTitle(mCategoryName);

		String itemSortOrder = ItemEntry.TABLE_NAME + "." + ItemEntry.COLUMN_NAME + " ASC";
		Uri itemUri = ItemEntry.buildItemsInCategoryUri(mCategoryID);
		String[] itemProjection = ItemListAdapter.S_ITEMS_PROJECTION;

		new ContentLoader(getActivity(), this)
				.loadContent(itemUri, itemsAdapter, itemProjection, itemSortOrder);

		super.onActivityCreated(savedInstanceState);
	}

}
