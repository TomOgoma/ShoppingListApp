package com.tomogoma.shoppinglistapp.items.add;

import android.content.Intent;
import android.os.Bundle;

import com.tomogoma.shoppinglistapp.R;
import com.tomogoma.shoppinglistapp.items.list.CategoryListingActivity;

public class AddItemActivity extends DoneActionActivity {

	public static final String EXTRA_long_CATEGORY_ID = AddItemActivity.class.getName() + "_extra.category.id";
	public static final String EXTRA_String_CATEGORY_NAME = AddItemActivity.class.getName() + "_extra.category.name";

	@Override
	protected Class<?> getParentActivity() {
		return CategoryListingActivity.class;
	}

	@Override
	protected void onSuperCreate(Bundle savedInstanceState) {

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
	protected void processDoneAction() {

		//  TODO result not okay for back press, cancel etc
		AddItemFragment addItemFragment = (AddItemFragment)
				getSupportFragmentManager().findFragmentByTag(AddItemFragment.class.getName());
		Intent result = addItemFragment.processInput();
		setResult(RESULT_OK, result);
		finish();
	}

}
