package com.androidapp.test.facebooktask.util;

import android.util.Log;
import android.widget.Toast;

import com.androidapp.test.facebooktask.R;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;

public class Application extends android.app.Application {
    private static final String TAG = "Application";

    @Override
    public void onCreate() {
        super.onCreate();

        FacebookSdk.sdkInitialize(getApplicationContext());
        AccessTokenTracker mAccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    LoginManager.getInstance().logOut();
                    Log.e(TAG, "FacebookAccessToken is invalid");
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalid_token), Toast.LENGTH_LONG).show();
                }
            }
        };
        mAccessTokenTracker.startTracking();
    }
}
