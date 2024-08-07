package com.ycombinator.news;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import java.util.Map;
import java.util.HashMap;

public class MainActivity extends Activity {
    private WebView webviewPage;
    private LinearLayout disconnectedPage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Webview page
        webviewPage = findViewById(R.id.main_webview_page);
        webviewPage.setBackgroundColor(Color.TRANSPARENT);

        var webSettings = webviewPage.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettingsSetForceDark(webSettings);

        // Disconnected page
        disconnectedPage = findViewById(R.id.main_disconnected_page);
        findViewById(R.id.main_disconnected_refresh_button).setOnClickListener(view -> webviewPage.reload());
        findViewById(R.id.main_disconnected_hero_button).setOnClickListener(view -> webviewPage.reload());

        // Webview handlers
        webviewPage.setWebViewClient(new WebViewClient() {
            @SuppressWarnings("deprecation")
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return shouldOverrideUrlLoading(view, Uri.parse(url));
            }

            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return shouldOverrideUrlLoading(view, request.getUrl());
            }

            private boolean shouldOverrideUrlLoading(WebView view, Uri uri) {
                if (uri.getScheme().equals("https") && uri.getHost().equals("news.ycombinator.com"))
                    return false;
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
                return true;
            }

            @SuppressWarnings("deprecation")
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                onReceivedError(view);
            }

            public void onReceivedError(WebView view, WebResourceRequest webResourceRequest, WebResourceError webResourceError) {
                if (webResourceRequest.isForMainFrame()) {
                    onReceivedError(view);
                }
            }

            private void onReceivedError(WebView view) {
                view.stopLoading();

                if (disconnectedPage.getVisibility() == View.GONE) {
                    webviewPage.setVisibility(View.GONE);
                    disconnectedPage.setVisibility(View.VISIBLE);
                }
            }

            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (disconnectedPage.getVisibility() == View.VISIBLE) {
                    disconnectedPage.setVisibility(View.GONE);
                    webviewPage.setVisibility(View.VISIBLE);
                }

                super.onPageStarted(view, url, favicon);
            }

            public void onPageFinished(WebView view, String url) {
                CookieManager.getInstance().flush();

                super.onPageFinished(view, url);
            }
        });

        var intent = getIntent();
        if (intent.getAction() == Intent.ACTION_VIEW) {
            webviewPage.loadUrl(intent.getDataString());
        } else {
            webviewPage.loadUrl("https://news.ycombinator.com/");
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getAction() == Intent.ACTION_VIEW) {
            webviewPage.loadUrl(intent.getDataString());
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        if (disconnectedPage.getVisibility() == View.VISIBLE) {
            super.onBackPressed();
        }

        if (Uri.parse(webviewPage.getUrl()).getPath().equals("/")) {
            super.onBackPressed();
        }

        if (webviewPage.canGoBack()) {
            webviewPage.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("deprecation")
    private void webSettingsSetForceDark(WebSettings webSettings) {
        if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                webSettings.setAlgorithmicDarkeningAllowed(true);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                webSettings.setForceDark(WebSettings.FORCE_DARK_ON);
            }
        }
    }
}
