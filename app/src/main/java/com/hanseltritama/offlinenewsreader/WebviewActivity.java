package com.hanseltritama.offlinenewsreader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.ArrayList;
import java.util.Objects;

public class WebviewActivity extends AppCompatActivity {

    ArrayList<String> content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        Intent intent = new Intent();
        content = Objects.requireNonNull(intent.getExtras()).getStringArrayList("NEWS_ARRAY");

        int array_pos = intent.getExtras().getInt("ARRAY_POSITION");

        WebView webView = findViewById(R.id.my_webview);

        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient());

        webView.loadData(content.get(array_pos), "text/html", "UTF-8");

    }
}
