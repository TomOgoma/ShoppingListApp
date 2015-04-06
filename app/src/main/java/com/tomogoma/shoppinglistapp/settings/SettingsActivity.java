package com.tomogoma.shoppinglistapp.settings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.tomogoma.shoppinglistapp.R;
import com.tomogoma.shoppinglistapp.ShoppingListAppActivity;
import com.tomogoma.shoppinglistapp.data.FetchCurrenciesTask;
import com.tomogoma.shoppinglistapp.items.list.ListingActivity;

public class SettingsActivity extends ShoppingListAppActivity {

	private static final String LOG_TAG = SettingsActivity.class.getSimpleName();
	private Class<?> mCallingActivity;
	private Bundle mCategoryDetails;

	@Override
	protected Class<?> getParentActivity() {
		return mCallingActivity;
	}

	@Override
	protected void onSuperCreate(Bundle savedInstanceState) {

		new FetchCurrenciesTask(this).execute();
		setContentView(R.layout.settings_layout);

		if (savedInstanceState==null) {

			mCallingActivity = (Class) getIntent().getSerializableExtra(EXTRA_Class_CALLING_ACTIVITY);
			mCategoryDetails = getIntent().getBundleExtra(ListingActivity.EXTRA_Bundle_CATEGORY_DETAILS);

			getFragmentManager()
					.beginTransaction()
					.replace(R.id.settingsFrame, new PreferencesFragment())
					.commit();
		}
		else {
			mCallingActivity = (Class) savedInstanceState.getSerializable(EXTRA_Class_CALLING_ACTIVITY);
			mCategoryDetails = getIntent().getBundleExtra(ListingActivity.EXTRA_Bundle_CATEGORY_DETAILS);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(EXTRA_Class_CALLING_ACTIVITY, mCallingActivity);
		outState.putBundle(ListingActivity.EXTRA_Bundle_CATEGORY_DETAILS, mCategoryDetails);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void addToUpActionIntent(Intent upIntent) {
		Log.d(LOG_TAG, "Add to up action intent called");
		upIntent.putExtra(ListingActivity.EXTRA_Bundle_CATEGORY_DETAILS, mCategoryDetails);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

}
