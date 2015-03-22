package com.tomogoma.util;

import com.tomogoma.shoppinglistapp.data.Currency;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.CurrencyEntry;

/**
 * Created by ogoma on 24/02/15.
 */
public class Formatter {

	private static final String LOG_TAG = Formatter.class.getSimpleName();
	private static final boolean ACCEPT_ZERO = false;

	public static String formatPrice(Double unitPrice, float quantity, String boundCode,
	                                 double boundConversion, Currency preferredCurrency) {

		double price = unitPrice * quantity;
		return formatPrice(price, boundCode, boundConversion, preferredCurrency);
	}

	public static String formatPrice(Double price, String boundCode,
	                                 double boundConversion, Currency preferredCurrency) {

		if (!ACCEPT_ZERO && price == 0) {
			return "-/-";
		}

		String preferredCode = preferredCurrency.getCode();

		//  convert price if preferred currency is different from the one originally bound
		if (!boundCode.equals(preferredCode)) {

			//  First convert from bound currency to our base (default) currency
			if (!boundCode.equals(CurrencyEntry.DEFAULT_CODE)) {
				price = price / boundConversion;
			}

			// Finally convert from base (default) currency to preferred currency
			if (!preferredCode.equals(CurrencyEntry.DEFAULT_CODE)) {
				price = price * preferredCurrency.getLastConversion();
			}
		}

		return new StringBuilder()
				.append(String.format("%.2f", price))
				.append(formatCurrency(preferredCurrency))
				.toString();
	}

	public static String formatUnitPrice(double price, String boundCode,
	                                     double boundConversion, Currency preferredCurrency) {

		if (!ACCEPT_ZERO && price == 0) {
			return "-/-";
		}

		return "@" + formatPrice(price, boundCode, boundConversion, preferredCurrency);
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
		if (symbol == null || symbol.isEmpty()) {
			return currency.getCode();
		}
		return symbol;
	}

	public static String formatLongCurrency(Currency currency) {

		return new StringBuilder(currency.getName())
				.append(" (")
				.append(formatCurrency(currency))
				.append(")")
				.toString();
	}

}
