/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tomogoma.shoppinglistapp;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;
import android.util.Log;

import com.tomogoma.shoppinglistapp.data.DatabaseContract;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.CategoryEntry;
import com.tomogoma.shoppinglistapp.data.DatabaseContract.ItemEntry;

import static com.tomogoma.shoppinglistapp.TestDB.testItem;

public class TestProvider extends AndroidTestCase {

	public static final String LOG_TAG = TestProvider.class.getSimpleName();
	static final String KALAMAZOO_LOCATION_SETTING = "kalamazoo";
	static final String KALAMAZOO_WEATHER_START_DATE = "20140625";
	long locationRowId;

	public void setUp() {
		deleteAllRecords();
	}

	public void deleteAllRecords() {
		mContext.getContentResolver().delete(DatabaseContract.VersionEntry.CONTENT_URI, null, null);
		mContext.getContentResolver().delete(DatabaseContract.BrandEntry.CONTENT_URI, null, null);
		mContext.getContentResolver().delete(ItemEntry.CONTENT_URI, null, null);
		mContext.getContentResolver().delete(CategoryEntry.CONTENT_URI, null, null);

		Cursor cursor = mContext.getContentResolver().query(DatabaseContract.VersionEntry.CONTENT_URI,
		                                                    null, null, null, null);
		assertEquals(0, cursor.getCount());
		cursor.close();

		cursor = mContext.getContentResolver().query(DatabaseContract.BrandEntry.CONTENT_URI,
		                                             null, null, null, null);
		assertEquals(0, cursor.getCount());
		cursor.close();

		cursor = mContext.getContentResolver().query(ItemEntry.CONTENT_URI,
		                                             null, null, null, null);
		assertEquals(0, cursor.getCount());
		cursor.close();

		cursor = mContext.getContentResolver().query(CategoryEntry.CONTENT_URI,
		                                             null, null, null, null);
		assertEquals(0, cursor.getCount());
		cursor.close();
	}

	public void testInsertReadProvider() {

		ContentValues catTestValues = testItem.packageCategoryForDB();

		Uri categoryUri = mContext.getContentResolver().insert(CategoryEntry.CONTENT_URI, catTestValues);
		long categoryRowId = ContentUris.parseId(categoryUri);

		assertTrue(locationRowId != -1);

		Cursor cursor = mContext
				.getContentResolver()
				.query(CategoryEntry.CONTENT_URI, null, null, null, null);

		TestDB.validateCategoryCursor(cursor, TestDB.testItem);

		cursor = mContext.getContentResolver()
		                 .query(CategoryEntry.buildCategoryUri(locationRowId), null, null, null, null);

		TestDB.validateCategoryCursor(cursor, testItem);

		ContentValues itemTestValues = testItem.packageForDB();

		Uri itemInsertUri = mContext.getContentResolver()
		                            .insert(ItemEntry.CONTENT_URI, itemTestValues);
		assertTrue(itemInsertUri != null);

		Cursor itemCursor = mContext.getContentResolver().query(
				ItemEntry.CONTENT_URI, null, null, null, null);

		TestDB.validateItemCursor(itemCursor, testItem);


		// Add the location values in with the weather data so that we can make
		// sure that the join worked and we actually get all the values back
		addAllContentValues(weatherValues, catTestValues);

		// Get the joined Weather and Location data
		weatherCursor = mContext.getContentResolver().query(
				WeatherEntry.buildWeatherLocation(TestDb.TEST_LOCATION),
				null, // leaving "columns" null just returns all the columns.
				null, // cols for "where" clause
				null, // values for "where" clause
				null  // sort order
		);
		TestDb.validateCursor(weatherCursor, weatherValues);

		// Get the joined Weather and Location data with a start date
		weatherCursor = mContext.getContentResolver().query(
				WeatherEntry.buildWeatherLocationWithStartDate(
						TestDb.TEST_LOCATION, TestDb.TEST_DATE),
				null, // leaving "columns" null just returns all the columns.
				null, // cols for "where" clause
				null, // values for "where" clause
				null  // sort order
		);
		TestDb.validateCursor(weatherCursor, weatherValues);

		// Get the joined Weather data for a specific date
		weatherCursor = mContext.getContentResolver().query(
				WeatherEntry.buildWeatherLocationWithDate(TestDb.TEST_LOCATION, TestDb.TEST_DATE),
				null,
				null,
				null,
				null
		);
		TestDb.validateCursor(weatherCursor, weatherValues);
	}

	// The target api annotation is needed for the call to keySet -- we wouldn't want
	// to use this in our app, but in a test it's fine to assume a higher target.
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	void addAllContentValues(ContentValues destination, ContentValues source) {
		for (String key : source.keySet()) {
			destination.put(key, source.getAsString(key));
		}
	}

	public void testGetType() {
		// content://com.example.android.sunshine.app/weather/
		String type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
		// vnd.android.cursor.dir/com.example.android.sunshine.app/weather
		assertEquals(WeatherEntry.CONTENT_TYPE, type);

		String testLocation = "94074";
		// content://com.example.android.sunshine.app/weather/94074
		type = mContext.getContentResolver().getType(
				WeatherEntry.buildWeatherLocation(testLocation));
		// vnd.android.cursor.dir/com.example.android.sunshine.app/weather
		assertEquals(WeatherEntry.CONTENT_TYPE, type);

		String testDate = "20140612";
		// content://com.example.android.sunshine.app/weather/94074/20140612
		type = mContext.getContentResolver().getType(
				WeatherEntry.buildWeatherLocationWithDate(testLocation, testDate));
		// vnd.android.cursor.item/com.example.android.sunshine.app/weather
		assertEquals(WeatherEntry.CONTENT_ITEM_TYPE, type);

		// content://com.example.android.sunshine.app/location/
		type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
		// vnd.android.cursor.dir/com.example.android.sunshine.app/location
		assertEquals(LocationEntry.CONTENT_TYPE, type);

		// content://com.example.android.sunshine.app/location/1
		type = mContext.getContentResolver().getType(LocationEntry.buildLocationUri(1L));
		// vnd.android.cursor.item/com.example.android.sunshine.app/location
		assertEquals(LocationEntry.CONTENT_ITEM_TYPE, type);
	}

