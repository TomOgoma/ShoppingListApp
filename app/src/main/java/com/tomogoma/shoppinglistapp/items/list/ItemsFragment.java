package com.tomogoma.shoppinglistapp.items.list;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.tomogoma.shoppinglistapp.R;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.CategoryEntry;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.ItemEntry;
import com.tomogoma.shoppinglistapp.items.add.EditItemActivity;
import com.tomogoma.util.ui.ContentLoader;
import com.tomogoma.util.ui.ItemListAdapter;
import com.tomogoma.util.ui.ItemListAdapter.OnDeleteItemRequestListener;
import com.tomogoma.util.ui.ItemListAdapter.OnEditItemRequestListener;
import com.tomogoma.util.ui.UIUtils;

/**
 * Created by ogoma on 01/03/15.
 */
public class ItemsFragment extends Fragment
		implements OnItemClickListener, OnEditItemRequestListener, OnDeleteItemRequestListener{

	public static final String EXTRA_long_CATEGORY_ID = ItemsFragment.class.getName() + "_extra.category.id";
	public static final String EXTRA_String_CATEGORY_NAME = ItemsFragment.class.getName() + "_extra.category.name";

	private ItemListAdapter itemsAdapter;
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
			itemsAdapter.setOnEditItemRequestListener(this);
			itemsAdapter.setOnDeleteItemRequestListener(this);
		}
		listView.setAdapter(itemsAdapter);
		listView.setOnItemClickListener(this);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		getActivity().setTitle(mCategoryName);

		String itemSortOrder = ItemEntry.TABLE_NAME + "." + ItemEntry.COLUMN_NAME + " ASC";
		Log.d(getClass().getSimpleName(), "Got category ID " + mCategoryID);
		Uri itemUri = ItemEntry.buildItemsInCategoryUri(mCategoryID);
		Log.d(getClass().getName(), itemUri.getPath());
		String[] itemProjection = ItemListAdapter.S_ITEMS_PROJECTION;

		new ContentLoader(getActivity(), this)
				.loadContent(itemUri, itemsAdapter, itemProjection, itemSortOrder);

		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		if (position == itemsAdapter.getSelectedPosition()) {
			itemsAdapter.setSelectedPosition(-1);
		} else {
			itemsAdapter.setSelectedPosition(position);
		}
		//  TODO figure out how to use the following instead of re-querying the db:
//		itemsAdapter.getView(position, null, parent);
		//  passing null (instead of view) to ensure that newView() is called in order
		//  to re-inflate the view with the new layout.
		//  tried it debugged it, logged it -> goes through all expected method calls but
		//  does not update the actual view, Bummer!
		itemsAdapter.notifyDataSetChanged();
	}

	@Override
	public void onEditItemRequested(long itemID) {

		Intent editItemActivityIntent = new Intent(getActivity(), EditItemActivity.class);
		editItemActivityIntent.putExtra(EditItemActivity.EXTRA_String_CATEGORY_NAME, mCategoryName);
		editItemActivityIntent.putExtra(EditItemActivity.EXTRA_long_ITEM_ID, itemID);
		editItemActivityIntent.putExtra(EditItemActivity.EXTRA_Class_CALLING_ACTIVITY, getActivity().getClass());
		getActivity().startActivity(editItemActivityIntent);
	}

	@Override
	public void onDeleteItemRequested(long itemID, String itemName) {
		String whereClause = ItemEntry._ID + " = ?";
		String[] whereArgs = new String[] {String.valueOf(itemID)};
		ContentResolver contentResolver = getActivity().getContentResolver();
		int count = contentResolver.delete(ItemEntry.CONTENT_URI, whereClause, whereArgs);
		//  TODO do not delete immediately, archive and allow undo
		if (count==0) {
			UIUtils.showToast(getActivity(), "failed to delete the item, please try again");
		} else if (count>1) {
			UIUtils.showToast(getActivity(), "Whoaaa! I may have screwed up, please check that your data is okay");
		} else {
			UIUtils.showToast(getActivity(), "Successfully deleted " + itemName);
		}
	}
}
