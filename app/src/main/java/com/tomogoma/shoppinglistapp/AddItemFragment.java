package com.tomogoma.shoppinglistapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddItemFragment extends Fragment
		implements View.OnFocusChangeListener, TextView.OnEditorActionListener, EditTextWithKeyBoardBackEvent.OnImeBackListener {

	private EditText etItemName, etVersion, etUnitPrice, etUsefulUnitsPerActual, etQtty, etCat, etDesc;
	private EditTextWithKeyBoardBackEvent etBrand, etActualMeasUnit, etUsefulMeasUnit;
	private TextView tvItemName, tvBrand, tvVersion, tvUnitPrice, tvActualMeasUnit, tvUsefulMeasUnit,
			tvUsefulUnitsPerActual, tvQtty, tvCat, tvDesc;

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

	private void processInput() {

//	    Item item = new Item(etCat, etItemName, etBrand, etVersion, etUnitPrice, etQtty,
//	            etActualMeasUnit, etUsefulMeasUnit, etUsefulUnitsPerActual, etDesc);
//	    new DBAccess(getActivity()).insertItem(item);

		long categoryID = AddItem.addCategory(getActivity(), etCat);
		long itemID = AddItem.addItem(getActivity(), categoryID, etItemName, etUnitPrice, etQtty, etActualMeasUnit, etUsefulMeasUnit,
		                              etUsefulUnitsPerActual, etDesc);

		if (etBrand.getText().toString().isEmpty()) {
			return;
		}

		long brandID = AddItem.addBrand(getActivity(), itemID, etBrand, etUnitPrice, etQtty, etDesc);

		if (!etVersion.getText().toString().isEmpty()) {
			AddItem.addVersion(getActivity(), brandID, etVersion, etUnitPrice, etQtty, etDesc);
		}
		Fragment itemsFrag = new ItemsFragment();
		Bundle args = new Bundle();
		args.putLong(ItemsFragment.EXTRA_HIERARCHICAL_PARENT_ID, categoryID);
		itemsFrag.setArguments(args);
		((CanReplaceFragment) getActivity()).replaceFragment(this, itemsFrag);
	}

	private void initViews(View rootView) {

		etItemName = (EditText) rootView.findViewById(R.id.itemName);
		etUnitPrice = (EditText) rootView.findViewById(R.id.unitPrice);
		etQtty = (EditText) rootView.findViewById(R.id.quantityLayout);
		etCat = (EditText) rootView.findViewById(R.id.itemCategory);
		etVersion = (EditText) rootView.findViewById(R.id.version);
		etUsefulUnitsPerActual = (EditText) rootView.findViewById(R.id.usefulPerActual);
		etDesc = (EditText) rootView.findViewById(R.id.description);

		etBrand = (EditTextWithKeyBoardBackEvent) rootView.findViewById(R.id.brand);
		etActualMeasUnit = (EditTextWithKeyBoardBackEvent) rootView.findViewById(R.id.measUnit);
		etUsefulMeasUnit = (EditTextWithKeyBoardBackEvent) rootView.findViewById(R.id.usefulUnit);

		tvCat = (TextView) rootView.findViewById(R.id.itemCategoryHint);
		tvItemName = (TextView) rootView.findViewById(R.id.itemNameHint);
		tvUnitPrice = (TextView) rootView.findViewById(R.id.unitPriceHint);
		tvBrand = (TextView) rootView.findViewById(R.id.brandHint);
		tvActualMeasUnit = (TextView) rootView.findViewById(R.id.measUnitHint);
		tvQtty = (TextView) rootView.findViewById(R.id.quantityHint);
		tvVersion = (TextView) rootView.findViewById(R.id.versionHint);
		tvUsefulMeasUnit = (TextView) rootView.findViewById(R.id.usefulUnitHint);
		tvUsefulUnitsPerActual = (TextView) rootView.findViewById(R.id.usefulPerActualHint);
		tvDesc = (TextView) rootView.findViewById(R.id.descriptionHint);

		etItemName.setOnFocusChangeListener(this);
		etUnitPrice.setOnFocusChangeListener(this);
		etBrand.setOnFocusChangeListener(this);
		etActualMeasUnit.setOnFocusChangeListener(this);
		etQtty.setOnFocusChangeListener(this);
		etCat.setOnFocusChangeListener(this);
		etVersion.setOnFocusChangeListener(this);
		etUsefulMeasUnit.setOnFocusChangeListener(this);
		etUsefulUnitsPerActual.setOnFocusChangeListener(this);
		etDesc.setOnFocusChangeListener(this);

		etBrand.setOnEditorActionListener(this);
		etActualMeasUnit.setOnEditorActionListener(this);
		etUsefulMeasUnit.setOnEditorActionListener(this);

		etBrand.setOnEditTextImeBackListener(this);
		etActualMeasUnit.setOnEditTextImeBackListener(this);
		etUsefulMeasUnit.setOnEditTextImeBackListener(this);
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {

		if (hasFocus) {
			switch (v.getId()) {
				case R.id.itemCategory:
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
					setUnit(tvQtty, R.string.quantity_hint_detail);
					tvQtty.setVisibility(View.VISIBLE);
					break;
				case R.id.brand:
					tvBrand.setVisibility(View.VISIBLE);
					break;
				case R.id.version:
					tvVersion.setVisibility(View.VISIBLE);
					break;
				case R.id.measUnit:
					tvActualMeasUnit.setVisibility(View.VISIBLE);
					break;
				case R.id.usefulUnit:
					tvUsefulMeasUnit.setVisibility(View.VISIBLE);
					break;
				case R.id.usefulPerActual:
					tvUsefulUnitsPerActual.setVisibility(View.VISIBLE);
					break;
				case R.id.description:
					tvDesc.setVisibility(View.VISIBLE);
					break;
			}//switch
		} else {
			switch (v.getId()) {
				case R.id.itemCategory:
					tvCat.setVisibility(View.GONE);
					break;
				case R.id.itemName:
					tvItemName.setVisibility(View.GONE);
					break;
				case R.id.unitPrice:
					tvUnitPrice.setVisibility(View.GONE);
					break;
				case R.id.quantityLayout:
					tvQtty.setVisibility(View.GONE);
					break;
				case R.id.brand:
					tvBrand.setVisibility(View.GONE);
					setVersionEditability();
					break;
				case R.id.version:
					tvVersion.setVisibility(View.GONE);
					break;
				case R.id.measUnit:
					tvActualMeasUnit.setVisibility(View.GONE);
					break;
				case R.id.usefulUnit:
					tvUsefulMeasUnit.setVisibility(View.GONE);
					break;
				case R.id.usefulPerActual:
					tvUsefulUnitsPerActual.setVisibility(View.GONE);
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
			case R.id.brand:
				setVersionEditability();
				break;
			case R.id.measUnit:
				setUsefulMeasUnitEditability();
				break;
			case R.id.usefulUnit:
				setUsefulPerActualEditability();
				break;
		}
	}

	@Override
	public void onImeBack(View v, String text) {
		processDependentViewVisibility(v);
	}

	private void setUsefulMeasUnitEditability() {

		if (etActualMeasUnit.getText().toString().isEmpty()) {
			setUnEditable(etUsefulMeasUnit);
			setUsefulPerActualEditability();
			return;
		}

		String measUnit = etActualMeasUnit.getText().toString();
		String hintDetStr = getResources().getString(R.string.useful_unit_hint_detail);
		hintDetStr = String.format(hintDetStr, measUnit);
		tvUsefulMeasUnit.setText(hintDetStr);
		setEditable(etUsefulMeasUnit);
	}

	private void setUsefulPerActualEditability() {

		if (etUsefulMeasUnit.getText().toString().isEmpty()) {
			setUnEditable(etUsefulUnitsPerActual);
			return;
		}

		String measUnit = etActualMeasUnit.getText().toString();
		String usefulUnit = etUsefulMeasUnit.getText().toString();
		String hintDetStr = getResources().getString(R.string.useful_per_actual_hint_detail);
		hintDetStr = String.format(hintDetStr, measUnit, usefulUnit);
		tvUsefulUnitsPerActual.setText(hintDetStr);
		setEditable(etUsefulUnitsPerActual);
	}
}
