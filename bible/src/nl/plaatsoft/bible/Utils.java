package nl.plaatsoft.bible;

import android.content.Context;
import android.os.Build;

public class Utils {
    private Utils() {}

    @SuppressWarnings("deprecation")
    public static int contextGetColor(Context context, int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return context.getResources().getColor(id, null);
        return context.getResources().getColor(id);
    }
}
