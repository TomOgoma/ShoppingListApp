package com.tomogoma.shoppinglistapp;

import android.content.ContentValues;

import com.tomogoma.shoppinglistapp.data.DatabaseContract;

/**
 * Created by ogoma on 27/02/15.
 */
public class Category {

	private String categoryName;
	private boolean isNewCategory = true;

	public Category(String categoryName) {
		this.categoryName = categoryName;
	}

	public Category(String categoryName, boolean isNewCategory) {

		this.categoryName = categoryName;
		this.isNewCategory = isNewCategory;
	}

	public ContentValues packageForDB() {

		ContentValues contentValues = new ContentValues();
		contentValues.put(DatabaseContract.CategoryEntry.COLUMN_NAME, categoryName);
		return contentValues;
	}

	public boolean isNew() {
		return isNewCategory;
	}

	public void setIsNewCategory(boolean isNewCategory) {
		this.isNewCategory = isNewCategory;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
}
