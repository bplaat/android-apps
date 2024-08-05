package ml.bastiaan.rfidviewer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class Utils {
    private Utils() {}

    // Function that resturns the url of the right store page
    public static String getStorePageUrl(Context context) {
        if (Config.APP_OVERRIDE_STORE_PAGE_URL != null) {
            return Config.APP_OVERRIDE_STORE_PAGE_URL;
        } else {
            return "https://play.google.com/store/apps/details?id=" + context.getPackageName();
        }
    }

    // Function that opens the right store page for this app
    public static void openStorePage(Context context) {
        if (Config.APP_OVERRIDE_STORE_PAGE_URL != null) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Config.APP_OVERRIDE_STORE_PAGE_URL)));
        } else {
            String appPackageName = context.getPackageName();
            try {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (Exception exception) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        }
    }
}
