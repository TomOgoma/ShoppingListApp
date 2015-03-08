package com.tomogoma.shoppinglistapp.items.add;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.tomogoma.shoppinglistapp.R;

public class AddItemActivity extends ActionBarActivity {

	public static final String EXTRA_long_CATEGORY_ID = AddItemActivity.class.getName() + "_extra.category.id";
	public static final String EXTRA_String_CATEGORY_NAME = AddItemActivity.class.getName() + "_extra.category.name";

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_item);

		if (savedInstanceState == null) {

			String categoryName = getIntent().getStringExtra(EXTRA_String_CATEGORY_NAME);
			Bundle arguments = new Bundle();
			arguments.putString(AddItemFragment.EXTRA_String_CATEGORY_NAME, categoryName);

			AddItemFragment addItemFragment = new AddItemFragment();
			addItemFragment.setArguments(arguments);
			getSupportFragmentManager()
					.beginTransaction()
			        .add(R.id.container, addItemFragment, addItemFragment.getClass().getName())
			        .commit();
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.add_item, menu);
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

			case R.id.action_done: {
				//  TODO result not okay for back press, cancel etc
				AddItemFragment addItemFragment = (AddItemFragment)
						getSupportFragmentManager().findFragmentByTag(AddItemFragment.class.getName());
				Intent result = addItemFragment.processInput();
				setResult(RESULT_OK, result);
				finish();
				return true;
			}
			case R.id.action_settings:
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

}
