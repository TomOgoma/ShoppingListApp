package com.tomogoma.shoppinglistapp.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Tom Ogoma on 20/03/15.
 */
public class AuthenticatorService extends Service {

	private Authenticator mAuthenticator;

	@Override
	public void onCreate() {
		mAuthenticator = new Authenticator(this);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mAuthenticator.getIBinder();
	}
}
