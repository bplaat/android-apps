package nl.plaatsoft.bassietest;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import java.security.MessageDigest;

public class Utils {
    private Utils() {}

    // Function that hashes its input data to a md5 hash
    public static String md5(String data) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(data.getBytes());
            byte[] bytes = messageDigest.digest();
            String hash = "";
            for (int i = 0; i < bytes.length; i++) {
                hash += String.format("%02x", bytes[i]);
            }
            return hash;
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    // Function that opens the right store page for this app
    public static void openStorePage(Context context) {
        if (Config.SETTINGS_OVERRIDE_STORE_PAGE != null) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Config.SETTINGS_OVERRIDE_STORE_PAGE)));
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
