package com.sombersoft.slacklog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

public class SlackwareWebViewFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.slackware_webview_fragment, container, false);
        final SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipe_slackware_update);
        final WebView myWebView = (WebView) view.findViewById(R.id.slackware_webview);
        myWebView.loadUrl("https://www.linux.com/");
        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (ConnectionClass.isConnected(view.getContext())) {
                    myWebView.reload();
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    Snackbar.make(view, R.string.no_conn, Snackbar.LENGTH_LONG).show();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
        return view;
    }
}
