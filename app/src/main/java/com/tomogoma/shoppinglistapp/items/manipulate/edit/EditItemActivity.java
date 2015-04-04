package com.tomogoma.shoppinglistapp.items.manipulate.edit;

import android.content.Intent;
import android.os.Bundle;

import com.tomogoma.shoppinglistapp.R;
import com.tomogoma.shoppinglistapp.items.list.ListingActivity;
import com.tomogoma.shoppinglistapp.items.manipulate.ManipulateItemActivity;

public class EditItemActivity extends ManipulateItemActivity {

	@Override
	protected void onSuperCreate(Bundle savedInstanceState) {

		super.onSuperCreate(savedInstanceState);
		setContentView(R.layout.activity_add_item);

		if (savedInstanceState == null) {

			long itemID = getIntent().getLongExtra(ListingActivity.EXTRA_long_ITEM_ID, -1);
			mArguments.putLong(ListingActivity.EXTRA_long_ITEM_ID, itemID);

			EditItemFragment editItemFragment = new EditItemFragment();
			editItemFragment.setArguments(mArguments);

			getSupportFragmentManager()
					.beginTransaction()
			        .add(R.id.container, editItemFragment, editItemFragment.getClass().getName())
			        .commit();
		}
	}

	protected void processDoneAction() {

		//  TODO result not okay for back press, cancel etc
		EditItemFragment editItemFragment = (EditItemFragment)
				getSupportFragmentManager().findFragmentByTag(EditItemFragment.class.getName());
		Intent result = editItemFragment.processInput();
		if (result != null) {
			setResult(RESULT_OK, result);
			finish();
		}
	}

}
