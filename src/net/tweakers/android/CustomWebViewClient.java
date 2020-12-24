package net.tweakers.android;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import java.util.Map;
import java.util.HashMap;

public class CustomWebViewClient extends WebViewClient {
    private Context context;
    private Map<String, Boolean> urlAdCache = new HashMap<String, Boolean>();

    CustomWebViewClient(Context context) {
        this.context = context;
    }

    @SuppressWarnings("deprecation")
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return shouldOverrideUrlLoading(view, Uri.parse(url));
    }

    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return shouldOverrideUrlLoading(view, request.getUrl());
    }

    private boolean shouldOverrideUrlLoading(WebView view, Uri uri) {
        if (uri.getScheme().equals("https") && uri.getHost().equals("tweakers.net")) {
            return false;
        } else {
            try {
                context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
                return true;
            } catch (Exception exception) {
                exception.printStackTrace();
                return false;
            }
        }
    }

    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        AdBlocker adblocker = AdBlocker.getInstance(context);
        String url = request.getUrl().toString();

        boolean isAd;
        if (!urlAdCache.containsKey(url)) {
            isAd = adblocker.isAd(request.getUrl());
            urlAdCache.put(url, isAd);
        } else {
            isAd = urlAdCache.get(url);
        }

        return isAd ? adblocker.createEmptyResource() : super.shouldInterceptRequest(view, request);
    }

    public void onPageFinished(WebView view, String url) {
        CookieManager.getInstance().flush();

        super.onPageFinished(view, url);
    }
}
