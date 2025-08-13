/*
 * Copyright (c) 2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.alerts;

import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;

import nl.plaatsoft.android.compat.ContextCompat;
import nl.plaatsoft.android.fetch.FetchDataTask;

public class UpdateAlert {
    @SuppressWarnings("unused")
    public static void checkAndShow(Context context, String versionUrl, String storePageUrl) {
        // Check if app is installed from direct .apk
        String installerPackageName = ContextCompat.getInstallerPackageName(context);
        if (installerPackageName != null) {
            return;
        }

        // Fetch version url
        FetchDataTask.with(context).load(versionUrl).then(data -> {
            try {
                var matcher = Pattern.compile("(\\d+\\.\\d+\\.\\d+)").matcher(new String(data));
                if (matcher.find()) {
                    var foundVersion = matcher.group(1);
                    var appName = context.getPackageManager().getApplicationLabel(context.getApplicationInfo());
                    var appVersion = context.getPackageManager().getPackageInfo(context.getPackageName(),
                            0).versionName;
                    if (!foundVersion.equals(appVersion)) {
                        new AlertDialog.Builder(context)
                                .setTitle(context.getString(R.string.updatealert_title_label).replace("$0", appName))
                                .setMessage(context.getString(R.string.updatealert_message_label)
                                        .replace("$0", appVersion).replace("$1", appName))
                                .setPositiveButton(R.string.updatealert_update_button, (dialog, which) -> {
                                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(storePageUrl)));
                                })
                                .setNegativeButton(R.string.updatealert_later_button, null)
                                .show();
                    }
                }
            } catch (NameNotFoundException e) {
            }
        }).fetch();
    }
}
