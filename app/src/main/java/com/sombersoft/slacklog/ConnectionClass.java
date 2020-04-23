package com.sombersoft.slacklog;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/*
 * check if connection is available
 */
public class ConnectionClass {
    public static boolean isConnected(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = null;
        if (cm != null) {
            netInfo = cm.getActiveNetworkInfo();
        }
        if (null != netInfo)
            return netInfo.isConnected();
        return false;
    }
}
