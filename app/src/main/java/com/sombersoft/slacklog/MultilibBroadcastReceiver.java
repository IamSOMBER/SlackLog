package com.sombersoft.slacklog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MultilibBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null && action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            boolean checkMultilib = settings.getBoolean("AUTO_MULTILIB", true);
            if (checkMultilib && ConnectionClass.isConnected(context)) {
                Intent mIntent = new Intent(context, MultilibService.class);
                MultilibService.enqueueWork(context, mIntent);
            }
        }
    }
}
