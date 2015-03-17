package com.tomogoma.shoppinglistapp.items.manipulate.edit;

import android.content.Intent;
import android.os.Bundle;

import com.tomogoma.shoppinglistapp.R;
import com.tomogoma.shoppinglistapp.items.list.CategoryListingActivity;
import com.tomogoma.shoppinglistapp.items.manipulate.DoneActionActivity;

public class EditItemActivity extends DoneActionActivity {

	public static final String EXTRA_String_CATEGORY_NAME = EditItemActivity.class.getName() + "_extra.category.name";
	public static final String EXTRA_long_ITEM_ID = EditItemActivity.class.getName() + "_extra.item.id";
	public static final String EXTRA_Class_CALLING_ACTIVITY = EditItemActivity.class.getName() + "_extra.calling.activity";

	private Class<?> mParentActivity;

	protected Class<?> getParentActivity() {
		return mParentActivity;
	}

	@Override
	protected void onSuperCreate(Bundle savedInstanceState) {

		setContentView(R.layout.activity_add_item);

		if (savedInstanceState == null) {

			String categoryName = getIntent().getStringExtra(EXTRA_String_CATEGORY_NAME);
			long itemID = getIntent().getLongExtra(EXTRA_long_ITEM_ID, -1);
			mParentActivity = (Class<?>) getIntent().getSerializableExtra(EXTRA_Class_CALLING_ACTIVITY);
			mParentActivity = (mParentActivity==null)? CategoryListingActivity.class: mParentActivity;

			Bundle arguments = new Bundle();
			arguments.putLong(EditItemFragment.EXTRA_long_ITEM_ID, itemID);
			arguments.putString(EditItemFragment.EXTRA_String_CATEGORY_NAME, categoryName);

			EditItemFragment editItemFragment = new EditItemFragment();
			editItemFragment.setArguments(arguments);

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
