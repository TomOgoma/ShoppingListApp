package com.tomogoma.shoppinglistapp.items.list;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.tomogoma.shoppinglistapp.R;
import com.tomogoma.shoppinglistapp.data.ContentLoader;
import com.tomogoma.shoppinglistapp.data.ContentLoader.OnLoadFinishedListener;
import com.tomogoma.shoppinglistapp.data.DatabaseContract;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.CategoryEntry;

import java.util.HashMap;

/**
 * Created by Tom Ogoma on 01/03/15.
 */
public class CategoriesFragment extends ListFragment {

	public static final String EXTRA_long_CATEGORY_ID = CategoriesFragment.class.getName() + "_extra.category.id";

	private static final String[] M_SELECT_COLUMNS = new String[]{
			DatabaseContract.CategoryEntry._ID,
			DatabaseContract.CategoryEntry.COLUMN_NAME
	};
	private static final int _ID_COL_INDEX = 0;
	private static final int NAME_COL_INDEX = 1;
	private static final String SELECTED_POSITION_KEY = CategoriesFragment.class.getName() + "_selected.position.key";

	public interface OnCategorySelectedListener {

		public void onCategorySelected(long categoryID, String categoryName);
	}

	private SimpleCursorAdapter mCategoryAdapter;
	private OnCategorySelectedListener mCategorySelectCallBack;
	private int mSelectedCategoryPosition;
	//  TODO set current category ID as the selected category in list (or the first)
	private long mCurrCategoryID;

	private HashMap<Long, Integer> mIDPositionMappings;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setRetainInstance(true);

		if (getArguments() == null) {
			mCurrCategoryID = CategoryEntry.DEFAULT_ID;
		} else {
			mCurrCategoryID = getArguments().getLong(EXTRA_long_CATEGORY_ID,
			                                         CategoryEntry.DEFAULT_ID);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		if (savedInstanceState == null) {
			initializeViews();
		} else {
			mSelectedCategoryPosition = savedInstanceState.getInt(SELECTED_POSITION_KEY);
		}
		return inflater.inflate(R.layout.default_list_layout, container, false);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {

		String sortOrder = DatabaseContract.CategoryEntry.COLUMN_NAME + " ASC";
		Uri categoryUri = DatabaseContract.CategoryEntry.CONTENT_URI;
		mCategorySelectCallBack = (OnCategorySelectedListener) getActivity();
		ContentLoader contentLoader = new ContentLoader(getActivity(), this);
		contentLoader.setOnLoadFinishedListener(new OnLoadFinishedListener() {
			@Override
			public void onLoadFinished(int id) {
				if (mSelectedCategoryPosition != ListView.INVALID_POSITION) {
					setSelection(mSelectedCategoryPosition);
				}
			}
		});
		contentLoader.loadContent(categoryUri, mCategoryAdapter, M_SELECT_COLUMNS, sortOrder);

		super.onActivityCreated(savedInstanceState);
	}

	protected void updateCategoryID(long newCategoryID) {
		mCurrCategoryID = newCategoryID;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		super.onListItemClick(l, v, position, id);
		Cursor cursor = mCategoryAdapter.getCursor();
		if (cursor != null && cursor.moveToPosition(position)) {
			long categoryID = cursor.getLong(_ID_COL_INDEX);
			String categoryName = cursor.getString(NAME_COL_INDEX);
			mCategorySelectCallBack.onCategorySelected(categoryID, categoryName);
			mSelectedCategoryPosition = position;
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {

		if (mSelectedCategoryPosition != ListView.INVALID_POSITION) {
			outState.putInt(SELECTED_POSITION_KEY, mSelectedCategoryPosition);
		}
		super.onSaveInstanceState(outState);
	}

	private void initializeViews() {

		mIDPositionMappings = new HashMap<>();
		mCategoryAdapter = new SimpleCursorAdapter(
				getActivity(),
				R.layout.list_category,
				null,
				new String[]{DatabaseContract.CategoryEntry.COLUMN_NAME},
				new int[]{R.id.categoryName},
				0
		);

		setListAdapter(mCategoryAdapter);
	}

}
