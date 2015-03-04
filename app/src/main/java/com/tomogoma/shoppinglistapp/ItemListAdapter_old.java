package com.tomogoma.shoppinglistapp;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.tomogoma.util.Formatter;

import java.util.List;

/**
 * Created by ogoma on 24/02/15.
 */
public class ItemListAdapter_old extends ArrayAdapter<Item> {

	private final Activity activity;
	private final List<Item> items;

	public ItemListAdapter_old(Activity activity, List<Item> items) {

		super(activity, R.layout.list_item, items);
		this.activity = activity;
		this.items = items;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View rowView = convertView;

		ViewHolder viewHolder;
		if (rowView == null) {

			LayoutInflater inflater = activity.getLayoutInflater();
			rowView = inflater.inflate(R.layout.list_item, null);

			viewHolder = new ViewHolder();
			viewHolder.title = (TextView) rowView.findViewById(R.id.title);
			viewHolder.quantity = (TextView) rowView.findViewById(R.id.quantityLayout);
			viewHolder.brandVersion = (TextView) rowView.findViewById(R.id.version);
			viewHolder.unitPrice = (TextView) rowView.findViewById(R.id.unitPrice);
			viewHolder.totalPrice = (TextView) rowView.findViewById(R.id.totalPrice);
			viewHolder.addToShoppingList = (CheckBox) rowView.findViewById(R.id.shoppingListAdd);
			rowView.setTag(viewHolder);
		} else {

			viewHolder = (ViewHolder) rowView.getTag();
		}

		final Item item = items.get(position);

		double unitPrice = item.getUnitPrice();
		double totalPrice = item.getTotalPrice();

		viewHolder.title.setText(item.getItemName());
		viewHolder.quantity.setText(item.getHumanQuantity());
		viewHolder.unitPrice.setText(Formatter.formatUnitPrice(unitPrice));
		viewHolder.brandVersion.setText(item.getVersion());
		viewHolder.totalPrice.setText(Formatter.formatPrice(totalPrice));
		viewHolder.addToShoppingList.setChecked(item.isInShoppingList());

		viewHolder.addToShoppingList.setOnCheckedChangeListener(
				new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						item.setInShoppingList(isChecked);
					}
				});

		return rowView;
	}

	private static class ViewHolder {
		TextView title, quantity, brandVersion, unitPrice, totalPrice;
		CheckBox addToShoppingList;
	}
}
