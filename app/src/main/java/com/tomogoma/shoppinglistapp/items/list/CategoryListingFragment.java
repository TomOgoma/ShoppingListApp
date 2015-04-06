package com.tomogoma.shoppinglistapp.items.list;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.tomogoma.shoppinglistapp.R;
import com.tomogoma.shoppinglistapp.data.ContentLoader;
import com.tomogoma.shoppinglistapp.data.ContentLoader.OnLoadFinishedListener;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.CategoryEntry;
import com.tomogoma.shoppinglistapp.util.UI;

import java.util.HashMap;

/**
 * Created by Tom Ogoma on 01/03/15.
 */
public class CategoryListingFragment extends ListFragment implements OnItemLongClickListener {

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
	private String mCurrCategoryName;
	private int mSelectPosition;
	private boolean mIsOnlyPane;
	private boolean mIsItemsLoaded;


	private ActionBarActivity mActivity;
	private ActionMode mSingleActionMode;
	private ActionMode.Callback mSingleActionModeCallback = new ActionMode.Callback() {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.edit_cab, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			mode.setTitle(mCurrCategoryName);
			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

			switch (item.getItemId()) {
				case R.id.action_edit: {
					mode.finish();
					openEditDialog();
					return true;
				}
				default: return false;
			}
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mSingleActionMode = null;
		}
	};

	public interface OnCategorySelectedListener {
		public void onCategorySelected(long categoryID, String categoryName);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCategorySelectCallBack = (OnCategorySelectedListener) activity;
		mActivity = (ActionBarActivity) activity;
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
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (savedInstanceState == null) {
			initializeViews();
			ListView listView = getListView();
			listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			listView.setOnItemLongClickListener(this);
		}
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {

		String sortOrder = CategoryEntry.COLUMN_NAME + " ASC";
		Uri categoryUri = CategoryEntry.CONTENT_URI;
		ContentLoader contentLoader = new ContentLoader(getActivity(), this);
		contentLoader.setOnLoadFinishedListener(new OnLoadFinishedListener() {
			@Override
			public void onLoadFinished(int id) {
				mIsItemsLoaded = true;
				setListShown(true);
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

		if (!mIsItemsLoaded) {
			setListShown(false);
		}

		//  TODO total hack, find better way of doing this
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
		if (getSelectionDetails(position)) {
			if (mSingleActionMode!=null) {
				mSingleActionMode.finish();
			}
			mCategorySelectCallBack.onCategorySelected(mCurrCategoryID, mCurrCategoryName);
		}
	}


	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

		view.animate();
		if (getSelectionDetails(position)) {
			if (!mIsOnlyPane) {
				mCategorySelectCallBack.onCategorySelected(mCurrCategoryID, mCurrCategoryName);
			}
			if (mSingleActionMode == null) {
				mSingleActionMode = mActivity.startSupportActionMode(mSingleActionModeCallback);
			}
			mSingleActionMode.invalidate();
			return true;
		}
		return false;
	}

	@Override
	public void onStop() {
		if (mSingleActionMode !=null) {
			mSingleActionMode.finish();
		}
		super.onStop();
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

	private boolean getSelectionDetails(int position) {

		Cursor cursor = mCategoryAdapter.getCursor();
		if (cursor != null && cursor.moveToPosition(position)) {
			mCurrCategoryID = cursor.getLong(_ID_COL_INDEX);
			mCurrCategoryName = cursor.getString(NAME_COL_INDEX);
			mSelectPosition = position;
			return true;
		}
		return false;
	}

	private void openEditDialog() {

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View dialogLayout = inflater.inflate(R.layout.dialog_edit_category, null);

		final EditText etCategoryName = (EditText) dialogLayout.findViewById(R.id.categoryName);
		Button actionDone = (Button) dialogLayout.findViewById(R.id.actionDone);
		Button actionCancel = (Button) dialogLayout.findViewById(R.id.actionCancel);

		final Dialog dialog = new Dialog(getActivity());
		dialog.setContentView(dialogLayout);
		dialog.setTitle(R.string.title_dialog_edit_category);

		etCategoryName.setText(mCurrCategoryName);
		actionDone.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (updateCategory(etCategoryName)) {
					String message = getString(R.string.toast_category_updated);
					message = String.format(message, etCategoryName.getText().toString());
					UI.showKeyboardToast(getActivity(), message);
					UI.hideKeyboard(mActivity, etCategoryName);
					dialog.dismiss();
				}
			}
		});
		actionCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		dialog.show();
	}

	/**
	 * Update category in db with id: {@link #mCurrCategoryID},
	 * and name: {@link #mCurrCategoryName}.
	 * @param etCategoryName EditText containing the new name of the category
	 * @return true if it no post-entry-details are required from user
	 * i.e. (safe to dismiss dialog/activity/view), false otherwise
	 */
	private boolean updateCategory(EditText etCategoryName) {

		String newName = etCategoryName.getText().toString();
		if (newName.isEmpty()) {
			String message = getString(R.string.error_inputViewErr_is_empty);
			etCategoryName.setError(message);
			return false;
		}

		if (newName.equals(mCurrCategoryName)) {
			return true;
		}

		ContentValues contentValues = new ContentValues();
		contentValues.put(CategoryEntry.COLUMN_NAME, newName);
		int updateCount = mActivity.getContentResolver().update(
				CategoryEntry.CONTENT_URI,
				contentValues,
				CategoryEntry._ID + "=?",
				new String[]{String.valueOf(mCurrCategoryID)}
		);

		if (updateCount==0) {
			String message = getString(R.string.error_already_exists);
			message = String.format(message, "");
			etCategoryName.setError(message);
			return false;
		}

		return true;
	}

}
