package com.tomogoma.shoppinglistapp;

import android.content.ContentValues;

import com.tomogoma.shoppinglistapp.data.DatabaseContract;

/**
 * Created by ogoma on 27/02/15.
 */
public class BrandVersion {

	private String brandVersionName;
	private String brandVersionDesc;
	private String brandName;
	private float quantity;
	private double price;

	public BrandVersion(String brandName, String brandVersionName,
	                    String brandVersionDesc, float quantity, double price) {

		this.brandName = brandName;
		this.brandVersionName = brandVersionName;
		this.brandVersionDesc = brandVersionDesc;
		this.quantity = quantity;
		this.price = price;
	}

	public ContentValues packageForDB() {

		ContentValues contentValues = new ContentValues();
		contentValues.put(DatabaseContract.VersionEntry.COLUMN_BRAND_KEY, brandName);
		contentValues.put(DatabaseContract.VersionEntry.COLUMN_NAME, brandVersionName);
		contentValues.put(DatabaseContract.VersionEntry.COLUMN_DESC, brandVersionDesc);
		contentValues.put(DatabaseContract.VersionEntry.COLUMN_PRICE, price);
		contentValues.put(DatabaseContract.VersionEntry.COLUMN_QTTY, quantity);
		return contentValues;
	}

	public String getBrandVersionName() {
		return brandVersionName;
	}

	public void setBrandVersionName(String brandVersionName) {
		this.brandVersionName = brandVersionName;
	}

	public String getBrandVersionDesc() {
		return brandVersionDesc;
	}

	public void setBrandVersionDesc(String brandVersionDesc) {
		this.brandVersionDesc = brandVersionDesc;
	}

	public String getBrandName() {
		return brandName;
	}

	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}

	public float getQuantity() {
		return quantity;
	}

	public void setQuantity(float quantity) {
		this.quantity = quantity;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}
}
