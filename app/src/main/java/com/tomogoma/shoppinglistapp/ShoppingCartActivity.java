package com.tomogoma.shoppinglistapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;


public class ShoppingCartActivity extends ActionBarActivity
		implements CanReplaceFragment {

	protected static final String EXTRA_long_CATEGORY_ID = ItemsFragment.class.getName() + "_extra.category.id";
	protected static final String EXTRA_String_CATEGORY_NAME = ItemsFragment.class.getName() + "_extra.category.name";

	private Fragment activeFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shopping_cart);
		if (savedInstanceState == null) {
			addFragment(new CategoriesFragment());
		}
	}

	private void addFragment(Fragment fragment) {
		getSupportFragmentManager()
				.beginTransaction()
				.add(R.id.container, fragment)
				.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.shopping_cart, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();
		if (id == R.id.action_settings) {
			//  TODO implement settings activity
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void replaceFragment(Fragment currFrag, Fragment withFragment) {

		if (withFragment instanceof ItemsFragment) {
			setTitle(getIntent().getStringExtra(EXTRA_String_CATEGORY_NAME));
		} else if (withFragment instanceof AddItemFragment) {
			setTitle("Add Item");
		} else if (withFragment instanceof CategoriesFragment) {
			setTitle("Shopping List App");
		}

		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.container, withFragment)
				.commit();
		activeFragment = withFragment;
	}

	@Override
	public void onBackPressed() {

		if (activeFragment instanceof CategoriesFragment) {
			super.onBackPressed();
			return;
		}

		replaceFragment(activeFragment, new CategoriesFragment());
	}
}