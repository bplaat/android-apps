package net.tweakers.android;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;

public class CustomWebViewClient extends WebViewClient {
    private Context context;

    CustomWebViewClient(Context context) {
        this.context = context;
    }

    @SuppressWarnings("deprecation")
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return handleUri(Uri.parse(url));
    }

    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return handleUri(request.getUrl());
    }

    private boolean handleUri(Uri uri) {
        if (uri.getScheme().equals("https") && uri.getHost().equals("tweakers.net")) {
            return false;
        }

        else {
            context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
            return true;
        }
    }

    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        CookieManager.getInstance().flush();
    }
}
