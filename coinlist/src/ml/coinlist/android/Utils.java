package ml.coinlist.android;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;

public class Utils {
    private Utils() {}

    @SuppressWarnings("deprecation")
    public static int contextGetColor(Context context, int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return new ContextWrapper(context).getColor(id);
        } else {
            return context.getResources().getColor(id);
        }
    }
}
