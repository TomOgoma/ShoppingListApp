package com.tomogoma.shoppinglistapp.data;

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
	private OnLoadFinishedListener mLoadFinishedCallBack;

	private enum ReceiverType {CURSOR, CURSOR_ADAPTER}

	private class LoaderData {

		private int id;
		private Uri providerUri;
		private ReceiverType mReceiverType;
		private Cursor cursor;
		private CursorAdapter cursorAdapter;
		private String[] projection;
		private String[] selectionArgs;
		private String selection;
		private String sortOrder;

		public LoaderData(Uri providerUri, CursorAdapter cursorAdapter,
		                  int id, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

			mReceiverType = ReceiverType.CURSOR_ADAPTER;
			this.cursorAdapter = cursorAdapter;
			initMembers(providerUri, id, projection, selection, selectionArgs, sortOrder);
		}

		public LoaderData(Uri providerUri,  int id, String[] projection,
		                  String selection, String[] selectionArgs, String sortOrder) {

			mReceiverType = ReceiverType.CURSOR;
			initMembers(providerUri, id, projection, selection, selectionArgs, sortOrder);
		}

		private void initMembers(Uri providerUri, int id, String[] projection,
		                         String selection, String[] selectionArgs, String sortOrder) {
			this.id = id;
			this.projection = projection;
			this.providerUri = providerUri;
			this.sortOrder = sortOrder;
			this.selection = selection;
			this.selectionArgs = selectionArgs;
		}
	}

	public interface OnLoadFinishedListener{
		public void onLoadFinished(int id);
	}

	public ContentLoader(Context context, Fragment fragment) {

		this.context = context;
		this.fragment = fragment;
	}

	public <U extends CursorAdapter> int loadContent(Uri providerUri, U cursorAdapter, String[] projection, String sortOrder) {
		return loadContent(providerUri, cursorAdapter, projection, null, null, sortOrder);
	}

	public <U extends CursorAdapter> int loadContent(Uri providerUri, U cursorAdapter, String[] projection, String selection,
	                                                 String[] selectionArgs, String sortOrder) {

		int id = getMyLoaderID();
		LoaderData loaderData;
		loaderData = new LoaderData(providerUri, cursorAdapter, id, projection, selection, selectionArgs, sortOrder);
		return loadContent(loaderData);
	}

	public int loadContent(Uri providerUri, String[] projection, String sortOrder) {
		return loadContent(providerUri, projection, null, null, sortOrder);
	}

	public int loadContent(Uri providerUri, String[] projection,
	                                                 String selection, String[] selectionArgs, String sortOrder) {

		int id = getMyLoaderID();
		LoaderData loaderData;
		loaderData = new LoaderData(providerUri, id, projection, selection, selectionArgs, sortOrder);
		return loadContent(loaderData);
	}

	public Cursor getLoadedCursor(int loaderID) {
		return sLoaderDataMap.get(loaderID).cursor;
	}

	public void setOnLoadFinishedListener(OnLoadFinishedListener listener) {
		mLoadFinishedCallBack = listener;
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

		switch (loaderData.mReceiverType) {
			case CURSOR_ADAPTER:
				loaderData.cursorAdapter.swapCursor(data);
			case CURSOR:
				loaderData.cursor = data;
				break;
		}

		if (mLoadFinishedCallBack != null) {
			mLoadFinishedCallBack.onLoadFinished(loaderID);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

		int loaderID = loader.getId();
		LoaderData loaderData = sLoaderDataMap.get(loaderID);
		if (loaderData == null) {
			return;
		}

		switch (loaderData.mReceiverType) {
			case CURSOR_ADAPTER:
				loaderData.cursorAdapter.swapCursor(null);
			case CURSOR:
				loaderData.cursor = null;
				break;
		}
	}

	private int loadContent(LoaderData loaderData) {

		sLoaderDataMap.put(loaderData.id, loaderData);
		fragment.getLoaderManager().initLoader(loaderData.id, null, this);
		return loaderData.id;
	}

	private synchronized int getMyLoaderID() {
		sLastLoaderID++;
		return sLastLoaderID;
	}
}
