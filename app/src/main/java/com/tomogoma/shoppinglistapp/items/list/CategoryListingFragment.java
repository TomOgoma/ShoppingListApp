package com.tomogoma.shoppinglistapp.items.list;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.tomogoma.shoppinglistapp.R;
import com.tomogoma.shoppinglistapp.data.ContentLoader;
import com.tomogoma.shoppinglistapp.data.ContentLoader.OnLoadFinishedListener;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.CategoryEntry;

import java.util.HashMap;

/**
 * Created by Tom Ogoma on 01/03/15.
 */
public class CategoryListingFragment extends ListFragment {

	public static final String EXTRA_boolean_IS_ONLY_PANE = CategoryListingFragment.class.getName() + "_is.only.pane";

	private static final String LOG_TAG = CategoryListingFragment.class.getSimpleName();

	private static final String[] SELECT_COLUMNS = new String[]{
			CategoryEntry._ID,
			CategoryEntry.COLUMN_NAME
	};
	private static final int _ID_COL_INDEX = 0;
	private static final int NAME_COL_INDEX = 1;

	private HashMap<Long, Integer> mIDPositionMappings;
	private SimpleCursorAdapter mCategoryAdapter;
	private OnCategorySelectedListener mCategorySelectCallBack;
	private long mCurrCategoryID;
	private int mSelectPosition;
	private boolean mIsOnlyPane;

	public interface OnCategorySelectedListener {
		public void onCategorySelected(long categoryID, String categoryName);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCategorySelectCallBack = (OnCategorySelectedListener) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setRetainInstance(true);

		Bundle arguments = getArguments();
		if (arguments == null) {
			mCurrCategoryID = CategoryEntry.DEFAULT_ID;
			mIsOnlyPane = true;
		} else {
			mCurrCategoryID = arguments.getLong(ListingActivity.EXTRA_long_CATEGORY_ID,
			                                         CategoryEntry.DEFAULT_ID);
			mIsOnlyPane = arguments.getBoolean(EXTRA_boolean_IS_ONLY_PANE,
			                                         true);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		if (savedInstanceState == null) {
			initializeViews();
		}
		return inflater.inflate(R.layout.default_list_layout, container, false);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {

		String sortOrder = CategoryEntry.COLUMN_NAME + " ASC";
		Uri categoryUri = CategoryEntry.CONTENT_URI;
		ContentLoader contentLoader = new ContentLoader(getActivity(), this);
		contentLoader.setOnLoadFinishedListener(new OnLoadFinishedListener() {
			@Override
			public void onLoadFinished(int id) {
				if (!mIsOnlyPane && mSelectPosition != ListView.INVALID_POSITION) {
					getListView().post(new Runnable() {
						@Override
						public void run() {
							getListView().requestFocusFromTouch();
							setSelection(mSelectPosition);
						}
					});
				}
			}
		});
		contentLoader.loadContent(categoryUri, mCategoryAdapter, SELECT_COLUMNS, sortOrder);

		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();

		// Cannot perform this in onLoad finished, so we come do it here
		if (!mIsOnlyPane && mSelectPosition != ListView.INVALID_POSITION) {
			ListView list = getListView();
			long listItemID = list.getItemIdAtPosition(mSelectPosition);
			View listItemView = list.getFocusedChild();
			onListItemClick(list, listItemView, mSelectPosition, listItemID);
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		super.onListItemClick(l, v, position, id);
		Cursor cursor = mCategoryAdapter.getCursor();
		if (cursor != null && cursor.moveToPosition(position)) {
			mCurrCategoryID = cursor.getLong(_ID_COL_INDEX);
			String categoryName = cursor.getString(NAME_COL_INDEX);
			mSelectPosition = position;
			mCategorySelectCallBack.onCategorySelected(mCurrCategoryID, categoryName);
		}
	}

	private void initializeViews() {

		mIDPositionMappings = new HashMap<>();
		mCategoryAdapter = new SimpleCursorAdapter(
				getActivity(),
				R.layout.list_category,
				null,
				new String[]{CategoryEntry.COLUMN_NAME},
				new int[]{R.id.categoryName},
				0
			);

		mCategoryAdapter.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				long categoryID = cursor.getLong(_ID_COL_INDEX);
				int currPosition = cursor.getPosition();
				mIDPositionMappings.put(categoryID, mSelectPosition);
				if (categoryID == mCurrCategoryID) {
					mSelectPosition = currPosition;
				}
				return false;
			}
			});
		setListAdapter(mCategoryAdapter);
	}

}
