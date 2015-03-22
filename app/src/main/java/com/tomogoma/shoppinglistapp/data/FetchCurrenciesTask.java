package com.tomogoma.shoppinglistapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.tomogoma.shoppinglistapp.R;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.CurrencyEntry;
import com.tomogoma.shoppinglistapp.util.UI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Vector;

/**
 * Created by Tom Ogoma on 19/03/15.
 */
public class FetchCurrenciesTask  extends AsyncTask<Void, String, Void> {

	private final String LOG_TAG = FetchCurrenciesTask.class.getSimpleName();
	private final Context mContext;

	public FetchCurrenciesTask(Context context) {
		mContext = context;
	}

	private void getCurrencyDataFromJson(String currencyJsonStr) throws JSONException {

		final String FCCA_CODE = "currencyId";
		final String FCCA_NAME = "currencyName";
		final String FCCA_SYMBOL = "currencySymbol";
		final String FCCA_COUNTRY = "name";

		final String FCCA_LIST = "results";

		JSONObject currenciesObject = new JSONObject(currencyJsonStr).getJSONObject(FCCA_LIST);
		Iterator<String> currenciesIterator = currenciesObject.keys();

		//  Setting size based on known number of countries with exclusive currencies
		//  at time of coding (reduces resizing cost of Vector for the known countries
		//  with knowledge that Vector can increase its capacity if the number of countries
		//  is ever greater than the set size). Let's hope the number of countries will never
		//  significantly go below our set capacity (for space optimization purposes?)
		Vector<ContentValues> cVList =  new Vector<>(100);
		while (currenciesIterator.hasNext()) {

			String currencyStr = currenciesIterator.next();
			JSONObject currencyObject = currenciesObject.getJSONObject(currencyStr);

			String name, code, symbol, countryName;

			symbol = currencyObject.optString(FCCA_SYMBOL);
			code = currencyObject.getString(FCCA_CODE);
			name = currencyObject.getString(FCCA_NAME);
			countryName = currencyObject.getString(FCCA_COUNTRY);

			ContentValues currencyValues = new ContentValues();
			currencyValues.put(CurrencyEntry.COLUMN_CODE, code);
			currencyValues.put(CurrencyEntry.COLUMN_COUNTRY, countryName);
			currencyValues.put(CurrencyEntry.COLUMN_SYMBOL, symbol);
			currencyValues.put(CurrencyEntry.COLUMN_NAME, name);

			cVList.add(currencyValues);
		}
		if (cVList.size() > 0) {
			ContentValues[] cvArray = new ContentValues[cVList.size()];
			cVList.toArray(cvArray);
			mContext.getContentResolver().bulkInsert(CurrencyEntry.CONTENT_URI, cvArray);
		}
	}

	@Override
	protected void onProgressUpdate(String... values) {
		UI.showKeyboardToast(mContext, values[0]);
	}

	@Override
	protected Void doInBackground(Void... params) {

		HttpURLConnection urlConnection = null;
		BufferedReader reader = null;
		String currencyJsonStr = null;

		try {

			final String CURRENCY_URL_STR = "http://www.freecurrencyconverterapi.com/api/v3/countries";
			URL url = new URL(CURRENCY_URL_STR);

			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.connect();

			InputStream inputStream = urlConnection.getInputStream();
			StringBuffer buffer = new StringBuffer();
			if (inputStream == null) {
				Log.e(LOG_TAG, "Null input stream :(");
				return null;
			}
			reader = new BufferedReader(new InputStreamReader(inputStream));

			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line + "\n"); // for debugging purposes
			}

			if (buffer.length() == 0) {
				Log.e(LOG_TAG, "Buffer ended up empty");
				return null;
			}

			currencyJsonStr = buffer.toString();
		}
		catch (IOException e) {
			publishProgress(mContext.getString(R.string.error_toast_http_fetch_currencies));
			return null;
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (final IOException e) {
					Log.e(LOG_TAG, "Error closing stream", e);
					e.printStackTrace();
				}
			}
		}

		try {
			getCurrencyDataFromJson(currencyJsonStr);
		} catch (JSONException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
			e.printStackTrace();
		}
		return null;
	}
}
