package com.aidlinterfaceissue;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import java.util.Map;
import java.util.HashMap;
import android.util.Log;
import android.provider.Settings.Secure;

import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.Policy;
import com.google.android.vending.licensing.ServerManagedPolicy;

public class LicensingModule extends ReactContextBaseJavaModule {
    private ReactContext reactContext;
    LicensingModule(ReactApplicationContext context) {
        super(context);
        reactContext = context;
    }

    @Override
    public String getName() {
        return "LicensingModule";
    }

    @ReactMethod
    public void getId(String base64PublicKey, String salt, final Promise promise) {
        String deviceId = Secure.getString(reactContext.getContentResolver(), Secure.ANDROID_ID);

        final LicenseChecker checker = new LicenseChecker(
                reactContext, new ServerManagedPolicy(reactContext,
                new AESObfuscator(salt.getBytes(), reactContext.getPackageName(), deviceId)),
                base64PublicKey);

        checker.checkAccess(new LicenseCheckerCallback() {
            @Override
            public void allow(int reason) {
                promise.resolve(true);
                checker.onDestroy();
            }

            @Override
            public void allowWithUserId(int reason, String userId) {
                promise.resolve(userId);
                checker.onDestroy();
            }

            @Override
            public void dontAllow(int reason) {
                if(reason == Policy.RETRY) {
                    promise.resolve(null);
                }
                else {
                    promise.resolve(false);
                }
                checker.onDestroy();
            }

            @Override
            public void applicationError(int errorCode) {
                promise.reject("code: " + errorCode, "license check failed");
                checker.onDestroy();
            }
        });
    }
}