	public void testUpdateLocation() {
		// Create a new map of values, where column names are the keys
		ContentValues values = TestDb.createNorthPoleLocationValues();

		Uri locationUri = mContext.getContentResolver().
				insert(LocationEntry.CONTENT_URI, values);
		long locationRowId = ContentUris.parseId(locationUri);

		// Verify we got a row back.
		assertTrue(locationRowId != -1);
		Log.d(LOG_TAG, "New row id: " + locationRowId);

		ContentValues updatedValues = new ContentValues(values);
		updatedValues.put(LocationEntry._ID, locationRowId);
		updatedValues.put(LocationEntry.COLUMN_CITY_NAME, "Santa's Village");

		int count = mContext.getContentResolver().update(
				LocationEntry.CONTENT_URI, updatedValues, LocationEntry._ID + "= ?",
				new String[]{Long.toString(locationRowId)});

		assertEquals(count, 1);

		// A cursor is your primary interface to the query results.
		Cursor cursor = mContext.getContentResolver().query(
				LocationEntry.buildLocationUri(locationRowId),
				null,
				null, // Columns for the "where" clause
				null, // Values for the "where" clause
				null // sort order
		);

		TestDb.validateCursor(cursor, updatedValues);
	}

	// Make sure we can still delete after adding/updating stuff
	public void testDeleteRecordsAtEnd() {
		deleteAllRecords();
	}

	public void testUpdateAndReadWeather() {
		insertKalamazooData();
		String newDescription = "Cats and Frogs (don't warn the tadpoles!)";

		// Make an update to one value.
		ContentValues kalamazooUpdate = new ContentValues();
		kalamazooUpdate.put(WeatherEntry.COLUMN_SHORT_DESC, newDescription);

		mContext.getContentResolver().update(
				WeatherEntry.CONTENT_URI, kalamazooUpdate, null, null);

		// A cursor is your primary interface to the query results.
		Cursor weatherCursor = mContext.getContentResolver().query(
				WeatherEntry.CONTENT_URI,
				null,
				null,
				null,
				null
		);

		// Make the same update to the full ContentValues for comparison.
		ContentValues kalamazooAltered = createKalamazooWeatherValues(locationRowId);
		kalamazooAltered.put(WeatherEntry.COLUMN_SHORT_DESC, newDescription);

		TestDb.validateCursor(weatherCursor, kalamazooAltered);
	}

	// Inserts both the location and weather data for the Kalamazoo data set.
	public void insertKalamazooData() {
		ContentValues kalamazooLocationValues = createKalamazooLocationValues();
		Uri locationInsertUri = mContext.getContentResolver()
		                                .insert(LocationEntry.CONTENT_URI, kalamazooLocationValues);
		assertTrue(locationInsertUri != null);

		locationRowId = ContentUris.parseId(locationInsertUri);

		ContentValues kalamazooWeatherValues = createKalamazooWeatherValues(locationRowId);
		Uri weatherInsertUri = mContext.getContentResolver()
		                               .insert(WeatherEntry.CONTENT_URI, kalamazooWeatherValues);
		assertTrue(weatherInsertUri != null);
	}

	static ContentValues createKalamazooWeatherValues(long locationRowId) {
		ContentValues weatherValues = new ContentValues();
		weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationRowId);
		weatherValues.put(WeatherEntry.COLUMN_DATETEXT, KALAMAZOO_WEATHER_START_DATE);
		weatherValues.put(WeatherEntry.COLUMN_DEGREES, 1.2);
		weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 1.5);
		weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 1.1);
		weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, 85);
		weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, 35);
		weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, "Cats and Dogs");
		weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, 3.4);
		weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 42);

		return weatherValues;
	}

	static ContentValues createKalamazooLocationValues() {
		// Create a new map of values, where column names are the keys
		ContentValues testValues = new ContentValues();
		testValues.put(LocationEntry.COLUMN_LOCATION_SETTING, KALAMAZOO_LOCATION_SETTING);
		testValues.put(LocationEntry.COLUMN_CITY_NAME, "Kalamazoo");
		testValues.put(LocationEntry.COLUMN_COORD_LAT, 42.2917);
		testValues.put(LocationEntry.COLUMN_COORD_LONG, -85.5872);

		return testValues;
	}

	public void testRemoveHumidityAndReadWeather() {
		insertKalamazooData();

		mContext.getContentResolver().delete(WeatherEntry.CONTENT_URI,
		                                     WeatherEntry.COLUMN_HUMIDITY + " = " + locationRowId, null);

		// A cursor is your primary interface to the query results.
		Cursor weatherCursor = mContext.getContentResolver().query(
				WeatherEntry.CONTENT_URI,
				null,
				null,
				null,
				null
		);

		// Make the same update to the full ContentValues for comparison.
		ContentValues kalamazooAltered = createKalamazooWeatherValues(locationRowId);
		kalamazooAltered.remove(WeatherEntry.COLUMN_HUMIDITY);

		TestDb.validateCursor(weatherCursor, kalamazooAltered);
		int idx = weatherCursor.getColumnIndex(WeatherEntry.COLUMN_HUMIDITY);
		assertEquals(-1, idx);
	}
}
