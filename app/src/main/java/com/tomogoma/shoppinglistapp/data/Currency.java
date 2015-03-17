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

	public Currency(String code, String symbol, double lastConversion) {
		mCode = code;
		mSymbol = symbol;
		mLastConversion = lastConversion;
	}

	public Currency(String code, String symbol, String name) {
		mCode = code;
		mSymbol = symbol;
		mLastConversion = 1d;
		mName = name;
	}

	public Currency(String code, String symbol, String name, double lastConversion) {
		mCode = code;
		mSymbol = symbol;
		mLastConversion = lastConversion;
		mName = name;
	}

	public Currency(String code, String symbol, String name, String country, double lastConversion) {
		mCode = code;
		mSymbol = symbol;
		mLastConversion = lastConversion;
		mName = name;
		mCountry = country;
	}

	public String getCode() {
		return mCode;
	}

	public void setCode(String mCode) {
		this.mCode = mCode;
	}

	public String getSymbol() {
		return mSymbol;
	}

	public void setSymbol(String mSymbol) {
		this.mSymbol = mSymbol;
	}

	public double getLastConversion() {
		return mLastConversion;
	}

	public void setLastConversion(double mLastConversion) {
		this.mLastConversion = mLastConversion;
	}

	public String getName() {
		return mName;
	}

	public void setName(String mName) {
		this.mName = mName;
	}

	public String getCountry() {
		return mCountry;
	}

	public void setCountry(String mCountry) {
		this.mCountry = mCountry;
	}
}
