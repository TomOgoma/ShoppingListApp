package com.tomogoma.shoppinglistapp.items.add;


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
import com.tomogoma.shoppinglistapp.data.AddItem;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.CategoryEntry;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.ItemEntry;
import com.tomogoma.util.Formatter;
import com.tomogoma.util.ui.ContentLoader;
import com.tomogoma.util.ui.EditTextWithKeyBoardBackEvent;
import com.tomogoma.util.ui.EditTextWithKeyBoardBackEvent.OnImeBackListener;
import com.tomogoma.util.ui.TextFiltersAdapter;
import com.tomogoma.util.ui.UIUtils;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddItemFragment extends Fragment
		implements OnFocusChangeListener, OnEditorActionListener,
		           OnImeBackListener, OnCheckedChangeListener {

	public static final String EXTRA_String_CATEGORY_NAME = AddItemFragment.class.getName() + "_extra.category.id";

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
		loader.loadContent(itemUri, itemsAdapter, itemProjection, itemSortOrder);
		loader.loadContent(categoryUri, categoriesAdapter, categoryProjection, categorySortOrder);

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

	protected Intent processInput() {

		String itemName = autoTvItemName.getText().toString();
		String categoryName = autoTvCategoryName.getText().toString();

		if (categoryName.isEmpty() && itemName.isEmpty()) {

			autoTvItemName.setError("Probably forgot the item's name?");
			autoTvCategoryName.setError("Nice to have categories, isn't it?");

			String errorMessage = "Missing the item name or category name; must enter at least one";
			UIUtils.showToast(getActivity(), errorMessage);

			return null;
		}

		long categoryID = AddItem.addCategory(getActivity(), autoTvCategoryName);

		if (itemName.isEmpty()) {
			UIUtils.showToast(getActivity(), "Category: " + categoryName + " is now in place");
			return packageResultIntent(categoryID, categoryName);
		}

		long itemID = AddItem.addItem(getActivity(), categoryID, autoTvItemName,
		                              etUnitPrice, etQuantity, etLastsFor,
		                              etActualMeasUnit, lastsForUnit, etDesc);

		if (itemID == -1) {
			//  TODO edit the item?
			UIUtils.showToast(getActivity(), itemName + " already exists");
			return null;
		}

		String toastMessage =  itemName + " successfully created";

		if (categoryName.isEmpty()) {
			categoryName = CategoryEntry.DEFAULT_CATEGORY_NAME;
			toastMessage += " but placed";
		}

		toastMessage += " in the " + categoryName + " category";
		UIUtils.showToast(getActivity(), toastMessage);
		return packageResultIntent(categoryID, categoryName);
	}

	private Intent packageResultIntent(long categoryID, String categoryName) {

		Intent resultIntent = new Intent();
		resultIntent.putExtra(AddItemActivity.EXTRA_long_CATEGORY_ID, categoryID);
		resultIntent.putExtra(AddItemActivity.EXTRA_String_CATEGORY_NAME, categoryName);
		return resultIntent;
	}

	private void processDependentViewVisibility(View v) {

		switch (v.getId()) {
			case R.id.brandName:
				setVersionVisibility();
				break;
			case R.id.measUnit:
				setLastsForVisibility();
				break;
		}
	}

	private void setLastsForVisibility() {

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

		String categoryName = getArguments().getString(
				AddItemFragment.EXTRA_String_CATEGORY_NAME, CategoryEntry.DEFAULT_CATEGORY_NAME);
		autoTvCategoryName.setText(categoryName);
	}

}
