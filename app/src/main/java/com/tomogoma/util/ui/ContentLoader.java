package com.tomogoma.util.ui;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;

import java.util.HashMap;

/**
 * Created by ogoma on 05/03/15.
 */
public class ContentLoader implements LoaderCallbacks<Cursor> {

	private static int sLastLoaderID;
	private static HashMap<Integer, LoaderData> sLoaderDataMap = new HashMap<>();

	private Context context;
	private Fragment fragment;

	private class LoaderData {

		private int id;
		private Uri providerUri;
		private CursorAdapter cursorAdapter;
		private String[] projection;
		private String[] selectionArgs;
		private String selection;
		private String sortOrder;

		public LoaderData(Uri providerUri, CursorAdapter cursorAdapter,
		                  int id, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

			this.id = id;
			this.projection = projection;
			this.providerUri = providerUri;
			this.sortOrder = sortOrder;
			this.selection = selection;
			this.selectionArgs = selectionArgs;
			this.cursorAdapter = cursorAdapter;
		}
	}

	public ContentLoader(Context context, Fragment fragment) {

		this.context = context;
		this.fragment = fragment;
	}

	public <U extends CursorAdapter> int loadContent(Uri providerUri,
	                                                 U cursorAdapter, String[] projection, String sortOrder) {

		return loadContent(providerUri, cursorAdapter, projection, null, null, sortOrder);
	}

	public <U extends CursorAdapter> int loadContent(Uri providerUri, U cursorAdapter, String[] projection,
	                                                 String selection, String[] selectionArgs, String sortOrder) {

		LoaderData loaderData;
		synchronized (this) {
			sLastLoaderID++;
			loaderData = new LoaderData(providerUri, cursorAdapter,
			                            sLastLoaderID, projection, selection, selectionArgs, sortOrder);

		}

		sLoaderDataMap.put(loaderData.id, loaderData);
		fragment.getLoaderManager().initLoader(loaderData.id, null, this);
		return loaderData.id;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		LoaderData loaderData = sLoaderDataMap.get(id);
		if (loaderData == null) {
			return null;
		}

		return new CursorLoader(
				context,
				loaderData.providerUri,
				loaderData.projection,
				loaderData.selection,
				loaderData.selectionArgs,
				loaderData.sortOrder
		);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

		int loaderID = loader.getId();
		LoaderData loaderData = sLoaderDataMap.get(loaderID);
		if (loaderData == null) {
			return;
		}

		loaderData.cursorAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

		int loaderID = loader.getId();
		LoaderData loaderData = sLoaderDataMap.get(loaderID);
		if (loaderData == null) {
			return;
		}

		loaderData.cursorAdapter.swapCursor(null);
	}
}
