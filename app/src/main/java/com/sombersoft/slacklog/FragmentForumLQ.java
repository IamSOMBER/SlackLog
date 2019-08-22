package com.sombersoft.slacklog;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class FragmentForumLQ extends Fragment {

    private File fileTopics;
    private Context context;
    private RecyclerView recyclerTopics;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.forum_fragment, container, false);
        context = view.getContext();
        final SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.refresh);
        swipeRefreshLayout.setDistanceToTriggerSync(300);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ForumLQ.update(false, swipeRefreshLayout);
            }
        });
        recyclerTopics = view.findViewById(R.id.recyclerTopics);
        recyclerTopics.setHasFixedSize(true);
        recyclerTopics.setLayoutManager(new LinearLayoutManager(context));
        String fileName = null;
        if (getArguments() != null) {
            fileName = getArguments().getString("fileName");
        }
        if (fileName != null) {
            fileTopics = new File(context.getCacheDir(), fileName);
        }
        viewForum();

        return view;
    }

    private void viewForum() {
        ArrayList<String> array = new ArrayList<>();
        BufferedReader br;
        String readLine;

        if (fileTopics.exists())
            try {
                br = new BufferedReader(new FileReader(fileTopics));

                while ((readLine = br.readLine()) != null) {
                    array.add(readLine);
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        if (array.isEmpty())
            array.add(getString(R.string.downswipe));

        ForumLQTopicAdapter adapter = new ForumLQTopicAdapter(context, array);
        recyclerTopics.setAdapter(adapter);
    }
}
