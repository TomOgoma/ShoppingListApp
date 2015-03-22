package com.tomogoma.shoppinglistapp.data;

/**
 * Created by Tom Ogoma on 16/03/15.
 */
public class Currency {

	private String mCode;
	private String mSymbol;
	private double mLastConversion;
	private String mName;
	private String mCountry;

	/**
	 * Constructor
	 * Initializes the latest conversion to 1d
	 * See {@link #Currency(String, String, String, String, double)}
	 */
	public Currency(String code, String symbol, String name, String country) {
		this(code, symbol, name, country, 1d);
	}

	/**
	 * Constructor
	 * </p>
	 * Note that:
	 * </p>
	 * code cannot be null or empty
	 * </p>
	 * symbol cannot be null but an empty String is allowed
	 * </p>
	 * lastConversion cannot be <= 0
	 * </p>
	 * If country/name are null by the time their getters are called, an
	 * {@link java.lang.UnsupportedOperationException} will be thrown
	 * </p>
	 * @param code currency code (cannot be null/empty)
	 * @param symbol currency symbol (cannot be null but can be empty)
	 * @param name currency name
	 * @param country country name
	 * @param lastConversion the latest conversion rate KES->{code}
	 */
	public Currency(String code, String symbol, String name, String country, double lastConversion) {

		if (code==null||code.isEmpty()) {
			throw new IllegalArgumentException("code cannot be null/empty");
		}
		if (symbol==null) {
			throw new IllegalArgumentException("symbol cannot be empty");
		}
		if (lastConversion <= 0d) {
			throw new IllegalArgumentException("last conversion cannot be less than 1d");
		}
		mCode = code;
		mSymbol = symbol;
		mName = name;
		mCountry = country;
		mLastConversion = lastConversion;
	}

	/**
	 *
	 * @return currency code (never null/empty)
	 */
	public String getCode() {
		return mCode;
	}

	public void setCode(String mCode) {
		this.mCode = mCode;
	}

	/**
	 *
	 * @return currency symbol (never null but can be empty)
	 */
	public String getSymbol() {
		return mSymbol;
	}

	public void setSymbol(String mSymbol) {
		this.mSymbol = mSymbol;
	}

	/**
	 *
	 * @return latest conversion rate from KES cannot be <= 0d
	 */
	public double getLastConversion() {
		return mLastConversion;
	}

	public void setLastConversion(double mLastConversion) {
		this.mLastConversion = mLastConversion;
	}

	/**
	 *
	 * @return currency name
	 * @throws java.lang.UnsupportedOperationException if currency name is null/empty
	 */
	public String getName() {
		if (mName==null||mName.isEmpty()) {
			throw new UnsupportedOperationException("This currency does not have a name");
		}
		return mName;
	}

	public void setName(String mName) {
		this.mName = mName;
	}

	/**
	 *
	 * @return the country name
	 * @throws java.lang.UnsupportedOperationException when country name is null/empty
	 */
	public String getCountry() {
		if (mCountry==null||mCountry.isEmpty()) {
			throw new UnsupportedOperationException("this currency does not have a country");
		}
		return mCountry;
	}

	public void setCountry(String mCountry) {
		this.mCountry = mCountry;
	}
}
