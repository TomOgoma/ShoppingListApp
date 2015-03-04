package com.tomogoma.shoppinglistapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;


public class ShoppingCartActivity extends ActionBarActivity
		implements CanReplaceFragment {

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

		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

		if (!(currFrag instanceof AddItemFragment)) {
			fragmentTransaction.addToBackStack(String.valueOf(currFrag.getId()));
		}

		fragmentTransaction.replace(R.id.container, withFragment).commit();
	}

}