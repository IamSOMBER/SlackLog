package com.sombersoft.slacklog;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class FragmentChangelog extends Fragment {

    private RecyclerView recyclerLog;
    private Context mContext;
    private File archFile;
    private File rootDir;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.changelog_fragment, container,
                false);
        mContext = view.getContext();
        recyclerLog = view.findViewById(R.id.recyclerLog);
        recyclerLog.setHasFixedSize(true);
        recyclerLog.setLayoutManager(new LinearLayoutManager(mContext));
        rootDir = new File(mContext.getFilesDir(), "SlackLog");
        String fileName = getArguments().getString("fileName");
        Log.d("CHANGELOG", fileName.toUpperCase());
        archFile = new File(rootDir, fileName);

        final SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Changelog.update(false, swipeRefreshLayout);
            }
        });

        viewLog();

        return view;
    }

    private void viewLog() {
        ArrayList<String> array = new ArrayList<>();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        // SETTINGS PREFERENCE to get number of arguments to retrieve
        // IMPORTANT:cause a limitation until now it is not possible to
        // define a list of items
        // for settings preferences of Integer type. So they have must
        // declared String and then parsed of Integer type!!!!
        int numLog = Integer.parseInt(settings.getString("LOG_NUM", "10"));

        if (archFile.exists()) {
            try {
                FileReader fr = new FileReader(archFile);
                BufferedReader br = new BufferedReader(fr);
                String line;
                StringBuilder builder = new StringBuilder();
                int cont = 0;

                while ((line = br.readLine()) != null && cont < numLog) {
                    if (line.contains("+--------")) {
                        cont++;
                        array.add(builder.toString());
                        builder.setLength(0);
                    } else
                        builder.append(line + "\n");
                }
                br.close();
                fr.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            array.add(getString(R.string.downswipe));
        }
        ChangelogAdapter adapter = new ChangelogAdapter(mContext, array);
        recyclerLog.setAdapter(adapter);
    }
}
