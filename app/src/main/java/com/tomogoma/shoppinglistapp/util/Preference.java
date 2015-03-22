package com.tomogoma.shoppinglistapp.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.tomogoma.shoppinglistapp.R;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.CurrencyEntry;

/**
 * Created by Tom Ogoma on 20/03/15.
 */
public class Preference {

	/**
	 *  Fraction of sync interval by which the flexible sync interval is recommended
	 */
	public static final int SYNC_FLEXTIME_DIVIDER = 3;

	private static final String LOG_TAG = Preference.class.getSimpleName();

	public static long getPreferredCurrencyID(Context context) {

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String currencyIDStr = prefs.getString(
				context.getString(R.string.pref_key_currency),
				String.valueOf(CurrencyEntry.DEFAULT_ID)
			);

		try {
			return Long.parseLong(currencyIDStr);
		} catch (Exception e) {
			return CurrencyEntry.DEFAULT_ID;
		}
	}

	/**
	 *
	 * @param context
	 * @return preferred sync interval in seconds; returns the default if an error occurs
	 * when parsing preference values or the maximum sync interval if values calculate
	 * to <= 0
	 */
	public static int getPreferredSyncInterval(Context context) {

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		String defaultIntervalMinuteString = context.getString(R.string.pref_default_sync_frequency);
		String intervalMinuteString = prefs.getString(
				context.getString(R.string.pref_key_sync_frequency),
				defaultIntervalMinuteString
			);

		Log.d(LOG_TAG, "Preferred sync interval in minutes: " + intervalMinuteString);

		int intervalMinuteInt;
		try {
			intervalMinuteInt = Integer.parseInt(intervalMinuteString);
		} catch (Exception e) {
			Log.e(LOG_TAG, "Error parsing preferred sync interval, falling back to default", e);
			e.printStackTrace();
			intervalMinuteInt = Integer.parseInt(defaultIntervalMinuteString);
		}

		//  We don't want to return any interval less than the minimum allowed (assume an error)
		int minSyncIntervalMinutes = Integer.parseInt(context.getString(R.string.pref_min_sync_frequency));
		if (intervalMinuteInt <= minSyncIntervalMinutes) {
			intervalMinuteInt = Integer.parseInt(context.getString(R.string.pref_max_sync_frequency));
		}

		//  We are returning seconds
		return  intervalMinuteInt * 60;
	}

	/**
	 * Note that the calculated flex value will always be less than sync interval provided
	 * @param syncIntervalSec the sync interval for which to calculate flex sync interval
	 * @return  flex sync interval in seconds
	 * @throws java.lang.IllegalArgumentException if syncIntervalSec < minimum sync interval
	 */
	public static int getPreferredSyncFlexInterval(Context context, int syncIntervalSec) {

		int minSyncIntervalSecs = Integer.parseInt(context.getString(R.string.pref_min_sync_frequency)) * 60;

		//  return a third of sync interval if sync-flex interval will end up >= [minimum interval] * 2
		if (syncIntervalSec >= (minSyncIntervalSecs*2*SYNC_FLEXTIME_DIVIDER)) {
			return syncIntervalSec/SYNC_FLEXTIME_DIVIDER;
		}

		//  otherwise return Sync interval less five minutes
		//  Throw exception if [sync interval] < [minimum interval]
		if (syncIntervalSec >= minSyncIntervalSecs) {
			return syncIntervalSec - 60 * 5;
		} else {
			throw new IllegalArgumentException(
					"syncIntervalSec provided is less than minimum allowed sync frequency: "
					 + minSyncIntervalSecs + "seconds"
				);
		}
	}
}
