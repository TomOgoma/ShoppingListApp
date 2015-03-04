package com.tomogoma.shoppinglistapp;

import android.content.ContentValues;
import android.widget.EditText;

import com.tomogoma.util.Formatter;

import java.io.Serializable;

import static com.tomogoma.shoppinglistapp.data.DatabaseContract.ItemEntry;


/**
 * Created by ogoma on 23/02/15.
 */
public class Item implements Serializable {

	public static final transient String BEAN_ID = Item.class.getName() + "_item.bean.id";

	private String itemName;
	private String measUnit;
	private String usefulUnit;
	private float usefulUnitsPerActual;
	private String description;
	private float quantity;
	private double unitPrice;
	private boolean isInShoppingList;

	private Category category;
	private Brand brand;

	public Item(EditText categoryName, EditText itemName, EditText brand,
	            EditText version, EditText unitPrice,
	            EditText quantity,
	            EditText measUnit, EditText usefulUnit,
	            EditText usefulUnitsPerActual, EditText description) {

		this.measUnit = measUnit.getText().toString();
		this.usefulUnit = usefulUnit.getText().toString();
		this.isInShoppingList = false;
		category = new Category(categoryName.getText().toString());

		double price = 0d;
		float qtty = 0f;
		String name = itemName.getText().toString();
		String desc = description.getText().toString();
		String brandName = brand.getText().toString();
		String brandVersionName = version.getText().toString();
		try {
			qtty = Float.parseFloat(quantity.getText().toString());
			price = Double.parseDouble(unitPrice.getText().toString());
			this.usefulUnitsPerActual = Float.parseFloat(usefulUnitsPerActual.getText().toString());
		} catch (Exception e) {
			//  do nothing for empty
		}
		if (!brandName.isEmpty()) {
			this.brand = new Brand(name, brandName, brandVersionName, desc, qtty, price);
			return;
		}

		this.itemName = itemName.getText().toString();
		this.description = desc;
		this.quantity = qtty;
		this.unitPrice = price;
	}

	public Item(String categoryName, String itemName, String brandName, String versionName, double price, float qtty, String desc) {
		this(categoryName, itemName, brandName, versionName, price, qtty, null, null, 0, desc);
	}

	public Item(String categoryName, String itemName, String brand,
	            String version, double unitPrice,
	            float quantity,
	            String measUnit, String usefulUnit,
	            float usefulUnitsPerActual, String description) {

		this.measUnit = measUnit;
		this.usefulUnit = usefulUnit;
		this.usefulUnitsPerActual = usefulUnitsPerActual;
		this.isInShoppingList = false;
		category = new Category(categoryName);

		if (brand != null && !brand.isEmpty()) {
			this.brand = new Brand(itemName, brand, version, description, quantity, unitPrice);
			return;
		}

		this.itemName = itemName;
		this.description = description;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
	}

	public boolean brandHasVersion() {
		return isBranded() && brand.isBrandVersioned();
	}

	public boolean isBranded() {
		return brand != null;
	}

	public String getHumanQuantity() {

		final float quantity = getQuantity();

		String formattedQuantity = Formatter.formatQuantity(quantity);
		if (measUnit == null || measUnit.isEmpty()) {
			return formattedQuantity;
		}

		formattedQuantity += " " + measUnit;
		if (usefulUnit == null || usefulUnit.isEmpty()
				|| usefulUnitsPerActual == 0) {
			return formattedQuantity;
		}

		String usefulUnits = Formatter.formatQuantity
				(quantity * usefulUnitsPerActual);

		return new StringBuilder(formattedQuantity)
				.append(" (")
				.append(usefulUnits)
				.append(" ")
				.append(usefulUnit)
				.append(")")
				.toString();
	}

	public float getQuantity() {

		if (isBranded()) {
			return brand.getQuantity();
		}
		return quantity;
	}

	public void setQuantity(float quantity) {

		if (isBranded()) {
			brand.setQuantity(quantity);
		}
		this.quantity = quantity;
	}

