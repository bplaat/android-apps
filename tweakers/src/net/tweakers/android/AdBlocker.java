package net.tweakers.android;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebResourceResponse;
import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class AdBlocker {
    private static AdBlocker instance;

    private Context context;
    private Set<String> hostsBlacklist;

    private AdBlocker(Context context) {
        this.context = context;
        hostsBlacklist = new HashSet<String>();

        try (var bufferedReader = new BufferedReader(new InputStreamReader(context.getAssets().open("blacklist-adservers.txt")))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.length() > 0) {
                    hostsBlacklist.add(line);
                }
            }
        } catch (Exception exception) {
            Log.e(context.getPackageName(), "Can't read / parse adservers hosts file", exception);
        }
    }

    public static AdBlocker getInstance(Context context) {
        if (instance == null) {
            instance = new AdBlocker(context);
        }
        return instance;
    }

    public boolean isAd(Uri uri) {
        return isAdHost(uri.getHost());
    }

    private boolean isAdHost(String host) {
        if (host == null || host.length() == 0) {
            return false;
        }

        var index = host.indexOf(".");
        return index >= 0 && (hostsBlacklist.contains(host) || index + 1 < host.length() && isAdHost(host.substring(index + 1)));
    }

    public WebResourceResponse createEmptyResource() {
        return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("".getBytes()));
    }
}
