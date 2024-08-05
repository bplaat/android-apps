package nl.plaatsoft.redsquare.android;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.util.Log;
import android.view.Window;
import java.security.MessageDigest;

public class Utils {
    private Utils() {}

    @SuppressWarnings("deprecation")
    public static void windowSetDecorFitsSystemWindows(Window window, boolean decorFitsSystemWindows) {
        window.setDecorFitsSystemWindows(decorFitsSystemWindows);
    }

    @SuppressWarnings("deprecation")
    public static int contextGetColor(Context context, int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return new ContextWrapper(context).getColor(id);
        } else {
            return context.getResources().getColor(id);
        }
    }

    public static String md5(String data) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(data.getBytes());
            byte[] bytes = messageDigest.digest();
            StringBuilder hashBuilder = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                hashBuilder.append(String.format("%02x", bytes[i]));
            }
            return hashBuilder.toString();
        }
        catch (Exception exception) {
            Log.e(Consts.LOG_TAG, "Can't digest md5 hash", exception);
            return null;
        }
    }
}