	public ContentValues packageCategoryForDB() {
		return category.packageForDB();
	}

	public ContentValues packageBrandForDB() {
		return brand.packageForDB();
	}

	public ContentValues packageBrandVersionForDB() {
		return brand.getBrandVersion().packageForDB();
	}

	public ContentValues packageForDB() {

		ContentValues contentValues = new ContentValues();
		contentValues.put(ItemEntry.COLUMN_CAT_KEY, category.getCategoryName());
		contentValues.put(ItemEntry.COLUMN_NAME, getItemName());
		contentValues.put(ItemEntry.COLUMN_DESC, description);
		contentValues.put(ItemEntry.COLUMN_PRICE, unitPrice);
		contentValues.put(ItemEntry.COLUMN_QTTY, quantity);
		contentValues.put(ItemEntry.COLUMN_MEAS_UNIT, measUnit);
		contentValues.put(ItemEntry.COLUMN_USEFUL_UNIT, usefulUnit);
		contentValues.put(ItemEntry.COLUMN_USEFUL_PER_MEAS, usefulUnitsPerActual);
		return contentValues;
	}

	public String getItemName() {

		if (isBranded()) {
			return brand.getItemName();
		}
		return itemName;
	}

	public void setItemName(String itemName) {

		if (isBranded()) {
			brand.setItemName(itemName);
			return;
		}
		this.itemName = itemName;
	}

	public boolean isInNewCategory() {
		return category.isNew();
	}

	public double getTotalPrice() {
		return getUnitPrice() * getQuantity();
	}

	public double getUnitPrice() {

		if (isBranded()) {
			return brand.getPrice();
		}
		return unitPrice;
	}

	public void setUnitPrice(double unitPrice) {

		if (isBranded()) {
			brand.setPrice(unitPrice);
			return;
		}
		this.unitPrice = unitPrice;
	}

	public boolean isInShoppingList() {
		return isInShoppingList;
	}

	public void setInShoppingList(boolean isInShoppingList) {
		this.isInShoppingList = isInShoppingList;
	}

	public String getBrand() {

		if (isBranded()) {
			return brand.getBrandName();
		}
		return "";
	}

	public void setBrand(String brand) {
		if (isBranded()) {
			this.brand.setBrandName(brand);
		} else {
			this.brand = new Brand(itemName, brand, "", description, quantity, unitPrice);
			itemName = description = null;
			unitPrice = quantity = 0;
		}
	}

	public String getMeasUnit() {
		return measUnit;
	}

	public void setMeasUnit(String measUnit) {
		this.measUnit = measUnit;
	}

	public String getCategoryName() {
		return category.getCategoryName();
	}

	public void setCategoryName(String categoryName) {
		category.setCategoryName(categoryName);
	}

	public String getVersion() {
		if (isBranded() && brand.isBrandVersioned())
			return brand.getBrandVersion().getBrandVersionName();
		return "";
	}

	public void setBrandVersion(String brand, String version) {
		if (isBranded()) {
			this.brand.setBrandName(brand);
		} else {
			this.brand = new Brand(itemName, brand, "", description, quantity, unitPrice);
			itemName = description = null;
			unitPrice = quantity = 0;
		}
		setVersion(version);
	}

	public boolean setVersion(String version) {
		if (!isBranded()) {
			return false;
		}

		if (brand.isBrandVersioned()) {
			brand.getBrandVersion().setBrandVersionName(version);
		} else {
			brand = new Brand(brand.getItemName(), brand.getBrandName(), version, brand.getBrandDesc(), brand.getQuantity(),
			                  brand.getPrice());
		}
		return true;
	}

	public String getUsefulUnit() {
		return usefulUnit;
	}

	public void setUsefulUnit(String usefulUnit) {
		this.usefulUnit = usefulUnit;
	}

	public float getUsefulUnitsPerActual() {
		return usefulUnitsPerActual;
	}

	public void setUsefulUnitsPerActual(float usefulUnitsPerActual) {
		this.usefulUnitsPerActual = usefulUnitsPerActual;
	}

}
