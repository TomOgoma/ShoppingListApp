package com.tomogoma.util.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.tomogoma.shoppinglistapp.R;

/**
 * Created by Tom Ogoma on 08/03/15.
 */
public abstract class ShoppingListAppActivity extends ActionBarActivity {

	private  Class<Activity> mParentActivity;

	protected abstract  Class<?> getParentActivity();

	protected abstract  void onSuperCreate(Bundle savedInstanceState);

	@Override
	protected final void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		onSuperCreate(savedInstanceState);

		mParentActivity = (Class<Activity>) getParentActivity();
		if (mParentActivity != null) {
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

				//  TODO implement settings activity
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

	protected void replaceFragment(int containerID, Fragment withFragment) {

		UIUtils.hideKeyboard(this, getCurrentFocus());
		setTitle(getResources().getString(R.string.app_name));
		getSupportFragmentManager()
				.beginTransaction()
				.replace(containerID, withFragment, withFragment.getClass().getName())
				.commit();
	}

}
