package com.sombersoft.slacklog;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

@SuppressWarnings("deprecation")
public class Kernel extends AppCompatActivity {

    private static File kernelFile;
    private RecyclerView lvKernel;
    private DownloadKernel download;
    private static KernelAdapter adapter;
    private final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.4 (KHTML, like Gecko) Chrome/22.0.1229.94 Safari/537.4";
    private SwipeRefreshLayout swipeRefreshLayout;
    private static ProgressBar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_kernel);

        File rootDir = new File(getFilesDir(), "SlackLog");
        swipeRefreshLayout = findViewById(R.id.refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (ConnectionClass.isConnected(getBaseContext()))
                    update();
                else
                    swipeRefreshLayout.setRefreshing(false);
            }
        });
        bar = findViewById(R.id.progressBar);
        // creation of object to avoid NullPointerException exception when back
        // button is pressed
        download = new DownloadKernel();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.drawable.ic_launcher);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        kernelFile = new File(rootDir, "kernel.html");

        lvKernel = findViewById(R.id.lvKernel);
        lvKernel.setHasFixedSize(true);
        lvKernel.setLayoutManager(new LinearLayoutManager(this));

        TextView tvKernelSource = findViewById(R.id.tvFonteKernel);
        tvKernelSource.setShadowLayer(2, 1, 1, Color.WHITE);

        // Sponsors button to open web sites
        ImageView ivRedhat = findViewById(R.id.imageViewRedhat);
        ivRedhat.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://www.redhat.com/");
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
        });

        ImageView ivLinuxFound = findViewById(R.id.imageViewLinuxFound);
        ivLinuxFound.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://www.linuxfoundation.org/");
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
        });

        ImageView ivISC = findViewById(R.id.imageViewHostedISC);
        ivISC.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://www.isc.org/services/host/");
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
        });

        ImageView ivFastly = findViewById(R.id.imageViewFastly);
        ivFastly.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://www.fastly.com/");
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
        });

        ImageView ivOsl = findViewById(R.id.imageViewOsl);
        ivOsl.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://www.osuosl.org/");
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
        });

        ImageView ivVexx = findViewById(R.id.imageViewVexxHost);
        ivVexx.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://vexxhost.com/");
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
        });

        if (kernelFile.exists()) {
            ArrayList<String> array = new ArrayList<>();
            try {
                FileReader fr = new FileReader(kernelFile);
                BufferedReader br = new BufferedReader(fr);
                String line;
                while ((line = br.readLine()) != null) {
                    array.add(line);
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            adapter = new KernelAdapter(array);
            lvKernel.setAdapter(adapter);

        } else {
            bar.setVisibility(View.VISIBLE);
            update();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
            download.cancel(true);
        }
        if (bar.isShown())
            bar.setVisibility(View.GONE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
            download.cancel(true);
        }
        if (bar.isShown())
            bar.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setEnabled(false);
            download.cancel(true);
        }
    }

    private void update() {
        // AsyncTask can be executed just one time. If update button is pressed
        // while download is still running IllegalStateEception error would
        // be raised without this line
        download = new DownloadKernel();
        download.execute();
    }

    public void view() {
        ArrayList<String> array = new ArrayList<>();
        try {
            FileReader fr = new FileReader(kernelFile);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                array.add(line);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        adapter = new KernelAdapter(array);
        lvKernel.setAdapter(adapter);
    }

    /*
     * News file creation by the array retrieve from Download Class
     */
    public void makeFile(ArrayList<String> a) {
        Iterator<String> i = a.iterator();
        String line;
        try {
            FileWriter fw = new FileWriter(kernelFile);
            BufferedWriter bw = new BufferedWriter(fw);
            while (i.hasNext()) {
                if ((line = i.next()).contains("align=\"left\"")) {
                    bw.newLine();
                    bw.write(line);
                } else
                    bw.write(line);
            }

            bw.close();
            fw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class DownloadKernel extends
            AsyncTask<Void, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            ArrayList<String> array = new ArrayList<>();
            String line;

            if (ConnectionClass.isConnected(getBaseContext())) {
                try {
                    URL url = new URL("https://www.kernel.org/");
                    HttpURLConnection conn = (HttpURLConnection) url
                            .openConnection();
                    conn.setConnectTimeout(25 * 1000);
                    conn.setReadTimeout(40 * 1000);
                    conn.setRequestProperty("User-Agent", USER_AGENT);
                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader br = new BufferedReader(
                                new InputStreamReader(conn.getInputStream()));
                        while ((line = br.readLine()) != null && !isCancelled()) {
                            if (line.contains("mainline")) {
                                array.add("&nbsp" + line + "&nbsp");

                                while ((line = br.readLine()) != null
                                        && !isCancelled()) {
                                    if (!line.contains("href"))
                                        array.add(line + "&nbsp");
                                    if (line.contains("<strong>next"))
                                        break;
                                }
                            }
                        }
                        br.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return array;
        }

        @Override
        protected void onPostExecute(ArrayList<String> a) {
            swipeRefreshLayout.setRefreshing(false);
            bar.setVisibility(View.GONE);
            if (a.isEmpty())
                a.add(getString(R.string.downswipe));
            makeFile(a);
            view();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
                download.cancel(true);
            }
        }
    }
}
