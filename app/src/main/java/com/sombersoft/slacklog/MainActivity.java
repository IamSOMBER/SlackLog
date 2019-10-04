package com.sombersoft.slacklog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity {

    public static Context mContext;
    public static File newsFileSlacky;
    public static File newsFileAlien;
    private static final int SETTINGS_MANAGEMENT = 1;
    private static final String TAB_PREVIOUS_STATE = "selectedTab";
    private static ViewPager viewPager;
    private static MainAdapter mAdapter;
    private SharedPreferences slackPreferences;
    private Editor editor;
    private Toast mToast;
    private ActionBarDrawerToggle mDrawerToggle;
    public static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/76.0.3809.132 Safari/537.36 OPR/63.0.3368.66";
    private final String EXECUTE_UPDATE = "auto_update_flag";
    private ChangelogBroadcastReceiver changelogBroadcastReceiver;
    private MultilibBroadcastReceiver multilibBroadcastReceiver;

    @SuppressLint("ShowToast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        changelogBroadcastReceiver = new ChangelogBroadcastReceiver();
        multilibBroadcastReceiver = new MultilibBroadcastReceiver();
        registerReceiver(changelogBroadcastReceiver, intentFilter);
        registerReceiver(multilibBroadcastReceiver, intentFilter);

        mToast = Toast.makeText(this, getString(R.string.exit),
                Toast.LENGTH_SHORT);
        mContext = this;
        FragmentManager fm = getSupportFragmentManager();
        String PREF = "slackPref";
        // FOLDER CREATION in internal storage
        File rootDir = new File(getFilesDir(), "SlackLog");

        if (!rootDir.exists())
            if (!rootDir.mkdir()) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle(getString(R.string.error_title))
                        .setMessage(getString(R.string.write_error))
                        .setPositiveButton(getString(R.string.close),
                                new OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        finish();
                                    }
                                }).create().show();
            }

        newsFileAlien = new File(rootDir, "newsAlien.txt");
        newsFileSlacky = new File(rootDir, "newsSlacky.txt");
        slackPreferences = getSharedPreferences(PREF, MODE_PRIVATE);
        editor = slackPreferences.edit();
        // Initialize preferences with default values contained in xml folder
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.drawer);
        viewPager = findViewById(R.id.mainPager);
        mAdapter = new MainAdapter(fm);
        viewPager.setAdapter(mAdapter);
        TabLayout tabLayout = findViewById(R.id.htab_tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
        viewPager.setCurrentItem(slackPreferences.getInt(
                STATE_SELECTED_NAVIGATION_ITEM, 0));

        // Set up the action bar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                toolbar, 0, 0) {
            public void onDrawerClosed(View view) {
                supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                supportInvalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                switch (item.getItemId()) {
                    case R.id.changelog:
                        try {
                            intent = new Intent(getBaseContext(), Changelog.class);
                            startActivity(intent);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        return true;

                    case R.id.multilib:
                        try {
                            intent = new Intent(getBaseContext(), Multilib.class);
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        return true;

                    case R.id.kernel:
                        try {
                            intent = new Intent(getBaseContext(), Kernel.class);
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return true;

                    case R.id.forum:
                        try {
                            editor.putBoolean(EXECUTE_UPDATE, true);
                            editor.apply();
                            intent = new Intent(getBaseContext(), Forum.class);
                            startActivity(intent);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return true;

                    case R.id.forum_lq:
                        try {
                            editor.putBoolean(EXECUTE_UPDATE, true);
                            editor.apply();
                            intent = new Intent(getBaseContext(), ForumLQ.class);
                            startActivity(intent);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return true;

                    /*case R.id.donate:
                        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
                        View promptView = layoutInflater.inflate(R.layout.dialog_amount, null);

                        ImageView ivClickPaypal = promptView.findViewById(R.id.ivClickPaypal);
                        ivClickPaypal.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // PayPal.Me personal link
                                Uri paypalMe = Uri.parse("https://www.paypal.me/MirkoCruciani");
                                Intent openPaypal = new Intent(Intent.ACTION_VIEW, paypalMe);
                                startActivity(openPaypal);
                            }
                        });

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle(getString(R.string.thanks))
                                .setView(promptView)
                                .setMessage(getString(R.string.amount))
                                .setCancelable(true);
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();

                        return true;*/
                }
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(changelogBroadcastReceiver);
            unregisterReceiver(multilibBroadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        editor.putInt(TAB_PREVIOUS_STATE, viewPager.getCurrentItem());
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewPager.setCurrentItem(slackPreferences.getInt(TAB_PREVIOUS_STATE, 0));
    }

    /*
     * hardware menu button
     */
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivityForResult(i, SETTINGS_MANAGEMENT);
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater mInfl = getMenuInflater();
        mInfl.inflate(R.menu.settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case (R.id.abSettings):
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        // creation of the toast to confirm exit from app
        if (mToast.getView().isShown()) {
            mToast.cancel();
            super.onBackPressed();
        } else
            mToast.show();
    }

    /*
     * Creation of the news file, passing the array obtained by downloading
     * class
     */
    public static void makeFile(ArrayList<String> a, File file) {
        Iterator<String> i = a.iterator();
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            while (i.hasNext()) {
                bw.write(i.next() + " ");
            }

            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        mAdapter.notifyDataSetChanged();
        viewPager.destroyDrawingCache();
    }
}