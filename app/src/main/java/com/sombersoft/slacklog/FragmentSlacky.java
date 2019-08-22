package com.sombersoft.slacklog;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.text.Html;
import android.text.Spanned;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class FragmentSlacky extends Fragment {

    private static File newsFile;
    private static TextView tvNews;
    private static SwipeRefreshLayout swipeRefreshLayout;
    private static ProgressBar bar;
    private static View view;
    private static Context mContext;
    private static RelativeLayout mainLayout;
    private static final String SLACKYSITE_URL = "https://www.slacky.eu/slacky/Pagina_principale";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.activity_fragment_slacky,
                container, false);
        mContext = view.getContext();
        bar = view.findViewById(R.id.progressBar);
        File rootDir = new File(mContext.getFilesDir(), "SlackLog");

        if (!rootDir.exists())
            rootDir.mkdir();

        mainLayout = view.findViewById(R.id.mainLayout);
        swipeRefreshLayout = view.findViewById(R.id.swipeUpdate);
        swipeRefreshLayout.setDistanceToTriggerSync(300);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateFile(false);
            }
        });

        FloatingActionButton fabSlacky = view.findViewById(R.id.fabSlacky);
        fabSlacky.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent openSite = new Intent(Intent.ACTION_VIEW, Uri.parse(SLACKYSITE_URL));
                if (ConnectionClass.isConnected(mContext))
                    startActivity(openSite);
                else
                    Snackbar.make(mainLayout, getString(R.string.no_conn), Snackbar.LENGTH_SHORT)
                            .show();
            }
        });

        newsFile = new File(rootDir, "newsSlacky.txt");
        tvNews = view.findViewById(R.id.tvNewsSlacky);

        if (!newsFile.exists())
            updateFile(true);

        showNewsSlacky();
        return view;
    }

    public static void updateFile(boolean setProgressBar) {
        if (setProgressBar)
            bar.setVisibility(View.VISIBLE);

        DownloadSlacky slackyDownload = new DownloadSlacky();
        slackyDownload.execute();
    }


    public static void showNewsSlacky() {
        if (newsFile.length() != 0) {
            tvNews.setBackgroundColor(Color.TRANSPARENT);

            try {
                BufferedReader br = new BufferedReader(new FileReader(newsFile));
                while (br.ready()) {
                    Spanned line = Html.fromHtml(br.readLine());
                    tvNews.append(line);
                }

                br.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            tvNews.setPadding(0, 50, 0, 0);
            tvNews.setTextSize(15);
            tvNews.setGravity(Gravity.CENTER);
            tvNews.setTextColor(Color.GRAY);
            tvNews.setText(mContext.getString(R.string.downswipe));
        }
    }

    /*
     * SlackyNews downloading async class
     */
    public static class DownloadSlacky extends
            AsyncTask<Void, Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            ArrayList<String> array = new ArrayList<>();

            if (ConnectionClass.isConnected(MainActivity.mContext)) {
                String line;
                int numNews = 0;
                try {
                    URL url = new URL(SLACKYSITE_URL);
                    HttpURLConnection conn = (HttpURLConnection) url
                            .openConnection();
                    conn.setConnectTimeout(15 * 1000);
                    conn.setReadTimeout(20 * 1000);

                    // Set the string sent in the User-Agent request header in http
                    // requests
                    conn.setRequestProperty("User-Agent", MainActivity.USER_AGENT);

                    int rCode = conn.getResponseCode();

                    if (rCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader br = new BufferedReader(
                                new InputStreamReader(conn.getInputStream()));
                        while ((line = br.readLine()) != null) {
                            if (isCancelled())
                                break;
                            if (line.contains("<div class=\"boxblue\">News</div>")) {
                                while ((line = br.readLine()) != null
                                        && numNews < 5) {
                                    if (isCancelled())
                                        break;
                                    if (line.startsWith("</pre>"))
                                        numNews++;

                                    array.add(line);
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

            if (a != null && !a.isEmpty()) {
                MainActivity.makeFile(a, MainActivity.newsFileSlacky);
            }
        }

        @Override
        protected void onCancelled() {
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
