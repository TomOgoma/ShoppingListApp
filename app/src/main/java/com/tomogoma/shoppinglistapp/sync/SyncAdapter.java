package com.tomogoma.shoppinglistapp.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.tomogoma.shoppinglistapp.R;
import com.tomogoma.shoppinglistapp.data.DatabaseContract;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.CurrencyEntry;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.ItemEntry;
import com.tomogoma.util.PreferenceUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

/**
 * Created by Tom Ogoma on 20/03/15.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

	public static final String LOG_TAG = SyncAdapter.class.getSimpleName();

	//  API allows maximum of 10 conversions per query, using 9 to be safe
	private final double MAX_CONVERSIONS_PER_QUERY = 8d;

	public SyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

		HttpURLConnection urlConnection = null;
		BufferedReader reader = null;
		Vector<String> convertedCurrenciesJsonStr = new Vector<>();

		try {

			Vector<Uri> uris = prepareFetchConversionUri();

			for (Uri uri: uris) {

				Log.i(LOG_TAG, "Loading url: " + uri);
				URL url = new URL(uri.toString());

				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.connect();

				InputStream inputStream = urlConnection.getInputStream();
				StringBuffer buffer = new StringBuffer();
				if (inputStream == null) {
					Log.e(LOG_TAG, "Received no data for uri: " + uri.toString());
					continue;
				}

				reader = new BufferedReader(new InputStreamReader(inputStream));
				String line;
				while ((line = reader.readLine()) != null) {
					//  for debugging purposes
					buffer.append(line + "\n");
				}

				if (buffer.length() == 0) {
					Log.e(LOG_TAG, "Buffer turned out empty for uri: " + uri.toString());
					continue;
				}
				convertedCurrenciesJsonStr.add(buffer.toString());
			}
		}
		catch (IOException e) {
			Log.e(LOG_TAG, "IOException: " + e.getMessage(), e);
			e.printStackTrace();
			return;
		}
		finally {
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

		//  TODO Optimize (get all at once and update into db as one batch)
		for (String convertedCurrencyJsonStr: convertedCurrenciesJsonStr) {
			try {
				getConversionDataFromJson(convertedCurrencyJsonStr);
			}
			catch (JSONException e) {
				Log.e(LOG_TAG, "Error parsing json String " + convertedCurrencyJsonStr, e);
				e.printStackTrace();
			}
		}
	}

	private Vector<Uri> prepareFetchConversionUri() {

		final String[] WATCHED_CURRENCIES_PROJECTION = new String[] {
				CurrencyEntry.COLUMN_CODE
		};

		final String BASE_URL = "http://www.freecurrencyconverterapi.com/api/";
		final String API_VERSION = "v3";
		final String DATA_SET = "convert";
		final String QUERY_PARAM = "q";

		String preferredCode = "";

		ContentResolver contentResolver = getContext().getContentResolver();

		long prefdCurrencyID = PreferenceUtils.getPreferredCurrencyID(getContext());
		Cursor cursor = contentResolver.query(
				CurrencyEntry.buildCurrencyUri(prefdCurrencyID),
				WATCHED_CURRENCIES_PROJECTION,
				null, null, null
			);
		if (cursor.moveToFirst()) {
			preferredCode = cursor.getString(cursor.getColumnIndex(CurrencyEntry.COLUMN_CODE));
			//Log.d(LOG_TAG, "Got preferred code: " + preferredCode);
		}
		cursor.close();
		//Log.d(LOG_TAG, "Default code is: " + CurrencyEntry.DEFAULT_CODE);

		cursor = contentResolver.query(
				ItemEntry.buildItemsCurrenciesUri(),
		        WATCHED_CURRENCIES_PROJECTION,
		        null, null, null
			);

		int noOfRequests = (int) Math.ceil(cursor.getCount()/MAX_CONVERSIONS_PER_QUERY);
		Vector<Uri> uris = new Vector<>(noOfRequests);

		if (cursor.moveToFirst()) {

			int count;
			StringBuilder queryStrBuilder;

			if (preferredCode.isEmpty() || preferredCode.equals(CurrencyEntry.DEFAULT_CODE)) {

				count = 0;
				queryStrBuilder = new StringBuilder();

				//if (preferredCode.isEmpty()) {
				//	Log.d(LOG_TAG,"Preferred code is empty");
				//} else {
				//	Log.d(LOG_TAG, "Preferred code equals default code");
				//}
			}
			else {
				count = 1;
				queryStrBuilder = new StringBuilder(CurrencyEntry.DEFAULT_CODE).append("_").append(preferredCode);
				//Log.d(LOG_TAG,"preInsert query: " + queryStrBuilder);
			}

			while(true) {

				String code = cursor.getString(cursor.getColumnIndex(CurrencyEntry.COLUMN_CODE));
				//Log.d(LOG_TAG,"working on code: " + code + "...");

				//  No need to convert default currency to itself or reconvert to preferred code
				if (!CurrencyEntry.DEFAULT_CODE.equals(code) && !preferredCode.equals(code)) {
					//Log.d(LOG_TAG, "\tcode neither default nor preferred");

					//  Queries are comma separated
					if (count > 0) {
						queryStrBuilder.append(",");
						//Log.d(LOG_TAG, "adding comma separator for next: " + queryStrBuilder);
					}

					//  The query to be processed by the API e.g. KES_USD converts KES to USD
					queryStrBuilder.append(CurrencyEntry.DEFAULT_CODE).append("_").append(code);
					//Log.d(LOG_TAG, "\tadded working code to query: " + queryStrBuilder);
					count++;
				}
				//else {
				//	Log.d(LOG_TAG, "\tcode is either default or preferred, ignoring...");
				//}

				boolean hasNext = cursor.moveToNext();
				if (!hasNext || count >= MAX_CONVERSIONS_PER_QUERY) {

					String queryString = queryStrBuilder.toString();
					if (!queryString.isEmpty()) {

						//Log.d(LOG_TAG, "constructing uri based on query: " + queryStrBuilder);
						Uri uri = Uri.parse(BASE_URL).buildUpon()
						             .appendPath(API_VERSION)
						             .appendPath(DATA_SET)
						             .appendQueryParameter(QUERY_PARAM, queryStrBuilder.toString())
						             .build();
						uris.add(uri);
					}
					//else {
					//	Log.d(LOG_TAG, "nothing added to query string, no need to create uri");
					//}

					count = 0;
					queryStrBuilder = new StringBuilder();
				}

				if (!hasNext) {
					break;
				}
			}
		}
		cursor.close();

		return uris;
	}

	private void getConversionDataFromJson(String conversionJsonStr) throws JSONException {

		final String FCCA_CONVERSION = "val";
		final String FCCA_CODE_TO = "to";
		final String FCCA_CODE_FROM = "fr";

		final String FCCA_LIST = "results";

		JSONObject conversionsObject = new JSONObject(conversionJsonStr).getJSONObject(FCCA_LIST);
		Iterator<String> conversionKeysIterator = conversionsObject.keys();

		ArrayList<ContentProviderOperation> cPOList =  new ArrayList<>((int)MAX_CONVERSIONS_PER_QUERY);

		String selection = CurrencyEntry.COLUMN_CODE + " =? ";

		while (conversionKeysIterator.hasNext()) {

			String conversionKey = conversionKeysIterator.next();
			JSONObject conversionObject = conversionsObject.getJSONObject(conversionKey);

			String codeFrom = conversionObject.getString(FCCA_CODE_FROM);
			if(! codeFrom.equals(CurrencyEntry.DEFAULT_CODE)) {
				Log.e(LOG_TAG, "Got an unexpected conversion; from code: " + codeFrom);
				continue;
			}

			String conversion, code;
			String[] selectionArgs;

			code = conversionObject.getString(FCCA_CODE_TO);
			conversion = conversionObject.getString(FCCA_CONVERSION);
			selectionArgs = new String[]{code};

			ContentProviderOperation contentProviderOperation =
					ContentProviderOperation
							.newUpdate(CurrencyEntry.CONTENT_URI)
							.withValue(CurrencyEntry.COLUMN_LAST_CONVERSION, conversion)
							.withSelection(selection, selectionArgs)
							.build();
			cPOList.add(contentProviderOperation);
		}
		if (cPOList.size() > 0) {
			try {
				getContext().getContentResolver().applyBatch(DatabaseContract.CONTENT_AUTHORITY, cPOList);
			} catch (RemoteException e) {
				Log.e(LOG_TAG, "Remote Exception caught: " + e.getMessage());
			} catch (OperationApplicationException e) {
				Log.e(LOG_TAG, "OperationApplication Exception caught: " + e.getMessage());
			}
			//getContext().getContentResolver().bulkInsert(CurrencyEntry.CONTENT_URI, cpoArray);
		}
	}

	public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
		Log.d(LOG_TAG, "Sync intervals (seconds) [interval/flex]: " + syncInterval + "/" + flexTime);
		Account account = getSyncAccount(context);
		String authority = context.getString(R.string.content_authority);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// we can enable inexact timers in our periodic sync
			SyncRequest request = new SyncRequest.Builder().
					                                               syncPeriodic(syncInterval, flexTime).
					                                               setSyncAdapter(account, authority).build();
			ContentResolver.requestSync(request);
		} else {
			ContentResolver.addPeriodicSync(account,
			                                authority, new Bundle(), syncInterval);
		}
	}

	public static void syncImmediately(Context context) {
		Bundle bundle = new Bundle();
		bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
		bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
		ContentResolver.requestSync(getSyncAccount(context),
		                            context.getString(R.string.content_authority), bundle);
	}

	public static Account getSyncAccount(Context context) {

		AccountManager accountManager =
				(AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

		// Create the account type and default account
		Account newAccount = new Account(
				context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

		// If the password doesn't exist, the account doesn't exist
		if ( null == accountManager.getPassword(newAccount) ) {

			if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
				return null;
			}
			onAccountCreated(newAccount, context);
		}
		return newAccount;
	}


	private static void onAccountCreated(Account newAccount, Context context) {

		int syncInterval = PreferenceUtils.getPreferredSyncInterval(context);
		int syncFlexTime = PreferenceUtils.getPreferredSyncFlexInterval(context, syncInterval);
		SyncAdapter.configurePeriodicSync(context, syncInterval, syncFlexTime);
		ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
		syncImmediately(context);
	}

	public static void initializeSyncAdapter(Context context) {
		getSyncAccount(context);
	}


}
