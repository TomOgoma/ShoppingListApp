package com.tomogoma.shoppinglistapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.tomogoma.shoppinglistapp.settings.SettingsActivity;
import com.tomogoma.util.UIUtils;

/**
 * Created by Tom Ogoma on 08/03/15.
 */
public abstract class ShoppingListAppActivity extends ActionBarActivity {

	private  Class<Activity> mParentActivity;

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

	@Override
	protected final void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		onSuperCreate(savedInstanceState);

		mParentActivity = (Class<Activity>) getParentActivity();
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
				intent.putExtra(SettingsActivity.EXTRA_CALLING_ACTIVITY, getClass());
				startActivity(intent);
				return true;
			}
			case android.R.id.home: {

				Intent upIntent = new Intent(this, mParentActivity);
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

	protected void addFragment(int containerID, Fragment fragment) {

		getSupportFragmentManager()
				.beginTransaction()
				.add(containerID, fragment, fragment.getClass().getName())
				.commit();
	}

	/**
	 * Convenience method for replacing a fragment with another
	 * </p>
	 * NOTE: The ActionBar is set to be the app_name string resource
	 * @param containerID
	 * @param withFragment
	 */
	protected void replaceFragment(int containerID, Fragment withFragment) {

		UIUtils.hideKeyboard(this, getCurrentFocus());
		setTitle(getResources().getString(R.string.app_name));
		getSupportFragmentManager()
				.beginTransaction()
				.replace(containerID, withFragment, withFragment.getClass().getName())
				.commit();
	}

}
