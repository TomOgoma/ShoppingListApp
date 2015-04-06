package com.tomogoma.shoppinglistapp.items.manipulate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.tomogoma.shoppinglistapp.data.DatabaseContract.CategoryEntry;
import com.tomogoma.shoppinglistapp.items.list.CategoryListingActivity;
import com.tomogoma.shoppinglistapp.items.list.ListingActivity;

/**
 * Created by Tom Ogoma on 03/04/15.
 */
public abstract class ManipulateItemActivity extends DoneActionActivity {

	private static final String LOG_TAG = ManipulateItemActivity.class.getName();

	protected long mCategoryID;
	protected String mCategoryName;
	protected Bundle mArguments;
	protected Class<?> mParentActivity;

	protected Class<?> getParentActivity() {
		return mParentActivity;
	}

	@Override
	protected void addToUpActionIntent(Intent upIntent) {
		upIntent.putExtra(ListingActivity.EXTRA_Bundle_CATEGORY_DETAILS, mArguments);
	}

	@Override
	protected void onSuperCreate(Bundle savedInstanceState) {

		if (savedInstanceState == null) {
			mArguments = getIntent().getBundleExtra(ListingActivity.EXTRA_Bundle_CATEGORY_DETAILS);
			mParentActivity = (Class<?>) getIntent().getSerializableExtra(EXTRA_Class_CALLING_ACTIVITY);
		} else {
			mArguments = savedInstanceState.getBundle(ListingActivity.EXTRA_Bundle_CATEGORY_DETAILS);
			mParentActivity = (Class<?>) savedInstanceState.getSerializable(EXTRA_Class_CALLING_ACTIVITY);
		}

		if (mArguments != null) {
			mCategoryID = mArguments.getLong(ListingActivity.EXTRA_long_CATEGORY_ID,
			                                         CategoryEntry.DEFAULT_ID);
			mCategoryName = mArguments.getString(ListingActivity.EXTRA_String_CATEGORY_NAME,
			                                     CategoryEntry.DEFAULT_NAME);
			Log.d(LOG_TAG, "parent activity null? " + (mParentActivity==null) +": " + mParentActivity);
			mParentActivity = (mParentActivity==null)? CategoryListingActivity.class: mParentActivity;
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBundle(ListingActivity.EXTRA_Bundle_CATEGORY_DETAILS, mArguments);
		outState.putSerializable(EXTRA_Class_CALLING_ACTIVITY, mParentActivity);
		super.onSaveInstanceState(outState);
	}
}
