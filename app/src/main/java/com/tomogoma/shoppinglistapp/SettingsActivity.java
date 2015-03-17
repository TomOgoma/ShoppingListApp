package com.tomogoma.shoppinglistapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;

import com.tomogoma.shoppinglistapp.data.Currency;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.CurrencyEntry;
import com.tomogoma.util.Formatter;

import java.util.List;

public class SettingsActivity extends PreferenceActivity {

	private static final boolean ALWAYS_SIMPLE_PREFS = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setupActionBar();
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Show the Up button in the action bar.
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		setupSimplePreferencesScreen();
	}

	/**
	 * Shows the simplified settings UI if the device configuration if the
	 * device configuration dictates that a simplified, single-pane UI should be
	 * shown.
	 */
	private void setupSimplePreferencesScreen() {

		if (!isSimplePreferences(this)) {
			return;
		}

		PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);
		PreferenceCategory header = new PreferenceCategory(this);

		//  General
		ListPreference currencyPreference = prepareCurrencyPreference(this);
		root.addPreference(currencyPreference);
		setPreferenceScreen(root);

		// Notifications
		header.setTitle(R.string.pref_header_notifications);
		getPreferenceScreen().addPreference(header);
		addPreferencesFromResource(R.xml.pref_notification);

		// Data Sync
		header = new PreferenceCategory(this);
		header.setTitle(R.string.pref_header_data_sync);
		getPreferenceScreen().addPreference(header);
		addPreferencesFromResource(R.xml.pref_data_sync);

		bindPreferenceSummaryToValue(currencyPreference);
		bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_new_message_notifications)));
		bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_ringtone)));
		bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_vibrate)));
		bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_sync_frequency)));
	}

	@Override
	public boolean onIsMultiPane() {
		return isXLargeTablet(this) && !isSimplePreferences(this);
	}

	private static boolean isXLargeTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	/**
	 * Determines whether the simplified settings UI should be shown. This is
	 * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
	 * doesn't have newer APIs like {@link PreferenceFragment}, or the device
	 * doesn't have an extra-large screen. In these cases, a single-pane
	 * "simplified" settings UI should be shown.
	 */
	private static boolean isSimplePreferences(Context context) {
		return ALWAYS_SIMPLE_PREFS
				|| Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
				|| !isXLargeTablet(context);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onBuildHeaders(List<Header> target) {
		if (!isSimplePreferences(this)) {
			loadHeadersFromResource(R.xml.pref_headers, target);
		}
	}

	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();

			if (preference instanceof ListPreference) {

				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);

				// Set the summary to reflect the new value.
				preference.setSummary(
						index >= 0 ?
								listPreference.getEntries()[index]:
								null
					);

			} else if (preference instanceof RingtonePreference) {
				// For ringtone preferences, look up the correct display value
				// using RingtoneManager.
				if (TextUtils.isEmpty(stringValue)) {
					// Empty values correspond to 'silent' (no ringtone).
					preference.setSummary(R.string.pref_ringtone_silent);

				} else {
					Ringtone ringtone = RingtoneManager.getRingtone(
							preference.getContext(), Uri.parse(stringValue));

					if (ringtone == null) {
						// Clear the summary if there was a lookup error.
						preference.setSummary(null);
					} else {
						// Set the summary to reflect the new ringtone display
						// name.
						String name = ringtone.getTitle(preference.getContext());
						preference.setSummary(name);
					}
				}

			} else {
				// For all other preferences, set the summary to the value's
				// simple string representation.
				preference.setSummary(stringValue);
			}
			return true;
		}
	};

	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 *
	 * @see #sBindPreferenceSummaryToValueListener
	 */
	private static void bindPreferenceSummaryToValue(Preference preference) {
		// Set the listener to watch for value changes.
		preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		// Trigger the listener immediately with the preference's
		// current value.
		String notificationKey = preference.getContext().getString(R.string.pref_key_new_message_notifications);
		String vibrateKey = preference.getContext().getString(R.string.pref_key_vibrate);
		String preferenceKey = preference.getKey();
		if (preferenceKey.equals(notificationKey) || preferenceKey.equals(vibrateKey)) {
			sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
			                                                         PreferenceManager
					                                                         .getDefaultSharedPreferences(preference.getContext())
					                                                         .getBoolean(preference.getKey(), true));
		} else {
			sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
			                                                         PreferenceManager
					                                                         .getDefaultSharedPreferences(preference.getContext())
					                                                         .getString(preference.getKey(), ""));
		}
	}

	private static ListPreference prepareCurrencyPreference(Activity activity) {

		String[] currencyProjection = new String[] {
				CurrencyEntry._ID,
				CurrencyEntry.COLUMN_CODE,
				CurrencyEntry.COLUMN_NAME,
				CurrencyEntry.COLUMN_SYMBOL,
		};
		ContentResolver contentResolver = activity.getContentResolver();
		Cursor currencyCursor = contentResolver.query(CurrencyEntry.CONTENT_URI, currencyProjection, null, null, null);

		int currencyCount = currencyCursor.getCount();
		String[] entries = new String[currencyCount];
		String[] entryValues = new String[currencyCount];

		if (currencyCursor.moveToFirst()) {

			int counter=0;
			do {

				long id = currencyCursor.getLong(currencyCursor.getColumnIndex(CurrencyEntry._ID));
				String code = currencyCursor.getString(currencyCursor.getColumnIndex(CurrencyEntry.COLUMN_CODE));
				String name = currencyCursor.getString(currencyCursor.getColumnIndex(CurrencyEntry.COLUMN_NAME));
				String symbol = currencyCursor.getString(currencyCursor.getColumnIndex(CurrencyEntry.COLUMN_SYMBOL));

				entries[counter] = Formatter.formatLongCurrency(new Currency(code, symbol, name));
				entryValues[counter] = String.valueOf(id);
				counter ++;

			} while(currencyCursor.moveToNext());

		} else {
			//  TODO handle this error
		}
		currencyCursor.close();

		ListPreference currencyPreference = new ListPreference(activity);
		currencyPreference.setKey(activity.getString(R.string.pref_key_currency));
		currencyPreference.setTitle(activity.getString(R.string.pref_title_currency));
		currencyPreference.setNegativeButtonText(null);
		currencyPreference.setPositiveButtonText(null);
		currencyPreference.setEntries(entries);
		currencyPreference.setEntryValues(entryValues);
		Log.d("Logg", "loggg");
		currencyPreference.setDefaultValue(String.valueOf(CurrencyEntry.DEFAULT_ID));

		return currencyPreference;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class GeneralPreferenceFragment extends PreferenceFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {

			super.onCreate(savedInstanceState);

			PreferenceScreen root = getPreferenceManager().createPreferenceScreen(getActivity());
			ListPreference currencyPreference = prepareCurrencyPreference(getActivity());
			root.addPreference(currencyPreference);
			bindPreferenceSummaryToValue(currencyPreference);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class NotificationPreferenceFragment extends PreferenceFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {

			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_notification);
			bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_new_message_notifications)));
			bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_ringtone)));
			bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_vibrate)));
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class DataSyncPreferenceFragment extends PreferenceFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {

			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_data_sync);
			bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_sync_frequency)));
		}
	}

}
