package com.codepath.apps.mysimpletweets;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.codepath.apps.mysimpletweets.models.User;
import com.facebook.stetho.Stetho;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;

/*
 * This is the Android application itself and is used to configure various settings
 * including the image cache in memory and on disk. This also adds a singleton
 * for accessing the relevant rest client.
 *
 *     RestClient client = RestApplication.getRestClient();
 *     // use client to send requests to API
 *
 */
public class TwitterApplication extends Application {
    private static Context context;
    private static final String SHARED_PREF_DEFAULT = "SharedPreferencesDefault";
    private static TwitterApplication _instance;

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    private User currentUser;

    @Override
    public void onCreate() {
        super.onCreate();
        _instance = this;
        // This instantiates DBFlow
        FlowManager.init(new FlowConfig.Builder(this).build());
        // add for verbose logging
        // FlowLog.setMinimumLoggingLevel(FlowLog.Level.V);

        FlowManager.init(new FlowConfig.Builder(this).build());
        FlowLog.setMinimumLoggingLevel(FlowLog.Level.V);

        TwitterApplication.context = this;
        Stetho.initializeWithDefaults(this);
    }

    public static TwitterClient getRestClient() {
        return (TwitterClient) TwitterClient.getInstance(TwitterClient.class, TwitterApplication.context);
    }

    public static TwitterApplication instance() {
        return _instance;
    }

    public SharedPreferences getDefaultSharedPreferences() {
        return this.getSharedPreferences(SHARED_PREF_DEFAULT, Context.MODE_PRIVATE);
    }
}