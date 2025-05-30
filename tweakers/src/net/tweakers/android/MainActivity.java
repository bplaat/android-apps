/*
 * Copyright (c) 2020-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package net.tweakers.android;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import java.util.HashMap;
import javax.annotation.Nullable;

public class MainActivity extends BaseActivity {
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

        // Disconnected page
        disconnectedPage = findViewById(R.id.main_disconnected_page);
        findViewById(R.id.main_disconnected_refresh_button).setOnClickListener(view -> webviewPage.reload());
        findViewById(R.id.main_disconnected_hero_button).setOnClickListener(view -> webviewPage.reload());

        // Webview handlers
        var urlAdCache = new HashMap<String, Boolean>();
        webviewPage.setWebViewClient(new WebViewClient() {
            @Override
            @SuppressWarnings("deprecation")
            public boolean shouldOverrideUrlLoading(@SuppressWarnings("null") WebView view,
                    @SuppressWarnings("null") String url) {
                return shouldOverrideUrlLoading(view, Uri.parse(url));
            }

            @Override
            public boolean shouldOverrideUrlLoading(@SuppressWarnings("null") WebView view,
                    @SuppressWarnings("null") WebResourceRequest request) {
                return shouldOverrideUrlLoading(view, request.getUrl());
            }

            private boolean shouldOverrideUrlLoading(WebView view, Uri uri) {
                if (uri.getScheme().equals("https")
                        && (uri.getHost().endsWith("tweakers.net") || uri.getHost().equals("myprivacy.dpgmedia.nl")))
                    return false;
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
                return true;
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(@SuppressWarnings("null") WebView view,
                    @SuppressWarnings("null") WebResourceRequest request) {
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
                        + (int) (view.getPaddingBottom() / density) + "px'})();");

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
