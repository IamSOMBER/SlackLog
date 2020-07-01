package com.sombersoft.slacklog;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ScheduledUpdate extends Worker {
    private Context context;

    public ScheduledUpdate(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        boolean checkMultilib = settings.getBoolean("AUTO_MULTILIB", true);
        if (checkMultilib && ConnectionClass.isConnected(context)) {
            Intent mIntent = new Intent(context, MultilibService.class);
            MultilibService.enqueueWork(context, mIntent);
        }
        return Result.retry();
    }
}
