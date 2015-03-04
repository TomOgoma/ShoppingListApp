package com.tomogoma.util;

/**
 * Created by ogoma on 24/02/15.
 */
public class Formatter {

	private static final boolean acceptZero = false;

	public static String formatPrice(Double unitPrice, float quantity) {

		double price = unitPrice * quantity;
		return formatPrice(price);
	}

	public static String formatPrice(Double price) {

		if (!acceptZero && price == 0) {
			return "-/-";
		}

		return new StringBuilder()
				.append(String.format("%.2f", price))
				.append(" KES").toString();
	}

	public static String formatUnitPrice(double price) {

		if (!acceptZero && price == 0) {
			return "-/-";
		}

		return "@" + formatPrice(price);
	}

	public static String formatQuantity(float quantity) {

		if (Math.abs(quantity) % 1 == 0) {
			return String.valueOf((int) quantity);
		}

		return String.valueOf(quantity);
	}

}
