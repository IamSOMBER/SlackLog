package com.sombersoft.slacklog;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

@SuppressWarnings("deprecation")
public class Multilib extends AppCompatActivity {

    private File rootDir;
    private File fileMultilib;
    private final String PREF = "slackPref";
    private static String copyLastUpdate;
    private SharedPreferences mPref;
    private static ProgressBar bar;
    private static Editor editor;
    private final String UPDATE_COMMAND = "updateFlag_multilib";
    private static final String LAST_UPDATE = "lastMultilibUpdate";
    private MultilibAdapter adapter;
    private static SwipeRefreshLayout swipeRefreshLayout;
    private static RelativeLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multilib);
        rootDir = new File(getFilesDir(), "SlackLog");
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.drawable.ic_launcher);
        setSupportActionBar(toolbar);
        mainLayout = findViewById(R.id.relative);
        swipeRefreshLayout = findViewById(R.id.refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (ConnectionClass.isConnected(getBaseContext()))
                    update(false);
                else {
                    swipeRefreshLayout.setRefreshing(false);
                    Snackbar.make(mainLayout, getString(R.string.no_conn), Snackbar.LENGTH_SHORT)
                            .show();
                }
            }
        });
        mPref = getSharedPreferences(PREF, MODE_PRIVATE);
        bar = findViewById(R.id.progressBar);
        editor = mPref.edit();
        // creation of object to avoid NullPointerException exception when back
        // button is pressed
        fileMultilib = new File(rootDir, "multilib.txt");
        // copy of the String LAST_UPDATE, in case trying to retrieve on line
        // date would fail
        // and LAST_UPDATE would be overwritten with bugged value
        copyLastUpdate = mPref.getString(LAST_UPDATE, "empty");
        if (mPref.getBoolean(UPDATE_COMMAND, false)) {
            editor.putBoolean(UPDATE_COMMAND, false).apply();
            update(true);
        } else {
            if (fileMultilib.exists()) {
                //Log.d("MULTILIBLOG", "fileMultilib exists");
                view();
            } else {
                //Log.d("MULTILIBLOG", "fileMultilib DON'T exists");
                update(true);
            }
        }
    }

    /*
     * hardware MENU BUTTON disabled
     */
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return false;
    }

    /*
     * VIEW function
     */
    private void view() {
        ArrayList<String> array = new ArrayList<>();
        RecyclerView listView = findViewById(R.id.listView);
        listView.setHasFixedSize(true);
        listView.setLayoutManager(new LinearLayoutManager(this));
        FileReader fr;
        int cont = 0;
        String checkLine;
        if (fileMultilib.exists())
            //Log.d("MULTILIBLOG", "VIEW:fileMultilib exists");
        try {
            fr = new FileReader(fileMultilib);
            BufferedReader br = new BufferedReader(fr);
            while (br.ready() && cont < 5) {
                checkLine = br.readLine();
                //Log.d("MULTILIBLOG", "file contains: " + checkLine);
                if (!checkLine.contains("+--------------------------+"))
                    array.add(checkLine);
                else
                    cont++;
            }
            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        if (array.size() == 0) {
            //Log.d("MULTILIBLOG", "array size 0");
            array.add(getString(R.string.downswipe));
        } else
            //Log.d("MULTILIBLOG", "array size NOT 0");
        adapter = new MultilibAdapter(array);
        listView.setAdapter(adapter);
    }

    /*
     * update function
     */

    public void update(boolean showBar) {
        DownloadMultilib download = new DownloadMultilib();
        if (ConnectionClass.isConnected(getBaseContext())) {
            //Log.d("MULTILIBLOG", "internet connection OK");
            if (showBar)
                bar.setVisibility(View.VISIBLE);
            download.execute();
        } else {
            Snackbar.make(mainLayout, getString(R.string.no_conn), Snackbar.LENGTH_SHORT)
                    .show();
            swipeRefreshLayout.setRefreshing(false);
            view();
        }
    }

    /*
     * Downloading class
     */
    public class DownloadMultilib extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            final String UTL_MULTILIB = "http://www.slackware.com/~alien/multilib/ChangeLog.txt";
            boolean errorGettingChangelog = false;
            int responseCode;

            try {
                URL url = new URL(UTL_MULTILIB);
                //Log.d("MULTILIBLOG", "url: " + url.toString());
                File rootDir = new File(getFilesDir(), "SlackLog");
                File multilibFile = new File(rootDir, "multilib.txt");
                //Log.d("MULTILIBLOG", "file Multilib: " + multilibFile.getAbsolutePath());
                FileOutputStream fos = new FileOutputStream(multilibFile);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10 * 1000);
                conn.setRequestProperty("User-Agent", MainActivity.USER_AGENT);
                responseCode = conn.getResponseCode();
                byte[] buffer = new byte[8 * 1024];
                int i;
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    //Log.d("MULTILIBLOG", "URLConnection connection OK");
                    InputStream is = new BufferedInputStream(conn.getInputStream());
                    while ((i = is.read(buffer)) != -1 && !isCancelled()) {
                        // if user breaks update process, download ends and
                        // calls onCancelled function
                        fos.write(buffer, 0, i);
                    }
                    // flushes this stream. Implementations of this method
                    // should ensure that any buffered data is written out
                    fos.flush();
                    // Closes this stream. Concrete implementations of this
                    // class should free any resources during close
                    is.close();
                    fos.close();
                }
            } catch (IOException e) {
                //Log.d("MULTILIBLOG", "error: " + e.toString());
                errorGettingChangelog = true;
            }

            if (!errorGettingChangelog) {
                // get the date of the downloaded multilib file
                try {
                    URL urlToGetDate = new URL(UTL_MULTILIB);
                    HttpURLConnection connDate = (HttpURLConnection) urlToGetDate.openConnection();
                    connDate.setRequestProperty("User-Agent", MainActivity.USER_AGENT);
                    connDate.setConnectTimeout(15 * 1000);
                    connDate.setReadTimeout(20 * 1000);
                    responseCode = connDate.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader br = new BufferedReader(
                                new InputStreamReader(connDate.getInputStream()));
                        String l;
                        while ((l = br.readLine()) != null) {
                            if ((l.contains("href=\"current/\""))) {
                                String cutFirst = l.substring(l.indexOf("current/</a>") + 35);
                                String cutLast = cutFirst.substring(0, 11);
                                editor.putString(LAST_UPDATE, cutLast);
                                editor.apply();
                                break;
                            }
                        }

                        br.close();
                    } else {
                        editor.putString(LAST_UPDATE, copyLastUpdate);
                        editor.apply();
                    }
                } catch (IOException e) {
                    editor.putString(LAST_UPDATE, copyLastUpdate);
                    editor.apply();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            swipeRefreshLayout.setRefreshing(false);
            bar.setVisibility(View.GONE);
            view();
        }

        @Override
        protected void onCancelled() {
            swipeRefreshLayout.setRefreshing(false);
            bar.setVisibility(View.GONE);
        }
    }
}
