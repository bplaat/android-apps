/*
 * Copyright (c) 2021-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bassiemusic;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.util.Log;
import java.security.MessageDigest;

public class Utils {
    private Utils() {
    }

    @SuppressWarnings("deprecation")
    public static int contextGetColor(Context context, int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return new ContextWrapper(context).getColor(id);
        return context.getResources().getColor(id);
    }

    public static String md5(Context context, String data) {
        try {
            var messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(data.getBytes());
            var bytes = messageDigest.digest();
            var hashBuilder = new StringBuilder();
            for (var i = 0; i < bytes.length; i++) {
                hashBuilder.append(String.format("%02x", bytes[i]));
            }
            return hashBuilder.toString();
        } catch (Exception exception) {
            Log.e(context.getPackageName(), "An exception catched!", exception);
            return null;
        }
    }
}
