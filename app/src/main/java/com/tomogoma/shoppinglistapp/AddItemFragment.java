package com.tomogoma.shoppinglistapp;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import android.widget.Toast;

import com.tomogoma.shoppinglistapp.EditTextWithKeyBoardBackEvent.OnImeBackListener;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.CategoryEntry;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.ItemEntry;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddItemFragment extends Fragment
		implements OnFocusChangeListener, OnEditorActionListener,
		           OnImeBackListener, OnCheckedChangeListener {

	private static final int ITEM_LOADER_ID = 1;
	private static final int CATEGORY_LOADER_ID = 0;

	private EditText etVersion;
	private EditText etUnitPrice;
	private EditText etLastsFor;
	private EditText etQuantity;
	private EditText etDesc;

	private AutoCompleteTextView autoTvCategoryName;
	private AutoCompleteTextView autoTvItemName;

	private EditTextWithKeyBoardBackEvent etBrand;
	private EditTextWithKeyBoardBackEvent etActualMeasUnit;

	private TextView tvItemName;
	private TextView tvBrand;
	private TextView tvVersion;
	private TextView tvUnitPrice;
	private TextView tvActualMeasUnit;
	private TextView tvLastsFor;
	private TextView tvQuantity;
	private TextView tvCat;
	private TextView tvDesc;

	private RadioGroup rgLastsForUnit;

	private String lastsForUnit;

	private SimpleCursorAdapter itemsAdapter;
	private SimpleCursorAdapter categoriesAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_add_item, container, false);
		setHasOptionsMenu(true);
		initViews(rootView);
		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.add_item_frag, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

			case R.id.action_done:
				processInput();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		String itemSortOrder = ItemEntry.TABLE_NAME + "." + ItemEntry.COLUMN_NAME + " ASC";
		Uri itemUri = ItemEntry.CONTENT_URI;
		String[] itemProjection = new String[]{
				ItemEntry.TABLE_NAME + "." + ItemEntry._ID,
				ItemEntry.COLUMN_NAME
		};

		String categorySortOrder = CategoryEntry.COLUMN_NAME + " ASC";
		Uri categoryUri = CategoryEntry.CONTENT_URI;
		String[] categoryProjection = new String[]{
				CategoryEntry._ID,
				CategoryEntry.COLUMN_NAME
		};

		ContentLoader loader = new ContentLoader(getActivity(), this);
		loader.loadContent(itemUri, itemsAdapter, itemProjection, itemSortOrder);
		loader.loadContent(categoryUri, categoriesAdapter, categoryProjection, categorySortOrder);

		super.onActivityCreated(savedInstanceState);
	}

	private void processInput() {

		long categoryID = AddItem.addCategory(getActivity(), autoTvCategoryName);
		long itemID = AddItem.addItem(getActivity(), categoryID, autoTvItemName,
		                              etUnitPrice, etQuantity, etLastsFor,
		                              etActualMeasUnit, lastsForUnit, etDesc);

		if (itemID == -1) {
			Toast.makeText(getActivity(), "Failed to insert details fully", Toast.LENGTH_LONG)
			     .show();
			return;
		}

		Intent activityIntent = getActivity().getIntent();
		activityIntent.putExtra(ShoppingCartActivity.EXTRA_long_CATEGORY_ID, categoryID);
		activityIntent.putExtra(ShoppingCartActivity.EXTRA_String_CATEGORY_NAME,
		                        autoTvCategoryName.getText().toString());
		((CanReplaceFragment) getActivity()).replaceFragment(this, new ItemsFragment());
	}

	private void initViews(View rootView) {

		itemsAdapter = new TextFiltersAdapter(
				getActivity(),
				ItemEntry.CONTENT_URI,
				ItemEntry.TABLE_NAME + "." + ItemEntry._ID,
				ItemEntry.COLUMN_NAME
		);

		categoriesAdapter = new TextFiltersAdapter(
				getActivity(),
				CategoryEntry.CONTENT_URI,
				CategoryEntry._ID,
				CategoryEntry.COLUMN_NAME
		);

		autoTvCategoryName = (AutoCompleteTextView) rootView.findViewById(R.id.categoryName);
		autoTvItemName = (AutoCompleteTextView) rootView.findViewById(R.id.itemName);

		autoTvCategoryName.setAdapter(categoriesAdapter);
		autoTvItemName.setAdapter(itemsAdapter);

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

		String categoryName = getActivity().getIntent()
		                                   .getStringExtra(ShoppingCartActivity.EXTRA_String_CATEGORY_NAME);
		autoTvCategoryName.setText(categoryName);
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
					setLastsForEditability();
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

	private void setUnit(TextView descriptionView, int stringId) {

		String description = getResources().getString(stringId);
		String unit = etActualMeasUnit.getText().toString();
		if (unit.isEmpty()) {
			descriptionView.setText(String.format(description, "unit"));
		} else {
			descriptionView.setText(String.format(description, unit));
		}
	}

	private void setVersionEditability() {

		if (etBrand.getText().toString().isEmpty()) {
			setUnEditable(etVersion);
			return;
		}
		setEditable(etVersion);
	}

	private void setUnEditable(View v) {

		v.setEnabled(false);
		v.setFocusable(false);
	}

	private void setEditable(View v) {

		v.setEnabled(true);
		v.setFocusableInTouchMode(true);
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

		if (actionId == EditorInfo.IME_ACTION_NEXT) {
			processDependentViewVisibility(v);
		}
		return false;
	}

	private void processDependentViewVisibility(View v) {

		switch (v.getId()) {
			case R.id.brandName:
				setVersionEditability();
				break;
			case R.id.measUnit:
				setLastsForEditability();
				break;
		}
	}

	@Override
	public void onImeBack(View v, String text) {
		processDependentViewVisibility(v);
	}

	private void setLastsForEditability() {

		if (etActualMeasUnit.getText().toString().isEmpty()) {
			rgLastsForUnit.setVisibility(View.GONE);
			etLastsFor.setVisibility(View.GONE);
			return;
		}

		String measUnit = etActualMeasUnit.getText().toString();
		String hintDetStr = getResources().getString(R.string.useful_per_actual_hint_detail);
		hintDetStr = String.format(hintDetStr, measUnit, lastsForUnit);
		tvLastsFor.setText(hintDetStr);
		etLastsFor.setVisibility(View.VISIBLE);
		rgLastsForUnit.setVisibility(View.VISIBLE);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
			case R.id.lastsDays:
				lastsForUnit = "Days";
				break;
			case R.id.lastsWeeks:
				lastsForUnit = "Weeks";
				break;
			case R.id.lastsMonths:
				lastsForUnit = "Months";
				break;
			case R.id.lastsYears:
				lastsForUnit = "Years";
				break;
		}
	}

}
