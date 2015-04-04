package com.tomogoma.shoppinglistapp.items.manipulate.add;

import android.content.Intent;
import android.os.Bundle;

import com.tomogoma.shoppinglistapp.R;
import com.tomogoma.shoppinglistapp.items.manipulate.ManipulateItemActivity;

public class AddItemActivity extends ManipulateItemActivity {

	public static final String EXTRA_boolean_FILL_CATEGORY_FIELD = AddItemActivity.class.getName() + "_extra.fill.category.field";

	@Override
	protected void onSuperCreate(Bundle savedInstanceState) {

		super.onSuperCreate(savedInstanceState);
		setContentView(R.layout.activity_add_item);

		if (savedInstanceState == null) {

			boolean fillCategoryField = getIntent().getBooleanExtra(EXTRA_boolean_FILL_CATEGORY_FIELD, true);
			mArguments.putBoolean(AddItemFragment.EXTRA_boolean_FILL_CATEGORY_FIELD, fillCategoryField);

			AddItemFragment addItemFragment = new AddItemFragment();
			addItemFragment.setArguments(mArguments);
			getSupportFragmentManager()
					.beginTransaction()
			        .add(R.id.container, addItemFragment, addItemFragment.getClass().getName())
			        .commit();
		}
	}

	@Override
	protected void processDoneAction() {

		//  TODO result not okay for back press, cancel etc
		AddItemFragment addItemFragment = (AddItemFragment)
				getSupportFragmentManager().findFragmentByTag(AddItemFragment.class.getName());
		Intent result = addItemFragment.processInput();
		if (result != null) {
			setResult(RESULT_OK, result);
			finish();
		}
	}

}
