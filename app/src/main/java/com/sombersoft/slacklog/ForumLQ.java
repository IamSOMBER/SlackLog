package com.sombersoft.slacklog;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Objects;

public class ForumLQ extends AppCompatActivity {
    private static Context mContext;
    private static String fileLQNewbie;
    private static String fileLQSoftware;
    private static String fileLQHardware;
    private static String fileLQLaptop;
    private static String fileLQMobile;
    private static String fileLQSec;
    private static String fileLQServer;
    private static String fileLQDesktop;
    private static String fileLQNet;
    private static String fileLQDistro;
    private static String fileLQCloud;
    private static String fileLQScreen;
    private static String fileLQMisc;
    private static String fileLQNews;
    private static SharedPreferences settings;
    private boolean autoUpdate;
    private final String PREF = "slackPref";
    private SharedPreferences.Editor editorDef;
    private static DownloadTopicsLQ download;
    private static LinkedHashMap<String, String> forumLQ;// like HashMap, except that
    // when you iterate over it, it presents the items in the insertion order
    private static ViewPager mViewPager;
    private static SectionsPagerAdapter mSectionsPagerAdapter;
    private static ProgressBar bar;
    private static RelativeLayout mainLayout;
    private static LinearLayout linearLayout;
    private static HorizontalScrollView horizontalScrollView;
    /**
     * used to check if update must be executed on opening forum
     */
    private final String AUTOUPDATE = "auto_update_flag";
    private final static String SOFTWARE_URL = "url_software_forum";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum_lq);
        // settings preferences to check if auto update is selected
        settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        editorDef = settings.edit();
        autoUpdate = settings.getBoolean("FORUM_AUTO_DOWNLOAD", true);
        // tab selected will be stored with SharedPreferences class
        SharedPreferences slackPreferences = getSharedPreferences(PREF, MODE_PRIVATE);
        SharedPreferences.Editor editorPref = slackPreferences.edit();
        boolean check = slackPreferences.getBoolean(AUTOUPDATE, false);
        mContext = this;
        mainLayout = findViewById(R.id.relative);
        linearLayout = findViewById(R.id.linearLayout);
        horizontalScrollView = findViewById(R.id.horScroll);
        bar = findViewById(R.id.progressBar);

        fileLQNewbie = "lq_newbie.txt";
        fileLQSoftware = "lq_soft.txt";
        fileLQHardware = "lq_hard.txt";
        fileLQLaptop = "lq_laptop.txt";
        fileLQMobile = "lq_mobile.txt";
        fileLQSec = "lq_sec.txt";
        fileLQServer = "lq_server.txt";
        fileLQDesktop = "lq_desktop.txt";
        fileLQNet = "lq_net.txt";
        fileLQDistro = "lq_distro.txt";
        fileLQCloud = "lq_cloud.txt";
        fileLQScreen = "lq_screen.txt";
        fileLQMisc = "lq_general.txt";
        fileLQNews = "lq_news.txt";

        forumLQ = new LinkedHashMap<>();
        createMap();
        final TabLayout tabLayout = findViewById(R.id.tabLayout);

        // Set up the action bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.drawable.lqforum);
        setSupportActionBar(toolbar);

        // Specify a SpinnerAdapter to populate the dropdown list.
        // use .getThemedContext() to ensure that the text color is always appropriate for the action bar
        // background rather than the activity background.
        final SpinnerAdapter adapter = ArrayAdapter.createFromResource(getApplicationContext(),
                R.array.category, android.R.layout.simple_spinner_dropdown_item);
        final Spinner spinner = new Spinner(Objects.requireNonNull(getSupportActionBar()).getThemedContext());
        spinner.setAdapter(adapter);
        toolbar.addView(spinner, 0);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (tabLayout.getTabAt(position) != null) {
                    Objects.requireNonNull(tabLayout.getTabAt(position)).select(); // it may produce null pointer exception
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Create the adapter that will return a fragment for each of the 14
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        // when VIEW on PAGER change must be updated navigation tab item
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout) {
            @Override
            public void onPageSelected(int arg0) {
                showHorScrollBar(arg0);
                switch (arg0) {
                    case 1: // means software tab selected
                        editorDef.putString(SOFTWARE_URL, "https://www.linuxquestions.org/questions/linux-software-2/");
                        editorDef.apply();
                        Log.d("PAGE_SELECTED",
                                settings.getString(SOFTWARE_URL, "https://www.linuxquestions.org/questions/linux-software-2/"));
                }

                if (autoUpdate) {
                    update(true, null);
                }
            }

            @Override
            public void onPageScrolled(int arg0, float positionOffset, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
        // The one-stop shop for setting up this TabLayout with a ViewPager
        tabLayout.setupWithViewPager(mViewPager, false);

        if (autoUpdate && check) {
            editorPref.putBoolean(AUTOUPDATE, false);
            editorPref.apply();
            update(true, null);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (download != null && download.getStatus() == AsyncTask.Status.RUNNING)
            download.cancel(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (download != null && download.getStatus() == AsyncTask.Status.RUNNING)
            download.cancel(true);
    }

    private void showHorScrollBar(int tabSelected) {
        switch (tabSelected) {
            case (1):
                final HashMap<String, String> softwareMaps = new HashMap<>(2, 2);
                softwareMaps.put("Kernel", "https://www.linuxquestions.org/questions/linux-kernel-70/");
                softwareMaps.put("Games", "https://www.linuxquestions.org/questions/linux-games-33/");
                horizontalScrollView.setVisibility(View.VISIBLE);
                final Iterator<String> iterator = softwareMaps.keySet().iterator();
                while (linearLayout.getChildCount() < 2 && iterator.hasNext()) {
                    final String key = iterator.next();
                    final TextView textView = new TextView(this);
                    textView.setText(key);
                    textView.setClickable(true);
                    textView.setPadding(40, 10, 40, 10);
                    textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d("ONCLICK", softwareMaps.get(key));
                            editorDef.putString(SOFTWARE_URL, softwareMaps.get(key));
                            editorDef.apply();
                            update(true, null);
                        }
                    });
                    linearLayout.addView(textView);
                }
                return;

            default:
                horizontalScrollView.setVisibility(View.GONE);
        }
    }

    public static void update(boolean showBar, SwipeRefreshLayout swipe) {
        if (showBar)
            bar.setVisibility(View.VISIBLE);
        // determining the tab opened at the moment
        if (ConnectionClass.isConnected(mContext)) {
            switch (mViewPager.getCurrentItem()) {
                case 0:
                    download = new DownloadTopicsLQ(swipe);
                    download.execute(forumLQ.get("Newbie"), fileLQNewbie);
                    break;

                case 1:
                    download = new DownloadTopicsLQ(swipe);
                    download.execute(settings.getString(SOFTWARE_URL, "https://www.linuxquestions.org/questions/linux-software-2/"),
                            fileLQSoftware);
                    break;

                case 2:
                    download = new DownloadTopicsLQ(swipe);
                    download.execute(forumLQ.get("Hardware"), fileLQHardware);
                    break;

                case 3:
                    download = new DownloadTopicsLQ(swipe);
                    download.execute(forumLQ.get("Laptop"), fileLQLaptop);
                    break;

                case 4:
                    download = new DownloadTopicsLQ(swipe);
                    download.execute(forumLQ.get("Mobile"), fileLQMobile);
                    break;

                case 5:
                    download = new DownloadTopicsLQ(swipe);
                    download.execute(forumLQ.get("Sec"), fileLQSec);
                    break;

                case 6:
                    download = new DownloadTopicsLQ(swipe);
                    download.execute(forumLQ.get("Server"), fileLQServer);
                    break;

                case 7:
                    download = new DownloadTopicsLQ(swipe);
                    download.execute(forumLQ.get("Desktop"), fileLQDesktop);
                    break;

                case 8:
                    download = new DownloadTopicsLQ(swipe);
                    download.execute(forumLQ.get("Net"), fileLQNet);
                    break;

                case 9:
                    download = new DownloadTopicsLQ(swipe);
                    download.execute(forumLQ.get("Distro"), fileLQDistro);
                    break;

                case 10:
                    download = new DownloadTopicsLQ(swipe);
                    download.execute(forumLQ.get("Cloud"), fileLQCloud);
                    break;

                case 11:
                    download = new DownloadTopicsLQ(swipe);
                    download.execute(forumLQ.get("Screen"), fileLQScreen);
                    break;

                case 12:
                    download = new DownloadTopicsLQ(swipe);
                    download.execute(forumLQ.get("Misc"), fileLQMisc);
                    break;

                case 13:
                    download = new DownloadTopicsLQ(swipe);
                    download.execute(forumLQ.get("News"), fileLQNews);
                    break;

                default:
                    break;
            }
        } else {
            Snackbar.make(mainLayout, mContext.getString(R.string.no_conn), Snackbar.LENGTH_SHORT)
                    .show();
            if (swipe != null)
                swipe.setRefreshing(false);
            bar.setVisibility(View.GONE);
        }
    }

    private void createMap() {
        forumLQ.put("Newbie", "https://www.linuxquestions.org/questions/linux-newbie-8/");
        forumLQ.put("Software", settings.getString(SOFTWARE_URL, "https://www.linuxquestions.org/questions/linux-software-2/"));
        forumLQ.put("Hardware", "https://www.linuxquestions.org/questions/linux-hardware-18/");
        forumLQ.put("Laptop", "https://www.linuxquestions.org/questions/linux-laptop-and-netbook-25/");
        forumLQ.put("Mobile", "https://www.linuxquestions.org/questions/linux-mobile-81/");
        forumLQ.put("Sec", "https://www.linuxquestions.org/questions/linux-security-4/");
        forumLQ.put("Server", "https://www.linuxquestions.org/questions/linux-server-73/");
        forumLQ.put("Desktop", "https://www.linuxquestions.org/questions/linux-desktop-74/");
        forumLQ.put("Net", "https://www.linuxquestions.org/questions/linux-networking-3/");
        forumLQ.put("Distro", "https://www.linuxquestions.org/questions/linux-distributions-5/");
        forumLQ.put("Cloud", "https://www.linuxquestions.org/questions/linux-virtualization-and-cloud-90/");
        forumLQ.put("Screen", "https://www.linuxquestions.org/questions/linux-screencasts-and-screenshots-114/");
        forumLQ.put("Misc", "https://www.linuxquestions.org/questions/linux-general-1/");
        forumLQ.put("News", "https://www.linuxquestions.org/questions/linux-news-59/");
    }

    /**
     * A {@link FragmentStatePagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Bundle addBundle = new Bundle();
            switch (position) {
                case 0:
                    addBundle.putString("fileName", "lq_newbie.txt");
                    FragmentForumLQ newbie = new FragmentForumLQ();
                    newbie.setArguments(addBundle);
                    return newbie;

                case 1:
                    addBundle.putString("fileName", "lq_soft.txt");
                    FragmentForumLQ soft = new FragmentForumLQ();
                    soft.setArguments(addBundle);
                    return soft;

                case 2:
                    addBundle.putString("fileName", "lq_hard.txt");
                    FragmentForumLQ hard = new FragmentForumLQ();
                    hard.setArguments(addBundle);
                    return hard;

                case 3:
                    addBundle.putString("fileName", "lq_laptop.txt");
                    FragmentForumLQ laptop = new FragmentForumLQ();
                    laptop.setArguments(addBundle);
                    return laptop;

                case 4:
                    addBundle.putString("fileName", "lq_mobile.txt");
                    FragmentForumLQ mobile = new FragmentForumLQ();
                    mobile.setArguments(addBundle);
                    return mobile;

                case 5:
                    addBundle.putString("fileName", "lq_sec.txt");
                    FragmentForumLQ sec = new FragmentForumLQ();
                    sec.setArguments(addBundle);
                    return sec;

                case 6:
                    addBundle.putString("fileName", "lq_server.txt");
                    FragmentForumLQ server = new FragmentForumLQ();
                    server.setArguments(addBundle);
                    return server;

                case 7:
                    addBundle.putString("fileName", "lq_desktop.txt");
                    FragmentForumLQ desktop = new FragmentForumLQ();
                    desktop.setArguments(addBundle);
                    return desktop;

                case 8:
                    addBundle.putString("fileName", "lq_net.txt");
                    FragmentForumLQ net = new FragmentForumLQ();
                    net.setArguments(addBundle);
                    return net;

                case 9:
                    addBundle.putString("fileName", "lq_distro.txt");
                    FragmentForumLQ distro = new FragmentForumLQ();
                    distro.setArguments(addBundle);
                    return distro;

                case 10:
                    addBundle.putString("fileName", "lq_cloud.txt");
                    FragmentForumLQ cloud = new FragmentForumLQ();
                    cloud.setArguments(addBundle);
                    return cloud;

                case 11:
                    addBundle.putString("fileName", "lq_screen.txt");
                    FragmentForumLQ screen = new FragmentForumLQ();
                    screen.setArguments(addBundle);
                    return screen;

                case 12:
                    addBundle.putString("fileName", "lq_general.txt");
                    FragmentForumLQ general = new FragmentForumLQ();
                    general.setArguments(addBundle);
                    return general;

                case 13:
                    addBundle.putString("fileName", "lq_news.txt");
                    FragmentForumLQ news = new FragmentForumLQ();
                    news.setArguments(addBundle);
                    return news;
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 14 total pages.
            return 14;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            // Causes adapter to reload all Fragments when
            // notifyDataSetChanged is called
            return PagerAdapter.POSITION_NONE;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String[] lqForumTopics = getResources().getStringArray(R.array.category);
            return lqForumTopics[position];
        }
    }

    public static class DownloadTopicsLQ extends AsyncTask<String, Void, ArrayList<String>> {
        private SwipeRefreshLayout swipeRefreshLayout;
        private String fileName;

        DownloadTopicsLQ(SwipeRefreshLayout srl) {
            swipeRefreshLayout = srl;
        }

        @Override
        protected ArrayList<String> doInBackground(String... params) {
            fileName = params[1];
            ArrayList<String> mArray = new ArrayList<>();

            try {
                URL url = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(25000);
                conn.setReadTimeout(30000);
                conn.setRequestMethod("GET");
                conn.connect();
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            conn.getInputStream()));
                    String line;
                    StringBuilder builder = new StringBuilder();

                    while ((line = br.readLine()) != null && !isCancelled()) {
                        if (line.contains("<tbody id=\"threadbits_forum")) {
                            while (!(line = br.readLine()).contains("</tbody>")) {
                                if (!line.contains("</tr>"))
                                    builder.append(line);
                                else {
                                    if (builder.toString().contains("id=\"td_threadstatusicon_")) {
                                        builder.append("\n");
                                        // replace '&gt' with > and '&quot' with "
                                        mArray.add(builder.toString()
                                                .replace("&gt;", ">")
                                                .replace("&quot;", "\"")
                                                .replace("&amp;", "&")
                                                .replace("%96", "-"));
                                    }
                                    builder.setLength(0);
                                }
                            }
                            br.close();
                            break;
                        }
                    }
                } else {
                    Snackbar.make(mainLayout, mContext.getString(R.string.error), Snackbar.LENGTH_SHORT)
                            .show();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return mArray;
        }

        @Override
        protected void onPostExecute(ArrayList<String> a) {
            if (swipeRefreshLayout != null)
                swipeRefreshLayout.setRefreshing(false);
            bar.setVisibility(View.GONE);
            if (!isCancelled() && !a.isEmpty()) {
                createCacheFile(a);
            }
        }

        @Override
        protected void onCancelled(ArrayList<String> strings) {
            super.onCancelled(strings);
            if (swipeRefreshLayout != null)
                swipeRefreshLayout.setRefreshing(false);
            bar.setVisibility(View.GONE);
        }

        private void createCacheFile(ArrayList<String> a) {
            Iterator<String> i = a.iterator();
            String readLine;
            BufferedWriter bw;
            File fileToWrite = new File(mContext.getCacheDir(), fileName);

            try {
                bw = new BufferedWriter(new FileWriter(fileToWrite));
                while (i.hasNext()) {
                    readLine = i.next();
                    bw.write(readLine);
                }
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSectionsPagerAdapter.notifyDataSetChanged();
        }
    }
}
