package com.sombersoft.slacklog;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Objects;

@SuppressWarnings("deprecation")
public class Forum extends AppCompatActivity {

    private static Context mContext;
    private static ViewPager viewPager;
    private static ForumFragmentAdapter viewPagerAdapter;
    private static String fileTopics32;
    private static String fileTopics64;
    private static String fileTopicsGenDisc;
    private static String fileTopicsPack;
    private static String fileTopicsFree;
    private static String fileGnome;
    private static String fileWiki;
    private static String fileSec;
    private static String filePort;
    private static String fileProgram;
    private static String fileHardware;
    private static String fileLaptop;
    private static String fileMerchand;
    private static SharedPreferences settings;
    private boolean autoUpdate;
    private final String PREF = "slackPref";
    private Editor editor;
    private boolean check;
    private static DownloadTopics download;
    private FragmentManager fm;
    private static ProgressBar bar;
    private static RelativeLayout mainLayout;
    private static LinkedHashMap<String, String> forum;// like HashMap, except that
    // when you iterate over it, it presents the items in the insertion order
    /**
     * used to check if update must be executed on opening forum
     */
    private final String AUTOUPDATE = "auto_update_flag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_forum);

        mainLayout = findViewById(R.id.relative);
        forum = new LinkedHashMap<>();
        createMap();
        mContext = this;
        fileTopics32 = "topics32.txt";
        fileTopics64 = "topics64.txt";
        fileTopicsGenDisc = "topicsGenDisc.txt";
        fileTopicsPack = "topicsPack.txt";
        fileTopicsFree = "freeTopics.txt";
        fileGnome = "topicsGnome.txt";
        fileWiki = "topicsWiki.txt";
        fileSec = "topicsSecurity.txt";
        filePort = "topicsPorting.txt";
        fileProgram = "topicsProgram.txt";
        fileHardware = "topicsHardware.txt";
        fileLaptop = "topicsLaptop.txt";
        fileMerchand = "topicsMerchand.txt";

        // settings preferences to check if auto update is selected
        settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        autoUpdate = settings.getBoolean("FORUM_AUTO_DOWNLOAD", true);
        // tab selected will be stored with SharedPreferences class
        SharedPreferences slackPreferences = getSharedPreferences(PREF, MODE_PRIVATE);
        editor = slackPreferences.edit();
        check = slackPreferences.getBoolean(AUTOUPDATE, false);
        bar = findViewById(R.id.progressBar);
        final TabLayout tabLayout = findViewById(R.id.tabLayout);

        // Set up the action bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.drawable.ic_launcher);
        setSupportActionBar(toolbar);

