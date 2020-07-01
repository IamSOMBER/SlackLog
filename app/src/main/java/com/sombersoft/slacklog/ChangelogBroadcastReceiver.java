package com.sombersoft.slacklog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkRequest;

import java.util.concurrent.TimeUnit;

public class ChangelogBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        boolean checkChangelog = settings.getBoolean("AUTO_CHANGELOG", true);
        if (checkChangelog && ConnectionClass.isConnected(context)) {
            Intent mIntent = new Intent(context, ChangelogService.class);
            ChangelogService.enqueueWork(context, mIntent);
            //schedule the task to be executed
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.UNMETERED)
                    .build();
            WorkRequest checkUpdates = new PeriodicWorkRequest.Builder(ScheduledUpdate.class,
                    4, TimeUnit.HOURS,
                    15, TimeUnit.MINUTES)
                    .setConstraints(constraints)
                    .build();
        }
    }
}
