package com.tomogoma.shoppinglistapp.data;

import android.widget.EditText;

/**
 * Created by Tom Ogoma on 09/03/15.
 */
public class Item {

	long mCategoryID;
	long mItemID;
	double mPrice;
	float mQuantity;
	float mLastsFor = 0f;
	String mItemName;
	String mMeasUnit;
	String mLastsForUnit;
	String mDescription;

	public Item( long categoryID, String lastsForUnit, EditText itemName,
	             EditText unitPrice, EditText quantity, EditText lastsFor,
	             EditText measUnit, EditText description) {
		this(categoryID, -1, lastsForUnit,itemName, unitPrice, quantity, lastsFor, measUnit, description);
	}

	public Item( long categoryID, long itemID, String lastsForUnit, EditText itemName,
	             EditText unitPrice, EditText quantity, EditText lastsFor,
	             EditText measUnit, EditText description) {

		mItemID = itemID;
		mCategoryID = categoryID;
		mLastsForUnit = lastsForUnit;
		mItemName = itemName.getText().toString();
		mMeasUnit = measUnit.getText().toString();
		mDescription = description.getText().toString();

		//  Multiple tries to avoid short-circuiting
		//  Do nothing for number format exceptions (assume empty -> default 0)
		try {
			mPrice = Double.parseDouble(unitPrice.getText().toString());
		} catch (Exception e) {
		}

		try {
			this.mQuantity = Float.parseFloat(quantity.getText().toString());
		} catch (Exception e) {
		}

		try {
			this.mLastsFor = Float.parseFloat(lastsFor.getText().toString());
		} catch (Exception e) {
		}
	}
}