        // Specify a SpinnerAdapter to populate the dropdown list.
        // use .getThemedContext() to ensure that the text color is always appropriate for the action bar
        // background rather than the activity background.
        final SpinnerAdapter adapter = ArrayAdapter.createFromResource(getApplicationContext(),
                R.array.category_slacky, android.R.layout.simple_spinner_dropdown_item);
        final Spinner spinner = new Spinner(Objects.requireNonNull(getSupportActionBar()).getThemedContext());
        spinner.setAdapter(adapter);
        toolbar.addView(spinner, 0);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TabLayout.Tab tab = tabLayout.getTabAt(position);
                if (tab != null) {
                    tab.select(); // it may produce null pointer exception
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // before define the ViewPager and set the adapter
        viewPager = findViewById(R.id.forumPager);
        fm = getSupportFragmentManager();
        viewPagerAdapter = new ForumFragmentAdapter(fm);
        viewPager.setAdapter(viewPagerAdapter);
        // The one-stop shop for setting up this TabLayout with a ViewPager
        tabLayout.setupWithViewPager(viewPager, false);

        // when VIEW on PAGER change must be updated navigation tab item
        viewPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                Log.d("FORUM", "viewpager onPageScrolled");
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

        if (autoUpdate && check) {
            editor.putBoolean(AUTOUPDATE, false);
            editor.apply();

            update(true, null);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (download != null && download.getStatus() == Status.RUNNING)
            download.cancel(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (download != null && download.getStatus() == Status.RUNNING)
            download.cancel(true);
    }

    /*
     * hardware menu button disabled
     */
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return false;
    }

    private void createMap() {
        forum.put("Slackware-64", "https://www.slacky.eu/forum/viewforum.php?f=51");
        forum.put("Slackware", "https://www.slacky.eu/forum/viewforum.php?f=1");
        forum.put("Gnu/Linux in genere", "https://www.slacky.eu/forum/viewforum.php?f=2");
        forum.put("Packages", "https://www.slacky.eu/forum/viewforum.php?f=4");
        forum.put("Libera", "https://www.slacky.eu/forum/viewforum.php?f=3");
        forum.put("Gnome Slacky", "https://www.slacky.eu/forum/viewforum.php?f=13");
        forum.put("Wiki Slacky", "https://www.slacky.eu/forum/viewforum.php?f=12");
        forum.put("Sicurezza", "https://www.slacky.eu/forum/viewforum.php?f=5");
        forum.put("Porting Slackware", "https://www.slacky.eu/forum/viewforum.php?f=9");
        forum.put("Programmazione", "https://www.slacky.eu/forum/viewforum.php?f=20");
        forum.put("Hardware", "https://www.slacky.eu/forum/viewforum.php?f=6");
        forum.put("Laptop", "https://www.slacky.eu/forum/viewforum.php?f=7");
        forum.put("Mercatino", "https://www.slacky.eu/forum/viewforum.php?f=10");
    }

    public static void update(boolean showBar, SwipeRefreshLayout swipe) {
        if (showBar)
            bar.setVisibility(View.VISIBLE);
        // determining the tab opened at the moment
        if (ConnectionClass.isConnected(mContext)) {
            int currentTab = viewPager.getCurrentItem();
            switch (currentTab) {
                case 0:
                    download = new DownloadTopics(swipe);
                    download.execute(forum.get("Slackware-64"), fileTopics64);
                    break;

                case 1:
                    download = new DownloadTopics(swipe);
                    download.execute(forum.get("Slackware"), fileTopics32);
                    break;

                case 2:
                    download = new DownloadTopics(swipe);
                    download.execute(forum.get("Gnu/Linux in genere"),
                            fileTopicsGenDisc);
                    break;

                case 3:
                    download = new DownloadTopics(swipe);
                    download.execute(forum.get("Packages"), fileTopicsPack);
                    break;

                case 4:
                    download = new DownloadTopics(swipe);
                    download.execute(forum.get("Libera"), fileTopicsFree);
                    break;

                case 5:
                    download = new DownloadTopics(swipe);
                    download.execute(forum.get("Gnome Slacky"), fileGnome);
                    break;

                case 6:
                    download = new DownloadTopics(swipe);
                    download.execute(forum.get("Wiki Slacky"), fileWiki);
                    break;

                case 7:
                    download = new DownloadTopics(swipe);
                    download.execute(forum.get("Sicurezza"), fileSec);
                    break;

                case 8:
                    download = new DownloadTopics(swipe);
                    download.execute(forum.get("Porting Slackware"), filePort);
                    break;

                case 9:
                    download = new DownloadTopics(swipe);
                    download.execute(forum.get("Programmazione"), fileProgram);
                    break;

                case 10:
                    download = new DownloadTopics(swipe);
                    download.execute(forum.get("Hardware"), fileHardware);
                    break;

                case 11:
                    download = new DownloadTopics(swipe);
                    download.execute(forum.get("Laptop"), fileLaptop);
                    break;

                case 12:
                    download = new DownloadTopics(swipe);
                    download.execute(forum.get("Mercatino"), fileMerchand);
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

    public static class DownloadTopics extends AsyncTask<String, Void, ArrayList<String>> {
        private SwipeRefreshLayout swipeRefreshLayout;
        private ArrayList<String> array = new ArrayList<>();
        private final String USER_AGENT = MainActivity.USER_AGENT;
        private String fileName;

        DownloadTopics(SwipeRefreshLayout srl) {
            swipeRefreshLayout = srl;
        }

        @Override
        protected ArrayList<String> doInBackground(String... params) {
            fileName = params[1];
            // SETTINGS PREFERENCE to get number of arguments to retrieve
            // IMPORTANT:cause a limitation until now it is not possible to
            // define a list of items
            // for settings preferences of Integer type. So they have must
            // declared String and then parsed of Integer type!!!!
            int numTopic = Integer.parseInt(settings.getString("THREADS_NUM", "50"));
            String line;
            String topic;
            String author;
            String date;
            String nposts;
            try {
                URL url = new URL(params[0]);
                Log.d("FORUM", url.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(25000);
                conn.setReadTimeout(30000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", USER_AGENT);
                conn.connect();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));

                    while ((line = br.readLine()) != null && !isCancelled()) {
                        if (line.contains("<div class=\"list-inner\">Argomenti</div>")) {
                            while ((line = br.readLine()) != null
                                    && numTopic > 0 && !isCancelled()) {
                                // retrieve the title of thread
                                if (line.contains("class=\"topictitle\">")) {
                                    Log.d("FORUM", "topictitle FOUND");
                                    topic = line.replace("&quot;", "\"");
                                    array.add((topic.substring(topic
                                            .indexOf("<a href=\"")) + "#")
                                            .replace("</a>", ""));
                                }

                                // extraction of number of posts for each topic
                                if (line.contains("<dfn>Risposte")) {
                                    nposts = line.substring(
                                            line.indexOf("posts\">") + 7,
                                            line.indexOf("<dfn>"));
                                    array.add(nposts + "##");
                                }

                                // get the author of the last post
                                if (line.contains("<dfn>Ultimo messaggio")) {
                                    author = line.replace("</a>", "");
                                    array.add(author.substring(author
                                            .lastIndexOf(">") + 1) + "###");
                                }

                                // obtain the time and date of the latest reply
                                if (line.contains("title=\"Vedi ultimo messaggio\"")) {
                                    // get internet url of the last topic
                                    Document doc = Jsoup.parse(line);
                                    Element linkHref = doc.select("a").first();
                                    String link = linkHref.attr("href");

                                    // get date format
                                    String lineCut = line.substring(
                                            line.lastIndexOf("/>") + 2,
                                            line.length() - 7);
                                    date = lineCut.replace("</a>", "");
                                    array.add(date + "####" + link);
                                    array.add("\n");
                                    numTopic--;
                                }
                            }
                            break;
                        }
                    }
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return array;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (swipeRefreshLayout != null)
                swipeRefreshLayout.setRefreshing(false);
            bar.setVisibility(View.GONE);
        }

        @Override
        protected void onPostExecute(ArrayList<String> a) {
            if (swipeRefreshLayout != null)
                swipeRefreshLayout.setRefreshing(false);
            bar.setVisibility(View.GONE);
            if (!isCancelled()) {
                if (a.isEmpty()) {
                    a.add(mContext.getString(R.string.downswipe));
                }
                createCacheFile(a);
                viewPagerAdapter.notifyDataSetChanged();
            }
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
        }
    }
}