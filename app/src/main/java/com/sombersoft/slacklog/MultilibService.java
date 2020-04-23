package com.sombersoft.slacklog;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MultilibService extends JobIntentService {
    private final int CONN_TIMEOUT = 15000;
    private final int READ_TIMEOUT = 15000;
    private final String URL_ADDRESS = ("https://www.slackware.com/~alien/multilib/");
    private final String PREF = "slackPref";
    private final String UPDATE_COMMAND = "updateFlag_multilib";
    private final String LAST_UPDATE = "lastMultilibUpdate";
    private String lastMultilibUpdate;
    private String lastOnLineUpdate;

    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, Intent work) {
        int JOB_ID = 1111;
        enqueueWork(context, MultilibService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        final int NOTIFICATION_ID = 2;
        //sharedPreferences creation
        SharedPreferences mPref = getSharedPreferences(PREF, MODE_PRIVATE);
        Editor editor = mPref.edit();
        String name = "alarm_channel_notification";
        String id = "alarm_channel_id"; // The user-visible name of the channel
        String description = "alarm_channel_notification_icon"; // The user-visible description of the channel
        NotificationManager alarmNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(id, name, importance);
            mChannel.setDescription(description);
            if (alarmNotificationManager != null) {
                alarmNotificationManager.createNotificationChannel(mChannel);
            }
        }
        //Builder class for NotificationCompat objects. Allows easier control over all the flags,
        //as well as help constructing the typical notification layouts
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, id);
        builder.setSmallIcon(R.drawable.ic_multilib);
        builder.setContentTitle("Multilib:");
        builder.setAutoCancel(true);
        //creation of Intent for notify to be open by touch
        Intent notifyIntent = new Intent(this, Multilib.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, 0);
        builder.setContentIntent(pendingIntent);

        lastMultilibUpdate = mPref.getString(LAST_UPDATE, "empty");
        lastOnLineUpdate = getOnLineDate();

        if (!lastMultilibUpdate.equals(lastOnLineUpdate)) {
            editor.putBoolean(UPDATE_COMMAND, true);
            editor.apply();
            builder.setContentText(getString(R.string.latest) + lastOnLineUpdate);
            if (alarmNotificationManager != null) {
                alarmNotificationManager.notify(NOTIFICATION_ID, builder.build());
            }
        }
    }

    private String getOnLineDate() {
        String result = null;
        String l;
        try {
            URL urlToGetDate = new URL(URL_ADDRESS);
            HttpURLConnection connDate = (HttpURLConnection) urlToGetDate.openConnection();
            connDate.setConnectTimeout(CONN_TIMEOUT);
            connDate.setReadTimeout(READ_TIMEOUT);
            connDate.setRequestMethod("GET");
            connDate.setRequestProperty("User-Agent", MainActivity.USER_AGENT);
            int responseCode = connDate.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(connDate.getInputStream()));
                while ((l = br.readLine()) != null) {
                    if ((l.contains("href=\"current/\""))) {
                        String cutFirst = l.substring(l.indexOf("current/</a>") + 35);
                        result = cutFirst.substring(0, 11);
                        break;
                    }
                }
                br.close();
            } else
                return lastMultilibUpdate;
        } catch (IOException e) {
            e.printStackTrace();
            return lastMultilibUpdate;
        }
        return result;
    }
}
