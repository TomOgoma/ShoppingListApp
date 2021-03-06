package com.tomogoma.shoppinglistapp.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Tom Ogoma on 20/03/15.
 */
public class SyncService extends Service {

	private static final Object sSyncAdapterLock = new Object();
	private static CurrencySyncAdapter sCurrencySyncAdapter = null;

	@Override
	public void onCreate() {
		synchronized (sSyncAdapterLock) {
			if (sCurrencySyncAdapter == null) {
				sCurrencySyncAdapter = new CurrencySyncAdapter(getApplicationContext(), true);
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return sCurrencySyncAdapter.getSyncAdapterBinder();
	}
}
