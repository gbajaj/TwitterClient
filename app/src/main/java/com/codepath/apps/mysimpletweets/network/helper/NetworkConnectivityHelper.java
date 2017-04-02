package com.codepath.apps.mysimpletweets.network.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.widget.Toast;

import com.codepath.apps.mysimpletweets.TwitterApplication;

import java.io.IOException;


/**
 * Created by gauravb on 3/16/17.
 */

public class NetworkConnectivityHelper {
    public static Boolean isNetworkAvailable() {
        Context context = TwitterApplication.instance();
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void notifyNoNetwork(Context context) {
        showToast(context, "Network Not Connected");
    }

    public static void showToast(Context context, String text) {
        if (TextUtils.isEmpty(text) == false) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        }
    }
}
