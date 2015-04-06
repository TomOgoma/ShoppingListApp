package com.tomogoma.shoppinglistapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.tomogoma.shoppinglistapp.settings.SettingsActivity;
import com.tomogoma.shoppinglistapp.util.UI;

/**
 * Created by Tom Ogoma on 08/03/15.
 */
public abstract class ShoppingListAppActivity extends ActionBarActivity {


	public static final String EXTRA_Class_CALLING_ACTIVITY =
			ShoppingListAppActivity.class.getName() + "_extra.calling.activity";

	private  Class<?> mParentActivity;

	/**
	 * Expects the parent Activity Class of implementing instance for use in the "Up"
	 * button functionality
	 * @return hierarchical parentActivity class, null if none/unknown
	 */
	protected abstract  Class<?> getParentActivity();

	/**
	 * Use this in place of {@link #onCreate(android.os.Bundle)} which has been marked
	 * final in this implementation.
	 * </p>
	 * DO NOT call {@link super#onCreate(android.os.Bundle)}, that has already been
	 * done by the implementation of onCreate
	 * </p>
	 * No need to call {@link #onSuperCreate(android.os.Bundle)} as it is abstract here
	 * </p>
	 * @param savedInstanceState
	 */
	protected abstract  void onSuperCreate(Bundle savedInstanceState);

	/**
	 * Override this if you want to provide extra data for the class you provided in
	 * {@link #getParentActivity()}.
	 * @param upIntent
	 */
	protected void addToUpActionIntent(Intent upIntent) {}

	/**
	 * Override this if you want the up button for the settings intent to have extra
	 * data.
	 * @param settingsIntent
	 */
	protected void addToSettingsActivityIntent(Intent settingsIntent){}

	@Override
	protected final void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		onSuperCreate(savedInstanceState);

		mParentActivity = getParentActivity();
		if (mParentActivity == null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		} else {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case  R.id.action_settings: {

				Intent intent = new Intent(this, SettingsActivity.class);
				intent.putExtra(EXTRA_Class_CALLING_ACTIVITY, getClass());
				addToSettingsActivityIntent(intent);
				startActivity(intent);
				return true;
			}
			case android.R.id.home: {

				Intent upIntent = new Intent(this, mParentActivity);
				addToUpActionIntent(upIntent);
				if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
					TaskStackBuilder.create(this)
					                .addNextIntentWithParentStack(upIntent)
					                .startActivities();
				} else {
					NavUtils.navigateUpTo(this, upIntent);
				}
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Convenience method for adding a fragment to a container for the first time
	 * @param containerID
	 * @param fragment
	 */
	protected void addFragment(int containerID, Fragment fragment) {

		getSupportFragmentManager()
				.beginTransaction()
				.add(containerID, fragment, fragment.getClass().getName())
				.commit();
	}

	/**
	 * Convenience method for replacing a fragment with another.
	 * </p>
	 * NOTE: The ActionBar is set to be the app_name string resource
	 * @param containerID
	 * @param withFragment
	 */
	protected void replaceFragment(int containerID, Fragment withFragment) {

		UI.hideKeyboard(this, getCurrentFocus());
		setTitle(getResources().getString(R.string.app_name));
		getSupportFragmentManager()
				.beginTransaction()
				.replace(containerID, withFragment, withFragment.getClass().getName())
				.commit();
	}

}
