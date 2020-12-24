package net.tweakers.android;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebSettings;

public class MainActivity extends Activity {
    private WebView webview;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webview = new WebView(this);
        webview.setBackgroundColor(Color.TRANSPARENT);
        setContentView(webview);

        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webview.setWebViewClient(new CustomWebViewClient(this));

        Intent intent = getIntent();
        if (intent.getAction() == Intent.ACTION_VIEW) {
            webview.loadUrl(intent.getDataString());
        } else {
            webview.loadUrl("https://tweakers.net/");
        }
    }

    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getAction() == Intent.ACTION_VIEW) {
            webview.loadUrl(intent.getDataString());
        }
    }

    public void onBackPressed() {
        if (!Uri.parse(webview.getUrl()).getPath().equals("/") && webview.canGoBack()) {
            webview.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
