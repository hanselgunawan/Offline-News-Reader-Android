package com.hanseltritama.offlinenewsreader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.ArrayList;

public class WebviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        Intent intent = getIntent();

        WebView webView = findViewById(R.id.my_webview);

        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient());

        webView.loadData(intent. getStringExtra("NEWS_CONTENT"), "text/html", "UTF-8");

    }
}
