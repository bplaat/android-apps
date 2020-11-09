package nl.plaatsoft.redsquare.android;

import android.content.Context;
import android.os.Build;
import java.security.MessageDigest;

public class Utils {
    private Utils() {}

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

    @SuppressWarnings("deprecation")
    public static int getColor(Context context, int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getColor(id);
        } else {
            return context.getResources().getColor(id);
        }
    }
}
