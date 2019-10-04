package com.sombersoft.slacklog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import androidx.core.app.NavUtils;
import androidx.viewpager.widget.ViewPager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("deprecation")
public class Changelog extends AppCompatActivity {

    private static File rootDir;
    private static final String MIRROR_ARM = "https://www.slackware.org.uk/slackwarearm/slackwarearm-current/";
    private static final String MIRROR = "https://slackware.osuosl.org/";
    private final String PREF = "slackPref";
    private static final String VERSION_PREF64 = "versionPref64";
    private static final String VERSION_PREF32 = "versionPref32";
    private static final String UPDATE_COMMAND = "updateFlag_changelog";
    private final String BRANCH_32BIT = "branch32Bit";
    private final String BRANCH_64BIT = "branch64Bit";
    private final static String LAST_UPDATE_64BIT = "lastUpdate64bit";
    private final static String LAST_UPDATE_32BIT = "lastUpdate32bit";
    /**
     * Reference used for saving state of navigation bar and restore its view
     * tab
     */
    private final static String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    private String version;
    private static ViewPager viewPager;
    private static ChangelogFragmentAdapter adapter;
    private TextView textViewBranch;
    private List<String> listVersion;
    private AlertDialog.Builder alertMirror;
    private String branch;
    private static String copyLastUpdate64;
    private static String copyLastUpdate32;
    public static SharedPreferences slackPreferences;
    private static Editor editor;
    private static Context mContext;
    private static DownloadLog download;
    private static ProgressBar bar;
    private static TabLayout tabLayout;
    private static RelativeLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changelog);

        mContext = this;
        bar = findViewById(R.id.progressBar);
        mainLayout = findViewById(R.id.mainLayout);
        // version preference will be stored with SharedPreferences class
        slackPreferences = getSharedPreferences(PREF, MODE_PRIVATE);
        editor = slackPreferences.edit();
        rootDir = new File(getFilesDir(), "SlackLog");
        // Initialize preferences with default values contained in xml folder
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        viewPager = findViewById(R.id.pager);
        adapter = new ChangelogFragmentAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        textViewBranch = findViewById(R.id.tvMirror);
        textViewBranch.setShadowLayer(2, 0, 0, Color.BLACK);
        // create/restore version branch textview
        if (viewPager.getCurrentItem() == 0) {
            textViewBranch.setText(slackPreferences.getString(BRANCH_64BIT,
                    getString(R.string.branch) + " CURRENT"));
        } else if (viewPager.getCurrentItem() == 1) {
            textViewBranch.setText(slackPreferences.getString(BRANCH_32BIT,
                    getString(R.string.branch) + " CURRENT"));
        } else {
            textViewBranch.setText(MIRROR_ARM);
        }
        // Set up the action bar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.drawable.ic_launcher);
        setSupportActionBar(toolbar);
        // copy of the String LAST_UPDATE, in case trying to retrieve on line
        // date would fail
        // and LAST_UPDATE would be overwritten with bugged value
        copyLastUpdate64 = slackPreferences.getString(LAST_UPDATE_64BIT,
                "never");
        copyLastUpdate32 = slackPreferences.getString(LAST_UPDATE_32BIT,
                "never");
        Intent serviceIntent = new Intent(this,
                ChangelogBroadcastReceiver.class);
        startService(serviceIntent);
        // Available type of version
        listVersion = new ArrayList<>();
        listVersion = Arrays.asList("CURRENT", "STABLE");
        // AlertDialog needs of array CharSequence for elements list
        CharSequence[] versionList = (CharSequence[]) listVersion.toArray();
        // Version selection dialog window
        alertMirror = new AlertDialog.Builder(this);
        alertMirror.setTitle(getString(R.string.branch_version));
        alertMirror.setSingleChoiceItems(versionList, 0,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        version = listVersion.get(which);
                        if (tabLayout.getSelectedTabPosition() == 0) {
                            editor.putString(VERSION_PREF64, version);
                            editor.putString(BRANCH_64BIT, getString(R.string.branch)
                                    + version);
                            textViewBranch.setText(new StringBuilder().append(getString(R.string.branch))
                                    .append(version).toString());

                            // Do not set the branch 'till a connection is
                            // available to update
                            // the log, if not branch would be updated but not
                            // the last-update variable
                            if (ConnectionClass.isConnected(getBaseContext())) {
                                editor.apply();
                                update(true, null);
                            }
                        }

                        if (tabLayout.getSelectedTabPosition() == 1) {

                            editor.putString(VERSION_PREF32, version);
                            editor.putString(BRANCH_32BIT, getString(R.string.branch)
                                    + version);

                            textViewBranch.setText(new StringBuilder().append(getString(R.string.branch))
                                    .append(version).toString());

                            // Do not set the branch 'till a connection is
                            // available to update
                            // the log, if not branch would be updated but not
                            // the last-update variable
                            if (ConnectionClass.isConnected(getBaseContext())) {
                                editor.apply();
                                update(true, null);
                            }
                        }
                        dialog.dismiss();
                    }
                });

        tabLayout = findViewById(R.id.htab_tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                switch (tab.getPosition()) {                    case (0):
                        branch = slackPreferences.getString(BRANCH_64BIT,
                                getString(R.string.branch) + " CURRENT");
                        textViewBranch.setText(branch);
                        break;

                    case (1):
                        branch = slackPreferences.getString(BRANCH_32BIT,
                                getString(R.string.branch) + " CURRENT");
                        textViewBranch.setText(branch);
                        break;

                    case (2):
                        textViewBranch.setText("ARM only CURRENT");
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        // check if the class call comes from the background service, if true
        // run update function immediately
        if (slackPreferences.getBoolean(UPDATE_COMMAND, false)) {
            update(true, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        editor.putInt(STATE_SELECTED_NAVIGATION_ITEM, viewPager.getCurrentItem());
        editor.apply();
        if (bar.isShown())
            bar.setVisibility(View.GONE);
        if (download != null && download.getStatus() == AsyncTask.Status.RUNNING)
            download.cancel(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        editor.putInt(STATE_SELECTED_NAVIGATION_ITEM, viewPager.getCurrentItem());
        editor.apply();
        if (bar.isShown())
            bar.setVisibility(View.GONE);
        if (download != null && download.getStatus() == AsyncTask.Status.RUNNING)
            download.cancel(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewPager.setCurrentItem(slackPreferences.getInt(
                STATE_SELECTED_NAVIGATION_ITEM, 0));
    }

    /*
     * hardware menu button disabled
     */
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater mInfl = getMenuInflater();
        mInfl.inflate(R.menu.ab_mirror, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.abMirror):
                AlertDialog alert = alertMirror.create();
                alert.show();
                return true;

            case (android.R.id.home):
                NavUtils.navigateUpFromSameTask(this);

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
     * store the last date update
     */
    public static void setDateFile(String date, String bit) {
        if (bit.equals("64")) {
            editor.putString(LAST_UPDATE_64BIT, date);
            editor.apply();
        } else {
            editor.putString(LAST_UPDATE_32BIT, date);
            editor.apply();
        }
    }

    public static void update(boolean showBar, SwipeRefreshLayout swipeRefreshLayout) {
        if (ConnectionClass.isConnected(mContext)) {
            int tab;
            if (slackPreferences.getBoolean(UPDATE_COMMAND, false)) {
                editor.putBoolean(UPDATE_COMMAND, false);
                editor.apply();
                tab = slackPreferences.getInt(STATE_SELECTED_NAVIGATION_ITEM, 0);
            } else
                tab = viewPager.getCurrentItem();
            String suffix;
            download = new DownloadLog(swipeRefreshLayout);
            if (showBar)
                bar.setVisibility(View.VISIBLE);
            switch (tab) {
                case (0):
                    // determine which version of 32/64bit you want to download whether if CURRENT or STABLE
                    if (slackPreferences.getString(VERSION_PREF64, "CURRENT").equals("STABLE"))
                        suffix = "-14.2/";
                    else
                        suffix = "-current/";

                    download.execute((MIRROR + "slackware64" + suffix), "Changelog64Bit.txt");
                    break;
                case (1):
                    if (slackPreferences.getString(VERSION_PREF32, "CURRENT").equals("STABLE"))
                        suffix = "-14.2/";
                    else
                        suffix = "-current/";
                    download.execute((MIRROR + "slackware" + suffix), "Changelog32Bit.txt");
                    break;
                case (2):
                    download.execute(MIRROR_ARM, "ChangelogARM.txt");
                    break;
            }
        } else {
            Snackbar.make(mainLayout, mContext.getString(R.string.no_conn), Snackbar.LENGTH_SHORT)
                    .show();
            if (swipeRefreshLayout != null) /* if update is called from ChangelogService, but connection is no more available
                                                we'll get NullPointerException without this check
                                            */
                swipeRefreshLayout.setRefreshing(false);
        }
    }

    /*
     * download async class
     */
    private static class DownloadLog extends AsyncTask<String, Void, String> {
        private String vers = null;
        private String pathFileLog;
        private File file;
        private SwipeRefreshLayout swipeRefreshLayout;

        DownloadLog(SwipeRefreshLayout swipeRefreshLayout) {
            this.swipeRefreshLayout = swipeRefreshLayout;
        }

        @Override
        protected String doInBackground(String... params) {
            String result = null;
            boolean errorGettingChangelog = false;
            URL url;
            URL urlFile;
            String mainAddress = params[0];
            pathFileLog = params[1];
            String riga;
            // download the log file
            try {
                urlFile = new URL(mainAddress + "ChangeLog.txt");
                HttpURLConnection conn = (HttpURLConnection) urlFile
                        .openConnection();
                conn.setConnectTimeout(15 * 1000);
                conn.setReadTimeout(20 * 1000);
                // Set the string sent in the User-Agent request header in http
                // requests
                conn.setRequestProperty("User-Agent", MainActivity.USER_AGENT);
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream is = conn.getInputStream();
                    file = new File(rootDir, pathFileLog);
                    FileOutputStream fos = new FileOutputStream(file);
                    byte[] b = new byte[8 * 1024];
                    int i;
                    while ((i = is.read(b)) != -1 && !isCancelled()) {
                        fos.write(b, 0, i);
                    }
                    // flushes this stream. Implementations of this method
                    // should
                    // ensure that any buffered data is written out
                    fos.flush();
                    // Closes this stream. Concrete implementations of this
                    // class
                    // should free any resources during close
                    is.close();
                    fos.close();
                }
            } catch (IOException e) {
                errorGettingChangelog = true;
            }

            // if file changelog is downloaded CORRECTLY retrieve the date
            if (!errorGettingChangelog) {
                // If check doesn't involve ARM arch, which doesn't have
                // background check available,
                // take the on line date and store it on file
                if (!pathFileLog.equalsIgnoreCase(MIRROR_ARM)) {
                    BufferedReader br = null;
                    // get the date file from Internet
                    try {
                        url = new URL(mainAddress);
                        if (mainAddress.contains("64"))
                            vers = "64";
                        else
                            vers = "32";
                        HttpURLConnection conn = (HttpURLConnection) url
                                .openConnection();
                        conn.setConnectTimeout(15 * 1000);
                        conn.setReadTimeout(20 * 1000);
                        // Set the string sent in the User-Agent request header
                        // in http requests
                        conn.setRequestProperty("User-Agent", MainActivity.USER_AGENT);
                        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            while ((riga = br.readLine()) != null
                                    && !isCancelled()) {
                                if (riga.contains("<a href=\"ChangeLog.txt\">ChangeLog.txt</a>")) {
                                    result = riga;
                                    break;
                                }
                            }
                            br.close();
                        }
                    } catch (IOException e) {
                        if (vers.equals("64"))
                            result = copyLastUpdate64;
                        else
                            result = copyLastUpdate32;
                        /*
                         * manage StringIndexOutOfBoundsException error if it is
                         * impossible to cut the text properly
                         */
                    } catch (StringIndexOutOfBoundsException siobe) {
                        Toast.makeText(
                                mContext,
                                mContext.getString(R.string.error),
                                Toast.LENGTH_LONG).show();
                        siobe.printStackTrace();
                        if (vers.equals("64"))
                            result = copyLastUpdate64;
                        else
                            result = copyLastUpdate32;
                    } finally {
                        try {
                            if (br != null) {
                                br.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return result;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (swipeRefreshLayout != null)
                swipeRefreshLayout.setRefreshing(false);
            bar.setVisibility(View.GONE);
        }

        @Override
        protected void onPostExecute(String date) {
            if (swipeRefreshLayout != null)
                swipeRefreshLayout.setRefreshing(false);
            bar.setVisibility(View.GONE);
            if (date != null && vers != null) {
                setDateFile(date, vers);
                adapter.notifyDataSetChanged();
            }
        }
    }
}