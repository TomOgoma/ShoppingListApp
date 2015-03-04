package com.tomogoma.shoppinglistapp;

import android.content.ContentValues;

import com.tomogoma.shoppinglistapp.data.DatabaseContract;

/**
 * Created by ogoma on 27/02/15.
 */
public class Brand {

	private String brandName;
	private String brandDesc;
	private String itemName;

	private float quantity;
	private double price;

	private BrandVersion brandVersion;

	public Brand(String itemName, String brandName, String brandVersionName,
	             String brandDesc, float quantity, double price) {

		this.itemName = itemName;

		if (brandVersionName != null && !brandVersionName.isEmpty()) {
			brandVersion = new BrandVersion(brandName, brandVersionName, brandDesc, quantity, price);
			return;
		}

		this.brandName = brandName;
		this.brandDesc = brandDesc;
		this.quantity = quantity;
		this.price = price;
	}

	public ContentValues packageForDB() {

		ContentValues contentValues = new ContentValues();
		contentValues.put(DatabaseContract.BrandEntry.COLUMN_ITEM_KEY, getItemName());
		contentValues.put(DatabaseContract.BrandEntry.COLUMN_NAME, getBrandName());
		contentValues.put(DatabaseContract.BrandEntry.COLUMN_DESC, brandDesc);
		contentValues.put(DatabaseContract.BrandEntry.COLUMN_PRICE, price);
		contentValues.put(DatabaseContract.BrandEntry.COLUMN_QTTY, quantity);
		return contentValues;
	}

	public String getItemName() {
		return itemName;
	}

	public String getBrandName() {

		if (isBrandVersioned()) {
			return brandVersion.getBrandName();
		}
		return brandName;
	}

	public boolean isBrandVersioned() {
		return brandVersion != null;
	}

	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public BrandVersion getBrandVersion() {
		return brandVersion;
	}

	public void setBrandVersion(BrandVersion brandVersion) {
		this.brandVersion = brandVersion;
	}

	public String getBrandDesc() {
		if (isBrandVersioned()) {
			return brandVersion.getBrandVersionDesc();
		}
		return brandDesc;
	}

	public void setBrandDesc(String brandDesc) {

		if (isBrandVersioned()) {
			brandVersion.setBrandVersionDesc(brandDesc);
			return;
		}
		this.brandDesc = brandDesc;
	}

	public float getQuantity() {
		if (isBrandVersioned()) {
			return brandVersion.getQuantity();
		}
		return quantity;
	}

	public void setQuantity(float quantity) {

		if (isBrandVersioned()) {
			brandVersion.setQuantity(quantity);
			return;
		}
		this.quantity = quantity;
	}

	public double getPrice() {
		if (isBrandVersioned()) {
			return brandVersion.getPrice();
		}
		return price;
	}

	public void setPrice(double price) {
		if (isBrandVersioned()) {
			brandVersion.setPrice(price);
		}
		this.price = price;
	}

}
