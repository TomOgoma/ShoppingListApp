package com.tomogoma.shoppinglistapp.items.manipulate;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.tomogoma.shoppinglistapp.R;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.CategoryEntry;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.ItemEntry;
import com.tomogoma.shoppinglistapp.items.manipulate.add.AddItemActivity;
import com.tomogoma.shoppinglistapp.items.manipulate.add.AddItemFragment;
import com.tomogoma.util.Formatter;
import com.tomogoma.util.ui.ContentLoader;
import com.tomogoma.util.ui.EditTextWithKeyBoardBackEvent;
import com.tomogoma.util.ui.EditTextWithKeyBoardBackEvent.OnImeBackListener;
import com.tomogoma.util.ui.TextFiltersAdapter;
import com.tomogoma.util.ui.UIUtils;

public abstract class ManipulateItemFragment
		extends Fragment
		implements OnFocusChangeListener, OnEditorActionListener,  OnImeBackListener, OnCheckedChangeListener {

	public static final String EXTRA_String_CATEGORY_NAME = AddItemFragment.class.getName() + "_extra.category.id";

	protected EditText etVersion;
	protected EditText etUnitPrice;
	protected EditText etLastsFor;
	protected EditText etQuantity;
	protected EditText etDesc;
	protected AutoCompleteTextView autoTvCategoryName;
	protected AutoCompleteTextView autoTvItemName;
	protected EditTextWithKeyBoardBackEvent etBrand;
	protected EditTextWithKeyBoardBackEvent etActualMeasUnit;
	protected TextView tvItemName;
	protected TextView tvBrand;
	protected TextView tvVersion;
	protected TextView tvUnitPrice;
	protected TextView tvActualMeasUnit;
	protected TextView tvLastsFor;
	protected TextView tvQuantity;
	protected TextView tvCat;
	protected TextView tvDesc;
	protected RadioGroup rgLastsForUnit;

	protected String mLastsForUnit;

	private SimpleCursorAdapter mItemsAdapter;
	private SimpleCursorAdapter mCategoriesAdapter;

	protected abstract Intent processInput(String categoryName, String itemName);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_add_item, container, false);
		if (savedInstanceState == null) {
			initializeViews(rootView);
		}
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		String itemSortOrder = ItemEntry.TABLE_NAME + "." + ItemEntry.COLUMN_NAME + " ASC";
		String categorySortOrder = CategoryEntry.COLUMN_NAME + " ASC";

		Uri itemUri = ItemEntry.CONTENT_URI;
		Uri categoryUri = CategoryEntry.CONTENT_URI;

		String[] itemProjection = new String[]{
				ItemEntry.TABLE_NAME + "." + ItemEntry._ID,
				ItemEntry.COLUMN_NAME
			};
		String[] categoryProjection = new String[]{
				CategoryEntry._ID,
				CategoryEntry.COLUMN_NAME
			};

		ContentLoader loader = new ContentLoader(getActivity(), this);
		loader.loadContent(itemUri, mItemsAdapter, itemProjection, itemSortOrder);
		loader.loadContent(categoryUri, mCategoriesAdapter, categoryProjection, categorySortOrder);

		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {

		if (hasFocus) {
			switch (v.getId()) {
				case R.id.categoryName:
					tvCat.setVisibility(View.VISIBLE);
					break;
				case R.id.itemName:
					tvItemName.setVisibility(View.VISIBLE);
					break;
				case R.id.unitPrice:
					setUnit(tvUnitPrice, R.string.price_hint_detail);
					tvUnitPrice.setVisibility(View.VISIBLE);
					break;
				case R.id.quantityLayout:
					setUnit(tvQuantity, R.string.quantity_hint_detail);
					tvQuantity.setVisibility(View.VISIBLE);
					break;
				case R.id.brandName:
					tvBrand.setVisibility(View.VISIBLE);
					break;
				case R.id.versionName:
					tvVersion.setVisibility(View.VISIBLE);
					break;
				case R.id.measUnit:
					tvActualMeasUnit.setVisibility(View.VISIBLE);
					break;
				case R.id.lastsFor:
					tvLastsFor.setVisibility(View.VISIBLE);
					break;
				case R.id.description:
					tvDesc.setVisibility(View.VISIBLE);
					break;
			}//switch
		} else {
			switch (v.getId()) {
				case R.id.categoryName:
					tvCat.setVisibility(View.GONE);
					break;
				case R.id.itemName:
					tvItemName.setVisibility(View.GONE);
					break;
				case R.id.unitPrice:
					tvUnitPrice.setVisibility(View.GONE);
					break;
				case R.id.quantityLayout:
					tvQuantity.setVisibility(View.GONE);
					break;
				case R.id.brandName:
					tvBrand.setVisibility(View.GONE);
					break;
				case R.id.versionName:
					tvVersion.setVisibility(View.GONE);
					break;
				case R.id.measUnit:
					setLastsForVisibility();
					tvActualMeasUnit.setVisibility(View.GONE);
					break;
				case R.id.lastsFor:
					tvLastsFor.setVisibility(View.GONE);
					break;
				case R.id.description:
					tvDesc.setVisibility(View.GONE);
					break;
			}//switch
		}//if...else
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

		if (actionId == EditorInfo.IME_ACTION_NEXT) {
			processDependentViewVisibility(v);
		}
		return false;
	}

	@Override
	public void onImeBack(View v, String text) {
		processDependentViewVisibility(v);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
			case R.id.lastsDays:
				mLastsForUnit = getResources().getString(R.string.lasts_day_text);
				break;
			case R.id.lastsWeeks:
				mLastsForUnit = getResources().getString(R.string.lasts_week_text);
				break;
			case R.id.lastsMonths:
				mLastsForUnit = getResources().getString(R.string.lasts_month_text);
				break;
			case R.id.lastsYears:
				mLastsForUnit = getResources().getString(R.string.lasts_year_text);
				break;
		}
	}

	public Intent processInput() {

		String itemName = autoTvItemName.getText().toString();
		String categoryName = autoTvCategoryName.getText().toString();
		if (!validate(itemName, categoryName)) {
			return null;
		}
		return processInput(categoryName, itemName);
	}

	protected boolean validate(String categoryName, String itemName) {

		if (categoryName.isEmpty() && itemName.isEmpty()) {

			autoTvItemName.setError(getString(R.string.missing_item_error_view));
			autoTvCategoryName.setError(getString(R.string.missing_category_error_view));

			String errorMessage = "Missing the item name or category name; must enter at least one";
			UIUtils.showToast(getActivity(), errorMessage);

			return false;
		}

		return true;
	}

	protected Intent packageResultIntent(long categoryID, String categoryName) {

		Intent resultIntent = new Intent();
		resultIntent.putExtra(AddItemActivity.EXTRA_long_CATEGORY_ID, categoryID);
		resultIntent.putExtra(AddItemActivity.EXTRA_String_CATEGORY_NAME, categoryName);
		return resultIntent;
	}

	protected void processDependentViewVisibility(View v) {

		switch (v.getId()) {
			case R.id.brandName:
				setVersionVisibility();
				break;
			case R.id.measUnit:
				setLastsForVisibility();
				break;
		}
	}

	protected void setCategoryNameField(String categoryName) {
		autoTvCategoryName.setText(categoryName);
	}

	private void setLastsForVisibility() {

		if (etActualMeasUnit.getText().toString().isEmpty()) {
			rgLastsForUnit.setVisibility(View.GONE);
			etLastsFor.setVisibility(View.GONE);
			return;
		}

		String measUnit = etActualMeasUnit.getText().toString();
		String hintDetStr = getResources().getString(R.string.useful_per_actual_hint_detail);
		hintDetStr = String.format(hintDetStr, measUnit, mLastsForUnit);
		tvLastsFor.setText(hintDetStr);
		etLastsFor.setVisibility(View.VISIBLE);
		rgLastsForUnit.setVisibility(View.VISIBLE);
	}

	private void setVersionVisibility() {

		if (etBrand.getText().toString().isEmpty()) {
			etVersion.setVisibility(View.GONE);
			return;
		}
		etVersion.setVisibility(View.VISIBLE);
	}

	private void setUnit(TextView descriptionView, int stringId) {

		String description = getResources().getString(stringId);
		String unit = etActualMeasUnit.getText().toString();
		unit = Formatter.formatMeasUnit(unit);
		descriptionView.setText(String.format(description, unit));
	}

	private void initializeViews(View rootView) {

		mItemsAdapter = new TextFiltersAdapter(
				getActivity(),
				ItemEntry.CONTENT_URI,
				ItemEntry.TABLE_NAME + "." + ItemEntry._ID,
				ItemEntry.COLUMN_NAME
		);

		mCategoriesAdapter = new TextFiltersAdapter(
				getActivity(),
				CategoryEntry.CONTENT_URI,
				CategoryEntry._ID,
				CategoryEntry.COLUMN_NAME
		);

		mItemsAdapter.getStringConversionColumn();

		autoTvCategoryName = (AutoCompleteTextView) rootView.findViewById(R.id.categoryName);
		autoTvItemName = (AutoCompleteTextView) rootView.findViewById(R.id.itemName);

		autoTvCategoryName.setAdapter(mCategoriesAdapter);
		autoTvItemName.setAdapter(mItemsAdapter);

		etUnitPrice = (EditText) rootView.findViewById(R.id.unitPrice);
		etQuantity = (EditText) rootView.findViewById(R.id.quantity);
		etVersion = (EditText) rootView.findViewById(R.id.versionName);
		etLastsFor = (EditText) rootView.findViewById(R.id.lastsFor);
		etDesc = (EditText) rootView.findViewById(R.id.description);

		etBrand = (EditTextWithKeyBoardBackEvent) rootView.findViewById(R.id.brandName);
		etActualMeasUnit = (EditTextWithKeyBoardBackEvent) rootView.findViewById(R.id.measUnit);

		rgLastsForUnit = (RadioGroup) rootView.findViewById(R.id.lastsForUnit);

		tvCat = (TextView) rootView.findViewById(R.id.categoryNameHint);
		tvItemName = (TextView) rootView.findViewById(R.id.itemNameHint);
		tvUnitPrice = (TextView) rootView.findViewById(R.id.unitPriceHint);
		tvBrand = (TextView) rootView.findViewById(R.id.brandNameHint);
		tvActualMeasUnit = (TextView) rootView.findViewById(R.id.measUnitHint);
		tvQuantity = (TextView) rootView.findViewById(R.id.quantityHint);
		tvVersion = (TextView) rootView.findViewById(R.id.versionNameHint);
		tvLastsFor = (TextView) rootView.findViewById(R.id.lastsForHint);
		tvDesc = (TextView) rootView.findViewById(R.id.descriptionHint);

		autoTvItemName.setOnFocusChangeListener(this);
		etUnitPrice.setOnFocusChangeListener(this);
		etBrand.setOnFocusChangeListener(this);
		etActualMeasUnit.setOnFocusChangeListener(this);
		etQuantity.setOnFocusChangeListener(this);
		autoTvCategoryName.setOnFocusChangeListener(this);
		etVersion.setOnFocusChangeListener(this);
		rgLastsForUnit.setOnFocusChangeListener(this);
		etLastsFor.setOnFocusChangeListener(this);
		etDesc.setOnFocusChangeListener(this);

		etBrand.setOnEditorActionListener(this);
		etActualMeasUnit.setOnEditorActionListener(this);

		rgLastsForUnit.setOnCheckedChangeListener(this);
		onCheckedChanged(rgLastsForUnit, rgLastsForUnit.getCheckedRadioButtonId());

		etBrand.setOnEditTextImeBackListener(this);
		etActualMeasUnit.setOnEditTextImeBackListener(this);

		String categoryName = getArguments().getString(
				AddItemFragment.EXTRA_String_CATEGORY_NAME, CategoryEntry.DEFAULT_NAME);
		setCategoryNameField(categoryName);
	}

}
