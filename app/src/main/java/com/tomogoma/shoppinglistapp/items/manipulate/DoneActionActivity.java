package com.tomogoma.shoppinglistapp.items.manipulate;

import android.view.Menu;
import android.view.MenuItem;

import com.tomogoma.shoppinglistapp.R;
import com.tomogoma.shoppinglistapp.ShoppingListAppActivity;

public abstract class DoneActionActivity extends ShoppingListAppActivity {

	protected abstract void processDoneAction();

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.add_item, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

			case R.id.action_done: {
				processDoneAction();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

}
