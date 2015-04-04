package com.tomogoma.shoppinglistapp.util;

import com.tomogoma.shoppinglistapp.data.Currency;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.CurrencyEntry;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Created by ogoma on 24/02/15.
 */
public class Formatter {

	private static final String LOG_TAG = Formatter.class.getSimpleName();
	private static final boolean ACCEPT_ZERO = false;

	private static NumberFormat sNumberFormat = NumberFormat.getInstance();

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

		price = convertPrice(price, boundCode, boundConversion, preferredCode, preferredCurrency.getLastConversion());

		java.util.Currency currency = java.util.Currency.getInstance(preferredCode);
		sNumberFormat.setCurrency(currency);
		sNumberFormat.setGroupingUsed(true);
		sNumberFormat.setMaximumFractionDigits(2);
		sNumberFormat.setMinimumFractionDigits(2);
		if (sNumberFormat instanceof DecimalFormat) {
			((DecimalFormat) sNumberFormat).setDecimalSeparatorAlwaysShown(true);
			((DecimalFormat) sNumberFormat).setGroupingSize(3);
		}

		return new StringBuilder()
				.append(formatCurrency(preferredCurrency))
				.append(sNumberFormat.format(price))
				.toString();
	}

	public static double convertPrice(Double price, String boundCode,
	                                  double boundConversion, String preferredCode, double latestConversion) {

		//  convert price if preferred currency is different from the one originally bound
		if (!boundCode.equals(preferredCode)) {

			//  First convert from bound currency to our base (default) currency
			if (!boundCode.equals(CurrencyEntry.DEFAULT_CODE)) {
				price = price / boundConversion;
			}

			// Finally convert from base (default) currency to preferred currency
			if (!preferredCode.equals(CurrencyEntry.DEFAULT_CODE)) {
				price = price * latestConversion;
			}
		}

		return price;
	}

	public static String formatUnitPrice(double price, String boundCode,
	                                     double boundConversion, Currency preferredCurrency) {

		if (!ACCEPT_ZERO && price == 0) {
			return "-/-";
		}

		return "@ " + formatPrice(price, boundCode, boundConversion, preferredCurrency);
	}

	public static String formatQuantity(float quantity) {

		if (Math.abs(quantity) % 1 == 0) {
			return String.valueOf((int) quantity);
		}

		return String.valueOf(quantity);
	}

	public static String formatMeasUnit(String measUnit) {

		if (measUnit == null || measUnit.isEmpty()) {
			return "[Items]";
		}

		return measUnit;
	}

	public static String formatCurrency(Currency currency) {

		String symbol = currency.getSymbol();
		if (symbol.isEmpty()) {
			return currency.getCode();
		}
		return symbol;
	}

	public static String formatLongCurrency(Currency currency, boolean includeCountry) {

		StringBuilder longCurrencyBuilder = new StringBuilder(currency.getName())
				.append(" (")
				.append(formatCurrency(currency))
				.append(")");

		if (includeCountry) {
			longCurrencyBuilder
					.append(" - ")
					.append(currency.getCountry());
		}

		return longCurrencyBuilder.toString();
	}

}
