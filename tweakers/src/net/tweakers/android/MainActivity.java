/*
 * Copyright (c) 2020-2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package net.tweakers.android;

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
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;
import java.util.Map;
import java.util.HashMap;

public class MainActivity extends Activity implements OnBackInvokedCallback {
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

        // Disconnected page
        disconnectedPage = findViewById(R.id.main_disconnected_page);
        findViewById(R.id.main_disconnected_refresh_button).setOnClickListener(view -> webviewPage.reload());
        findViewById(R.id.main_disconnected_hero_button).setOnClickListener(view -> webviewPage.reload());

        // Webview handlers
        var urlAdCache = new HashMap<String, Boolean>();
        webviewPage.setWebViewClient(new WebViewClient() {
            @Override
            @SuppressWarnings("deprecation")
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return shouldOverrideUrlLoading(view, Uri.parse(url));
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return shouldOverrideUrlLoading(view, request.getUrl());
            }

            private boolean shouldOverrideUrlLoading(WebView view, Uri uri) {
                if (uri.getScheme().equals("https") && (uri.getHost().endsWith("tweakers.net") || uri.getHost().equals("myprivacy.dpgmedia.nl")))
                    return false;
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
                return true;
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                var adblocker = AdBlocker.getInstance(MainActivity.this);
                var url = request.getUrl().toString();

                boolean isAd;
                if (!urlAdCache.containsKey(url)) {
                    isAd = adblocker.isAd(request.getUrl());
                    urlAdCache.put(url, isAd);
                } else {
                    isAd = urlAdCache.get(url);
                }

                return isAd ? adblocker.createEmptyResource() : super.shouldInterceptRequest(view, request);
            }

            @Override
            @SuppressWarnings("deprecation")
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                onReceivedError(view);
            }

            @Override
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
                    updateBackListener();
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (disconnectedPage.getVisibility() == View.VISIBLE) {
                    disconnectedPage.setVisibility(View.GONE);
                    webviewPage.setVisibility(View.VISIBLE);
                }
                updateBackListener();

                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                CookieManager.getInstance().flush();

                super.onPageFinished(view, url);
            }
        });

        var intent = getIntent();
        if (intent.getAction() == Intent.ACTION_VIEW) {
            webviewPage.loadUrl(intent.getDataString());
        } else {
            webviewPage.loadUrl("https://tweakers.net/");
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getAction() == Intent.ACTION_VIEW) {
            webviewPage.loadUrl(intent.getDataString());
        }
    }

    protected boolean shouldBackOverride() {
        if (disconnectedPage.getVisibility() == View.VISIBLE) {
            return false;
        }
        if (Uri.parse(webviewPage.getUrl()).getPath().equals("/")) {
            return false;
        }
        if (webviewPage.canGoBack()) {
            return true;
        }
        return false;
    }

    @Override
    public void onBackInvoked() {
        if (webviewPage.canGoBack()) {
            webviewPage.goBack();
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        if (shouldBackOverride()) {
            onBackInvoked();
        } else {
            super.onBackPressed();
        }
    }

    protected void updateBackListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (shouldBackOverride()) {
                getOnBackInvokedDispatcher().registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT, this);
            } else {
                getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(this);
            }
        }
    }
}
