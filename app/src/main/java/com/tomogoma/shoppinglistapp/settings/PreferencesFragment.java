package com.tomogoma.shoppinglistapp.settings;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.Log;

import com.tomogoma.shoppinglistapp.R;
import com.tomogoma.shoppinglistapp.data.Currency;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.CurrencyEntry;
import com.tomogoma.shoppinglistapp.sync.CurrencySyncAdapter;
import com.tomogoma.shoppinglistapp.util.Formatter;
import com.tomogoma.shoppinglistapp.util.Preference;
import com.tomogoma.shoppinglistapp.util.UI;

/**
 * Created by Tom Ogoma on 20/03/15.
 */
public class PreferencesFragment extends PreferenceFragment
		implements LoaderCallbacks<Cursor> {

	private static final String LOG_TAG = PreferencesFragment.class.getSimpleName();
	private static final int CURRENCY_LOADER_ID = 90;

	private static  boolean sIsBindingPreference;

	private ListPreference mCurrencyPreference;
	private Cursor mCurrencyCursor;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		prepareCurrencyPreference();
		PreferenceScreen root = getPreferenceManager().createPreferenceScreen(getActivity());
		setPreferenceScreen(root);

		//  General (currency...)
		PreferenceCategory header = new PreferenceCategory(getActivity());
		header.setTitle(R.string.pref_header_general);
		root.addPreference(header);
		root.addPreference(mCurrencyPreference);

		//  Notification
		header = new PreferenceCategory(getActivity());
		header.setTitle(R.string.pref_header_notifications);
		root.addPreference(header);
		addPreferencesFromResource(R.xml.pref_notification);


		//  Sync
		header = new PreferenceCategory(getActivity());
		header.setTitle(R.string.pref_header_data_sync);
		root.addPreference(header);
		addPreferencesFromResource(R.xml.pref_data_sync);

		bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_new_message_notifications)));
		bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_ringtone)));
		bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_vibrate)));
		bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_sync_frequency)));
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(CURRENCY_LOADER_ID, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		if (id != CURRENCY_LOADER_ID) {
			return null;
		}

		String[] projection = new String[] {
				CurrencyEntry._ID,
				CurrencyEntry.COLUMN_CODE,
				CurrencyEntry.COLUMN_NAME,
				CurrencyEntry.COLUMN_SYMBOL,
				CurrencyEntry.COLUMN_COUNTRY
		};

		String sortOrder =  CurrencyEntry.COLUMN_NAME + " ASC";

		return new CursorLoader(
				getActivity(),
				CurrencyEntry.CONTENT_URI,
				projection,
				null,
				null,
				sortOrder
		);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

		if (loader.getId() ==  CURRENCY_LOADER_ID) {
			mCurrencyCursor = data;
			setCurrencyValues();
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

		if (loader.getId() == CURRENCY_LOADER_ID) {
			mCurrencyCursor = null;
		}
	}

	private void setCurrencyValues() {

		int currencyCount = mCurrencyCursor.getCount();
		String[] entries = new String[currencyCount];
		String[] entryValues = new String[currencyCount];

		if (mCurrencyCursor.moveToFirst()) {

			boolean hasNext;
			String previousName = "";
			String nextName = mCurrencyCursor.getString(mCurrencyCursor.getColumnIndex(CurrencyEntry.COLUMN_NAME));
			int counter=0;
			do {

				long id = mCurrencyCursor.getLong(mCurrencyCursor.getColumnIndex(CurrencyEntry._ID));
				String code = mCurrencyCursor.getString(mCurrencyCursor.getColumnIndex(CurrencyEntry.COLUMN_CODE));
				String name = nextName;
				String symbol = mCurrencyCursor.getString(mCurrencyCursor.getColumnIndex(CurrencyEntry.COLUMN_SYMBOL));
				String country = mCurrencyCursor.getString(mCurrencyCursor.getColumnIndex(CurrencyEntry.COLUMN_COUNTRY));

				Currency currency = new Currency(code, symbol, name, country);
				String entry = null;
				hasNext = mCurrencyCursor.moveToNext();

				if (hasNext) {

					nextName = mCurrencyCursor.getString(mCurrencyCursor.getColumnIndex(CurrencyEntry.COLUMN_NAME));
					if (nextName.equals(name)) {
						entry  = Formatter.formatLongCurrency(currency, true);
					}
				}

				if (name.equals(previousName) && !nextName.equals(name)) {
					entry  = Formatter.formatLongCurrency(currency, true);
				}

				if (entry == null) {
					entry   = Formatter.formatLongCurrency(currency, false);
				}

				entries[counter] = entry;
				entryValues[counter] = String.valueOf(id);

				previousName = name;
				counter ++;

			} while(hasNext);

		} else {
			//  TODO handle this error
			Log.e(LOG_TAG, "Cursor returned no rows; not even the default row; count: " + mCurrencyCursor.getCount());
			entries = new String[1];
			entryValues = new String[1];
			entries[0] = CurrencyEntry.DEFAULT_NAME;
			entryValues[0] = String.valueOf(CurrencyEntry.DEFAULT_ID);
			UI.showToast(getActivity(), getString(R.string.error_toast_fetch_currencies));
		}

		mCurrencyPreference.setEntries(entries);
		mCurrencyPreference.setEntryValues(entryValues);
		bindPreferenceSummaryToValue(mCurrencyPreference);
	}

	private void prepareCurrencyPreference() {

		mCurrencyPreference = new ListPreference(getActivity());
		mCurrencyPreference.setKey(getString(R.string.pref_key_currency));
		mCurrencyPreference.setTitle(getString(R.string.pref_title_currency));
		mCurrencyPreference.setDialogTitle(R.string.pref_title_currency);
		mCurrencyPreference.setNegativeButtonText(null);
		mCurrencyPreference.setPositiveButtonText(null);
		mCurrencyPreference.setDefaultValue(String.valueOf(CurrencyEntry.DEFAULT_ID));
	}

	private static android.preference.Preference.OnPreferenceChangeListener
			sBindPreferenceSummaryToValueListener
			= new android.preference.Preference.OnPreferenceChangeListener() {

		@Override
		public boolean onPreferenceChange(android.preference.Preference preference, Object value) {

			String stringValue = value.toString();

			if (preference instanceof ListPreference) {

				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);
				CharSequence entry = index >= 0 ? listPreference.getEntries()[index]: null;

				preference.setSummary(entry);

				//  Preference that need immediate update after user change
				if (!sIsBindingPreference) {

					String preferenceKey = preference.getKey();
					String syncKey = preference.getContext().getString(R.string.pref_key_sync_frequency);
					String currencyKey = preference.getContext().getString(R.string.pref_key_currency);

					//  For currency, request an immediate sync (for sake of currency conversions)
					if (preferenceKey.equals(currencyKey)) {
						CurrencySyncAdapter.syncImmediately(preference.getContext());
					}
					//  For sync interval, configure periodic sync immediately
					else if (preference.getKey().equals(syncKey)) {
						Context context = preference.getContext();
						int syncInterval = Integer.parseInt(stringValue) * 60;
						int syncFlexTime = Preference.getPreferredSyncFlexInterval(context, syncInterval);
						CurrencySyncAdapter.configurePeriodicSync(context, syncInterval, syncFlexTime);
					}
				}

			}
			else if (preference instanceof RingtonePreference) {
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

			} else if(preference instanceof CheckBoxPreference) {

				if (stringValue.equalsIgnoreCase("true")) {
					preference.setSummary("On");
				} else {
					preference.setSummary("Off");
				}
			}
			else {
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
	private static void bindPreferenceSummaryToValue(android.preference.Preference preference) {

		sIsBindingPreference = true;
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
		sIsBindingPreference = false;
	}

}
