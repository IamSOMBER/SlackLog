package com.sombersoft.slacklog;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class FragmentAlien extends Fragment {

    private static File newsFile;
    private static TextView tvNews;
    private static SwipeRefreshLayout swipeRefreshLayout;
    private static ProgressBar bar;
    private static View view;
    private static Context mContext;
    private static RelativeLayout mainLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_fragment_alien,
                container, false);
        mContext = view.getContext();
        bar = view.findViewById(R.id.progressBar);
        File rootDir = new File(mContext.getFilesDir(), "SlackLog");

        if (!rootDir.exists())
            rootDir.mkdir();

        mainLayout = view.findViewById(R.id.mainLayout);
        swipeRefreshLayout = view.findViewById(R.id.swipeUpdate);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                DownloadAlien alienDownload = new DownloadAlien();
                alienDownload.execute();
            }
        });

        FloatingActionButton fabAlien = view.findViewById(R.id.fabAlien);
        fabAlien.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openSite = new Intent(Intent.ACTION_VIEW, Uri.parse("https://alien.slackbook.org/blog/"));
                if (ConnectionClass.isConnected(mContext))
                    startActivity(openSite);
                else
                    Snackbar.make(mainLayout, getString(R.string.no_conn), Snackbar.LENGTH_SHORT)
                            .show();
            }
        });

        newsFile = new File(rootDir, "newsAlien.txt");
        tvNews = view.findViewById(R.id.tvNewsAlien);

        if (!newsFile.exists())
            updateFile(true);

        final SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeUpdate);
        swipeRefreshLayout.setDistanceToTriggerSync(300);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateFile(false);
            }
        });
        showNewsAlien();
        return view;
    }

    public static void updateFile(boolean setProgressBar) {
        if (setProgressBar)
            bar.setVisibility(View.VISIBLE);
        DownloadAlien alienDownload = new DownloadAlien();
        alienDownload.execute();
    }

    public static void showNewsAlien() {
        if (newsFile.length() != 0) {
            tvNews.setBackgroundColor(Color.TRANSPARENT);
            try {
                BufferedReader br = new BufferedReader(new FileReader(newsFile));
                tvNews.invalidate();
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
            tvNews.setTextColor(Color.LTGRAY);
            tvNews.setText(mContext.getString(R.string.downswipe));
        }
    }

    /*
     * AlienNews downloading async class
     */
    public static class DownloadAlien extends AsyncTask<Void, Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            ArrayList<String> array = new ArrayList<>();
            String stringHtml;
            if (ConnectionClass.isConnected(MainActivity.mContext)) {
                try {
                    Document document = Jsoup.connect("https://alien.slackbook.org/blog").get();
                    array.add(document.title() + "<br>" + "<br>");
                    Elements classes = document.select("article[id^=post-]");
                    for (Element element : classes) {
                        stringHtml = element.html();
                        array.add(stringHtml.replace(stringHtml.substring(stringHtml.indexOf("<img class=\"alignleft"), stringHtml.indexOf("</noscript>")),
                                ""));
                        if (stringHtml.contains("Posted on")){
                            array.add("<br>");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Snackbar.make(mainLayout, mContext.getString(R.string.no_conn), Snackbar.LENGTH_SHORT)
                        .show();
            }
            return array;
        }

        @Override
        protected void onPostExecute(ArrayList<String> a) {
            swipeRefreshLayout.setRefreshing(false);
            bar.setVisibility(View.GONE);
            if (a != null && !a.isEmpty()) {
                MainActivity.makeFile(a, MainActivity.newsFileAlien);
            }
        }

        @Override
        protected void onCancelled() {
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
