package com.sombersoft.slacklog;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ChangelogService extends JobIntentService {
    private final int CONN_TIMEOUT = 15000;
    private final int READ_TIMEOUT = 15000;
    private String lastUpdate64 = null;
    private String lastUpdate32 = null;
    private final String URL_ADDRESS = ("https://slackware.osuosl.org/");
    private final String PREF = "slackPref";
    private final String UPDATE_COMMAND = "updateFlag_changelog";
    private final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    private final String LAST_UPDATE_64BIT = "lastUpdate64bit";
    private final String LAST_UPDATE_32BIT = "lastUpdate32bit";
    private final String VERSION_PREF64 = "versionPref64";
    private final String VERSION_PREF32 = "versionPref32";
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, Intent work) {
        int JOB_ID = 1000;
        enqueueWork(context, ChangelogService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        final int NOTIFICATION_ID = 1;
        boolean changed64 = false;
        boolean changed32 = false;
        boolean bothChanged = false;

        // sharedPreferences handle
        preferences = getSharedPreferences(PREF, MODE_PRIVATE);
        editor = preferences.edit();

        // get the type of the branches selected to check the changes, this
        // influences
        // the url address passed to check the last update date
        String getVersion64Branch = preferences.getString(VERSION_PREF64,
                "current");
        String getVersion32Branch = preferences.getString(VERSION_PREF32,
                "current");

        // get the type of cpu architecture, if selected, to check updated
        // changelog
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());

        String archType = settings.getString("ARCH_TYPE", "96"); // 96 is both
        // 64 & 32 bit

        // Builder class for NotificationCompat objects. Allows easier control
        // over all the flags,
        // as well as help constructing the typical notification layouts
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
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext(), id);
        builder.setSmallIcon(R.drawable.ic_changelog);
        builder.setContentTitle("Changelog:");
        builder.setAutoCancel(true);
        // creation of Intent for notify to be executed on touch
        Intent notifyIntent = new Intent(this, Changelog.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notifyIntent, 0);
        builder.setContentIntent(pendingIntent);

        String url64;

        if (getVersion64Branch.equalsIgnoreCase("current"))
            url64 = URL_ADDRESS + "slackware64-current/";
        else
            url64 = URL_ADDRESS + "slackware64-14.1/";

        String url32;

        if (getVersion32Branch.equalsIgnoreCase("current"))
            url32 = URL_ADDRESS + "slackware-current/";
        else
            url32 = URL_ADDRESS + "slackware-14.1/";

        String latestVersion64Bit = null;
        String latestVersion32Bit = null;

        if (archType.equals("64") || archType.equals("96")) {
            latestVersion64Bit = onLineVersion(url64);
            lastUpdate64 = preferences.getString(LAST_UPDATE_64BIT, "never");

            if (latestVersion64Bit != null
                    && !latestVersion64Bit.equals("null")) // latestVersion??Bit
                // assume "null"
                // value for example
                // when connected to
                // a captive portal
                // so it must be
                // checked to avoid
                // an empty
                // notification
                changed64 = !latestVersion64Bit.equals(lastUpdate64);
        }

        if (archType.equals("32") || archType.equals("96")) {
            latestVersion32Bit = onLineVersion(url32);
            lastUpdate32 = preferences.getString(LAST_UPDATE_32BIT, "never");

            if (latestVersion32Bit != null
                    && !latestVersion32Bit.equals("null"))
                changed32 = !latestVersion32Bit.equals(lastUpdate32);
        }

        if (archType.equals("96"))
            if (latestVersion32Bit != null && latestVersion64Bit != null
                    && !latestVersion32Bit.equals("null")
                    && !latestVersion64Bit.equals("null")) {

                bothChanged = changed64 && changed32;
            }

        if (bothChanged) {
            builder.setContentText(getString(R.string.available32_64));
            if (alarmNotificationManager != null) {
                alarmNotificationManager.notify(NOTIFICATION_ID, builder.build());
            }
            editor.putInt(STATE_SELECTED_NAVIGATION_ITEM, 0);
            editor.putBoolean(UPDATE_COMMAND, true);
            editor.apply();

            return;
        }

        if (changed64) {
            builder.setContentText(getString(R.string.available64));
            if (alarmNotificationManager != null) {
                alarmNotificationManager.notify(NOTIFICATION_ID, builder.build());
            }
            editor.putInt(STATE_SELECTED_NAVIGATION_ITEM, 0);
            editor.putBoolean(UPDATE_COMMAND, true);
            editor.apply();

            return;
        }

        if (changed32) {
            builder.setContentText(getString(R.string.available32));
            if (alarmNotificationManager != null) {
                alarmNotificationManager.notify(NOTIFICATION_ID, builder.build());
            }
            editor.putInt(STATE_SELECTED_NAVIGATION_ITEM, 1);
            editor.putBoolean(UPDATE_COMMAND, true);
            editor.apply();
        }
    }

    /*
     * *****METHODS & FUNCTIONS******
     */
    public String onLineVersion(String urlReceived) {
        String line;
        int responseCode;
        URL url;

        try {
            url = new URL(urlReceived);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // Sets the request command which will be sent to the remote HTTP
            // server
            conn.setConnectTimeout(CONN_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    if (line.contains("<a href=\"ChangeLog.txt\">ChangeLog.txt</a>")) {
                        break;
                    }
                }

                br.close();
            } else {
                if (urlReceived.contains("slackware64"))
                    return lastUpdate64;
                else
                    return lastUpdate32;
            }
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();

            if (urlReceived.contains("slackware64"))
                return lastUpdate64;
            else
                return lastUpdate32;
        }
        return line;
    }
}
