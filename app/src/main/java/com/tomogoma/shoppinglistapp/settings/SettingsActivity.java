package com.tomogoma.shoppinglistapp.settings;

import android.os.Bundle;
import android.view.Menu;

import com.tomogoma.shoppinglistapp.R;
import com.tomogoma.shoppinglistapp.ShoppingListAppActivity;
import com.tomogoma.shoppinglistapp.data.FetchCurrenciesTask;

public class SettingsActivity extends ShoppingListAppActivity {

	public static final String EXTRA_CALLING_ACTIVITY = SettingsActivity.class.getName() + "_extra.calling.activity";

	//private static final String LOG_TAG = SettingsActivity.class.getSimpleName();
	private Class<?> mCallingActivity;

	@Override
	protected Class<?> getParentActivity() {
		return mCallingActivity;
	}

	@Override
	protected void onSuperCreate(Bundle savedInstanceState) {

		new FetchCurrenciesTask(this).execute();
		mCallingActivity = (Class) getIntent().getSerializableExtra(EXTRA_CALLING_ACTIVITY);
		setContentView(R.layout.settings_layout);
		getFragmentManager()
				.beginTransaction()
				.replace(R.id.settingsFrame, new PreferencesFragment())
				.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

}
