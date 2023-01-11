package com.app.signage91.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.multidex.MultiDexApplication;

import com.androidnetworking.AndroidNetworking;
import com.app.signage91.models.UserInfo;
import com.app.signage91.receivers.ConnectivityReceiver;
import com.app.signage91.utils.TopExceptionHandler;
import com.app.signage91.utils.retrofit.ApiService;
import com.app.signage91.utils.retrofit.ApiServiceProvider;
import com.app.signage91.utils.retrofit.RssFeedApiService;
import com.app.signage91.utils.retrofit.RssFeedApiServiceProvider;
import com.app.signage91.utils.room.MyDatabase;
import com.chibatching.kotpref.Kotpref;


public class MyApplication extends MultiDexApplication {

    RssFeedApiService rssFeedApiService;
    ApiService mApiService;
    private static MyApplication mInstance;
    MyDatabase mDatabase;
    //public static String dirPathNameImage = "Environment.getExternalStorageDirectory().toString()" + "\"" + "Signage91/Images/\"";
    //public static String dirPathNameVideo = "Environment.getExternalStorageDirectory().toString()" + "\"" + "Signage91/Videos/\"";
    public static String dirPathNameImage = "/Signage91/Images/";
    public static String dirPathNameVideo = "/Signage91/Videos/";

    public Boolean isApplicationActive = false;

    @SuppressLint("NewApi")
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));
        } catch (SecurityException e) {
            Log.e("EXCEPTION", "Could not set the Default Uncaught Exception Handler:" + e.getStackTrace());
        }

        mInstance = this;

        rssFeedApiService = RssFeedApiServiceProvider.INSTANCE.provideApiService(this);
        mApiService = ApiServiceProvider.INSTANCE.provideApiService(this);
        mDatabase = MyDatabase.Companion.getInstance(getApplicationContext());

        myUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        // setup handler for uncaught exception
        Thread.setDefaultUncaughtExceptionHandler(myUncaughtExceptionHandler);

        // Initialize Fast Android Networking Library
        AndroidNetworking.initialize(getApplicationContext());

        @SuppressLint({"NewApi", "LocalSuppress"}) StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

        setUpKotPref();

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
                //isApplicationActive = true;
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                //isApplicationActive = true;
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                //isApplicationActive = true;
                activity.setRequestedOrientation(UserInfo.INSTANCE.getRequestedOrientation());
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
                //isApplicationActive = true;
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
                //isApplicationActive = false;
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                //isApplicationActive = false;
            }

        });

        ProcessLifecycleOwner.get().getLifecycle().addObserver(new AppLifecycleListener());
    }

    class AppLifecycleListener implements DefaultLifecycleObserver {

        @Override
        public void onStart(@NonNull LifecycleOwner owner) {
            DefaultLifecycleObserver.super.onStart(owner);
            isApplicationActive = true;
        }

        @Override
        public void onStop(@NonNull LifecycleOwner owner) {
            DefaultLifecycleObserver.super.onStop(owner);
            isApplicationActive = false;
        }
    }

    private void setUpKotPref() {
        Kotpref.INSTANCE.init(getApplicationContext());
        // add Encrypt Support
        //Kotpref.gson = Gson()
        //Kotpref.cipherAdapter = SharedPrefCipherAdapter(applicationContext)
    }

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

    public static Thread.UncaughtExceptionHandler myUncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            for (StackTraceElement element : ex.getStackTrace()) {
                Log.e("RRR", element.toString());
            }
        }
    };

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }

    public RssFeedApiService getRssFeedApiService() {
        return rssFeedApiService;
    }

    public ApiService getApiService() {
        return mApiService;
    }


    public MyDatabase getDatabase() {
        return mDatabase;
    }

    public Boolean getApplicationActive() {
        return isApplicationActive;
    }


}
