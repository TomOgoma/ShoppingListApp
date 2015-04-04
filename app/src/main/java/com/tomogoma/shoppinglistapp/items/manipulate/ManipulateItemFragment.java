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

import com.tomogoma.shoppinglistapp.EditTextWithKeyBoardBackEvent;
import com.tomogoma.shoppinglistapp.EditTextWithKeyBoardBackEvent.OnImeBackListener;
import com.tomogoma.shoppinglistapp.R;
import com.tomogoma.shoppinglistapp.data.ContentLoader;
import com.tomogoma.shoppinglistapp.data.ContentLoader.OnLoadFinishedListener;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.CategoryEntry;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.ItemEntry;
import com.tomogoma.shoppinglistapp.items.list.ListingActivity;
import com.tomogoma.shoppinglistapp.util.Formatter;
import com.tomogoma.shoppinglistapp.util.UI;

public abstract class ManipulateItemFragment extends Fragment
		implements OnFocusChangeListener, OnEditorActionListener,
		           OnImeBackListener, OnCheckedChangeListener {

	protected EditText mEtVersion;
	protected EditText mEtUnitPrice;
	protected EditText mEtLastsFor;
	protected EditText mEtQuantity;
	protected EditText mEtDesc;
	protected AutoCompleteTextView mAutoTvCategoryName;
	protected AutoCompleteTextView mAutoTvItemName;
	protected EditTextWithKeyBoardBackEvent mEtBrand;
	protected EditTextWithKeyBoardBackEvent mEtActualMeasUnit;
	protected TextView mTvItemName;
	protected TextView mTvBrand;
	protected TextView mTvVersion;
	protected TextView mTvUnitPrice;
	protected TextView mTvActualMeasUnit;
	protected TextView mTvLastsFor;
	protected TextView mTvQuantity;
	protected TextView mTvCat;
	protected TextView mTvDesc;
	protected RadioGroup mRgLastsForUnit;

	protected String mLastsForUnit;
	protected boolean mIsAutoTextViewAdaptersLoaded;

	private ContentLoader mLoader;
	private SimpleCursorAdapter mItemsAdapter;
	private SimpleCursorAdapter mCategoriesAdapter;
	private int mItemsLoaderID;
	private int mCategoriesLoaderID;

	protected abstract Intent processInput(String categoryName, String itemName);

	private class OnAutoTVAdapterLoadFinishedListener implements OnLoadFinishedListener {

		private boolean isOtherLoaded;

		@Override
		public void onLoadFinished(int id) {

			if (mItemsLoaderID == id) {
				updateLoaderFlagWhenReady();
			}
			else if (mCategoriesLoaderID == id) {
				updateLoaderFlagWhenReady();
			}
		}

		private synchronized void updateLoaderFlagWhenReady() {
			if (isOtherLoaded) {
				mIsAutoTextViewAdaptersLoaded = true;
				onSuperAdaptersLoaded();
			} else {
				isOtherLoaded = true;
			}
		}
	}

	protected void onSuperAdaptersLoaded() {}

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

		mLoader = new ContentLoader(getActivity(), this);
		mLoader.setOnLoadFinishedListener(new OnAutoTVAdapterLoadFinishedListener());
		mItemsLoaderID = mLoader.loadContent(itemUri, mItemsAdapter, itemProjection, itemSortOrder);
		mCategoriesLoaderID = mLoader.loadContent(categoryUri, mCategoriesAdapter, categoryProjection, categorySortOrder);

		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {

		if (hasFocus) {
			switch (v.getId()) {
				case R.id.categoryName:
					mTvCat.setVisibility(View.VISIBLE);
					break;
				case R.id.itemName:
					mTvItemName.setVisibility(View.VISIBLE);
					break;
				case R.id.unitPrice:
					setUnit(mTvUnitPrice, R.string.price_hint_detail);
					mTvUnitPrice.setVisibility(View.VISIBLE);
					break;
				case R.id.quantityLayout:
					setUnit(mTvQuantity, R.string.quantity_hint_detail);
					mTvQuantity.setVisibility(View.VISIBLE);
					break;
				case R.id.brandName:
					mTvBrand.setVisibility(View.VISIBLE);
					break;
				case R.id.versionName:
					mTvVersion.setVisibility(View.VISIBLE);
					break;
				case R.id.measUnit:
					mTvActualMeasUnit.setVisibility(View.VISIBLE);
					break;
				case R.id.lastsFor:
					mTvLastsFor.setVisibility(View.VISIBLE);
					break;
				case R.id.description:
					mTvDesc.setVisibility(View.VISIBLE);
					break;
			}//switch
		} else {
			switch (v.getId()) {
				case R.id.categoryName:
					mTvCat.setVisibility(View.GONE);
					break;
				case R.id.itemName:
					mTvItemName.setVisibility(View.GONE);
					break;
				case R.id.unitPrice:
					mTvUnitPrice.setVisibility(View.GONE);
					break;
				case R.id.quantityLayout:
					mTvQuantity.setVisibility(View.GONE);
					break;
				case R.id.brandName:
					mTvBrand.setVisibility(View.GONE);
					break;
				case R.id.versionName:
					mTvVersion.setVisibility(View.GONE);
					break;
				case R.id.measUnit:
					setLastsForVisibility();
					mTvActualMeasUnit.setVisibility(View.GONE);
					break;
				case R.id.lastsFor:
					mTvLastsFor.setVisibility(View.GONE);
					break;
				case R.id.description:
					mTvDesc.setVisibility(View.GONE);
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

		String itemName = mAutoTvItemName.getText().toString();
		String categoryName = mAutoTvCategoryName.getText().toString();
		if (!validate(itemName, categoryName)) {
			return null;
		}
		return processInput(categoryName, itemName);
	}

	protected boolean validate(String categoryName, String itemName) {

		if (categoryName.isEmpty() && itemName.isEmpty()) {

			mAutoTvItemName.setError(getString(R.string.error_inputViewErr_missing_item));
			mAutoTvCategoryName.setError(getString(R.string.error_inputViewErr_missing_category));

			String errorMessage = getString(R.string.error_toast_missing_item_or_category);
			UI.showKeyboardToast(getActivity(), errorMessage);

			return false;
		}

		return true;
	}

	protected Intent packageResultIntent(long categoryID, String categoryName) {

		Intent resultIntent = new Intent();
		resultIntent.putExtra(ListingActivity.EXTRA_long_CATEGORY_ID, categoryID);
		resultIntent.putExtra(ListingActivity.EXTRA_String_CATEGORY_NAME, categoryName);
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
		mAutoTvCategoryName.setText(categoryName);
	}

	private void setLastsForVisibility() {

		if (mEtActualMeasUnit.getText().toString().isEmpty()) {
			mRgLastsForUnit.setVisibility(View.GONE);
			mEtLastsFor.setVisibility(View.GONE);
			return;
		}

		String measUnit = mEtActualMeasUnit.getText().toString();
		String hintDetStr = getResources().getString(R.string.useful_per_actual_hint_detail);
		hintDetStr = String.format(hintDetStr, measUnit, mLastsForUnit);
		mTvLastsFor.setText(hintDetStr);
		mEtLastsFor.setVisibility(View.VISIBLE);
		mRgLastsForUnit.setVisibility(View.VISIBLE);
	}

	private void setVersionVisibility() {

		if (mEtBrand.getText().toString().isEmpty()) {
			mEtVersion.setVisibility(View.GONE);
			return;
		}
		mEtVersion.setVisibility(View.VISIBLE);
	}

	private void setUnit(TextView descriptionView, int stringId) {

		String description = getResources().getString(stringId);
		String unit = mEtActualMeasUnit.getText().toString();
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

		mAutoTvCategoryName = (AutoCompleteTextView) rootView.findViewById(R.id.categoryName);
		mAutoTvItemName = (AutoCompleteTextView) rootView.findViewById(R.id.itemName);

		mAutoTvCategoryName.setAdapter(mCategoriesAdapter);
		mAutoTvItemName.setAdapter(mItemsAdapter);

		mEtUnitPrice = (EditText) rootView.findViewById(R.id.unitPrice);
		mEtQuantity = (EditText) rootView.findViewById(R.id.quantity);
		mEtVersion = (EditText) rootView.findViewById(R.id.versionName);
		mEtLastsFor = (EditText) rootView.findViewById(R.id.lastsFor);
		mEtDesc = (EditText) rootView.findViewById(R.id.description);

		mEtBrand = (EditTextWithKeyBoardBackEvent) rootView.findViewById(R.id.brandName);
		mEtActualMeasUnit = (EditTextWithKeyBoardBackEvent) rootView.findViewById(R.id.measUnit);

		mRgLastsForUnit = (RadioGroup) rootView.findViewById(R.id.lastsForUnit);

		mTvCat = (TextView) rootView.findViewById(R.id.categoryNameHint);
		mTvItemName = (TextView) rootView.findViewById(R.id.itemNameHint);
		mTvUnitPrice = (TextView) rootView.findViewById(R.id.unitPriceHint);
		mTvBrand = (TextView) rootView.findViewById(R.id.brandNameHint);
		mTvActualMeasUnit = (TextView) rootView.findViewById(R.id.measUnitHint);
		mTvQuantity = (TextView) rootView.findViewById(R.id.quantityHint);
		mTvVersion = (TextView) rootView.findViewById(R.id.versionNameHint);
		mTvLastsFor = (TextView) rootView.findViewById(R.id.lastsForHint);
		mTvDesc = (TextView) rootView.findViewById(R.id.descriptionHint);

		mAutoTvItemName.setOnFocusChangeListener(this);
		mEtUnitPrice.setOnFocusChangeListener(this);
		mEtBrand.setOnFocusChangeListener(this);
		mEtActualMeasUnit.setOnFocusChangeListener(this);
		mEtQuantity.setOnFocusChangeListener(this);
		mAutoTvCategoryName.setOnFocusChangeListener(this);
		mEtVersion.setOnFocusChangeListener(this);
		mRgLastsForUnit.setOnFocusChangeListener(this);
		mEtLastsFor.setOnFocusChangeListener(this);
		mEtDesc.setOnFocusChangeListener(this);

		mEtBrand.setOnEditorActionListener(this);
		mEtActualMeasUnit.setOnEditorActionListener(this);

		mRgLastsForUnit.setOnCheckedChangeListener(this);
		onCheckedChanged(mRgLastsForUnit, mRgLastsForUnit.getCheckedRadioButtonId());

		mEtBrand.setOnEditTextImeBackListener(this);
		mEtActualMeasUnit.setOnEditTextImeBackListener(this);

		String categoryName = getArguments().getString(
				ListingActivity.EXTRA_String_CATEGORY_NAME, CategoryEntry.DEFAULT_NAME);
		setCategoryNameField(categoryName);
	}

}
