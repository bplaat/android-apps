/*
 * Copyright (c) 2024-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package com.ycombinator.news;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import nl.plaatsoft.android.alerts.UpdateAlert;
import nl.plaatsoft.android.compat.CompatActivity;
import nl.plaatsoft.android.compat.WebSettingsCompat;

import org.jspecify.annotations.Nullable;

public class MainActivity extends CompatActivity {
    private @SuppressWarnings("null") WebView webviewPage;
    private @SuppressWarnings("null") LinearLayout disconnectedPage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webviewPage = findViewById(R.id.main_webview_page);
        useWindowInsets(webviewPage);

        // Webview page
        webviewPage.setBackgroundColor(Color.TRANSPARENT);

        var webSettings = webviewPage.getSettings();
        webSettings.setJavaScriptEnabled(true);
        if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
            == Configuration.UI_MODE_NIGHT_YES) {
            WebSettingsCompat.setForceDark(webSettings, true);
        }

        // Disconnected page
        disconnectedPage = findViewById(R.id.main_disconnected_page);
        findViewById(R.id.main_disconnected_refresh_button).setOnClickListener(view -> webviewPage.reload());
        findViewById(R.id.main_disconnected_hero_button).setOnClickListener(view -> webviewPage.reload());

        // Webview handlers
        webviewPage.setWebViewClient(new WebViewClient() {
            @Override
            @SuppressWarnings("deprecation")
            public boolean shouldOverrideUrlLoading(
                @SuppressWarnings("null") WebView view, @SuppressWarnings("null") String url) {
                return shouldOverrideUrlLoading(view, Uri.parse(url));
            }

            @Override
            public boolean shouldOverrideUrlLoading(
                @SuppressWarnings("null") WebView view, @SuppressWarnings("null") WebResourceRequest request) {
                return shouldOverrideUrlLoading(view, request.getUrl());
            }

            private boolean shouldOverrideUrlLoading(WebView view, Uri uri) {
                if (uri.getScheme().equals("https") && uri.getHost().equals("news.ycombinator.com"))
                    return false;
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
                return true;
            }

            @Override
            @SuppressWarnings("deprecation")
            public void onReceivedError(@SuppressWarnings("null") WebView view, int errorCode,
                @SuppressWarnings("null") String description, @SuppressWarnings("null") String failingUrl) {
                onReceivedError(view);
            }

            @Override
            public void onReceivedError(@SuppressWarnings("null") WebView view,
                @SuppressWarnings("null") WebResourceRequest webResourceRequest,
                @SuppressWarnings("null") WebResourceError webResourceError) {
                if (webResourceRequest.isForMainFrame()) {
                    onReceivedError(view);
                }
            }

            private void onReceivedError(WebView view) {
                view.stopLoading();

                if (disconnectedPage.getVisibility() == View.GONE) {
                    webviewPage.setVisibility(View.GONE);
                    disconnectedPage.setVisibility(View.VISIBLE);
                    updateBackListener();
                }
            }

            @Override
            public void onPageStarted(@SuppressWarnings("null") WebView view, @SuppressWarnings("null") String url,
                @SuppressWarnings("null") Bitmap favicon) {
                if (disconnectedPage.getVisibility() == View.VISIBLE) {
                    disconnectedPage.setVisibility(View.GONE);
                    webviewPage.setVisibility(View.VISIBLE);
                }
                updateBackListener();

                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(@SuppressWarnings("null") WebView view, @SuppressWarnings("null") String url) {
                var density = getResources().getDisplayMetrics().density;
                view.loadUrl("javascript:(function(){document.body.style.paddingBottom='"
                    + (int)(view.getPaddingBottom() / density) + "px'})();");

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

        // Show update alert
        UpdateAlert.checkAndShow(this,
            "https://raw.githubusercontent.com/bplaat/android-apps/refs/heads/master/bin/hackernews/bob.toml",
            "https://github.com/bplaat/android-apps/tree/master/bin/hackernews");
    }

    @Override
    public void onNewIntent(@SuppressWarnings("null") Intent intent) {
        super.onNewIntent(intent);
        if (intent.getAction() == Intent.ACTION_VIEW)
            webviewPage.loadUrl(intent.getDataString());
    }

    @Override
    protected boolean shouldBackOverride() {
        if (disconnectedPage.getVisibility() == View.VISIBLE)
            return false;
        if (Uri.parse(webviewPage.getUrl()).getPath().equals("/"))
            return false;
        if (webviewPage.canGoBack())
            return true;
        return false;
    }

    @Override
    protected void onBack() {
        if (webviewPage.canGoBack())
            webviewPage.goBack();
    }
}
