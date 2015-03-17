package com.tomogoma.util;

import com.tomogoma.shoppinglistapp.data.Currency;

/**
 * Created by ogoma on 24/02/15.
 */
public class Formatter {

	private static final boolean acceptZero = false;

	public static String formatPrice(Double unitPrice, float quantity, String saveTimeCode, double saveTimeConversion, Currency currency) {

		double price = unitPrice * quantity;
		return formatPrice(price, saveTimeCode, saveTimeConversion, currency);
	}

	public static String formatPrice(Double price, String saveTimeCode, double saveTimeConversion, Currency currency) {

		if (!acceptZero && price == 0) {
			return "-/-";
		}

		if (!saveTimeCode.equals(currency.getCode())) {
			price = price * saveTimeConversion / currency.getLastConversion();
		}

		return new StringBuilder()
				.append(String.format("%.2f", price))
				.append(formatCurrency(currency))
				.toString();
	}

	public static String formatUnitPrice(double price, String saveTimeCode, double saveTimeConversion, Currency currency) {

		if (!acceptZero && price == 0) {
			return "-/-";
		}

		return "@" + formatPrice(price, saveTimeCode, saveTimeConversion, currency);
	}

	public static String formatQuantity(float quantity) {

		if (Math.abs(quantity) % 1 == 0) {
			return String.valueOf((int) quantity);
		}

		return String.valueOf(quantity);
	}

	public static String formatMeasUnit(String measUnit) {

		if (measUnit == null || measUnit.isEmpty()) {
			return "[Item]";
		}

		return measUnit;
	}

	public static String formatCurrency(Currency currency) {

		String symbol = currency.getSymbol();
		if (symbol != null || !symbol.isEmpty()) {
			return symbol;
		}
		return currency.getCode();
	}

	public static String formatLongCurrency(Currency currency) {

		return new StringBuilder(currency.getName())
				.append(" (")
				.append(formatCurrency(currency))
				.append(")")
				.toString();
	}

}
