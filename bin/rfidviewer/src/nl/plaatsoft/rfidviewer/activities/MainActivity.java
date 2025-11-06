/*
 * Copyright (c) 2021-2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.rfidviewer.activities;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;

import nl.plaatsoft.android.alerts.UpdateAlert;
import nl.plaatsoft.android.compat.IntentCompat;
import nl.plaatsoft.rfidviewer.R;
import nl.plaatsoft.rfidviewer.tasks.MifareReadTask;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class MainActivity extends BaseActivity implements PopupMenu.OnMenuItemClickListener {
    private static final int PENDING_INTENT_REQUEST_CODE = 0;
    private static final int SETTINGS_REQUEST_CODE = 1;

    private @SuppressWarnings("null") ScrollView landingPage;
    private @SuppressWarnings("null") ScrollView readingPage;
    private @SuppressWarnings("null") ScrollView dataPage;
    private @SuppressWarnings("null") TextView dataOutputLabel;
    private @SuppressWarnings("null") ScrollView errorPage;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private int oldLanguage = -1;
    private int oldTheme = -1;

    private @Nullable MifareReadTask mifareReadTask;
    private @Nullable NfcAdapter nfcAdapter;
    private @Nullable PendingIntent pendingIntent;
    private @NonNull IntentFilter[] intentFiltersArray = new IntentFilter[0];
    private @NonNull String[][] techListsArray = new String[0][0];

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        landingPage = findViewById(R.id.main_landing_page);
        readingPage = findViewById(R.id.main_reading_page);
        dataPage = findViewById(R.id.main_data_page);
        dataOutputLabel = findViewById(R.id.main_data_output_label);
        errorPage = findViewById(R.id.main_error_page);
        useWindowInsets(landingPage, readingPage, dataPage, errorPage);

        // Write button
        findViewById(R.id.main_write_button).setOnClickListener(view -> {
            startActivity(new Intent(this, WriteActivity.class));
        });

        // Options menu button
        findViewById(R.id.main_options_menu_button).setOnClickListener(view -> {
            var optionsMenu = new PopupMenu(this, view, Gravity.TOP | Gravity.RIGHT);
            optionsMenu.getMenuInflater().inflate(R.menu.options, optionsMenu.getMenu());
            optionsMenu.setOnMenuItemClickListener(this);
            optionsMenu.show();
        });

        // Variables for NFC foreground intent dispatch
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null) {
            pendingIntent = PendingIntent.getActivity(this, PENDING_INTENT_REQUEST_CODE,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);
            intentFiltersArray = new IntentFilter[] {new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)};
            techListsArray = new String[][] {new String[] {NfcA.class.getName(), MifareClassic.class.getName()}};
        }

        // Pass intent of to intent handler
        var intent = getIntent();
        if (intent != null)
            onNewIntent(intent);

        // Show update alert
        UpdateAlert.checkAndShow(this,
            "https://raw.githubusercontent.com/bplaat/android-apps/refs/heads/master/bin/rfidviewer/bob.toml",
            SettingsActivity.STORE_PAGE_URL);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (nfcAdapter != null)
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (nfcAdapter != null)
            nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    public boolean onMenuItemClick(@SuppressWarnings("null") MenuItem item) {
        if (item.getItemId() == R.id.menu_options_settings) {
            oldLanguage = settings.getLanguage();
            oldTheme = settings.getTheme();
            startActivityForResult(new Intent(this, SettingsActivity.class), SETTINGS_REQUEST_CODE);
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @SuppressWarnings("null") Intent data) {
        // When settings activity is closed check for restart
        if (requestCode == SETTINGS_REQUEST_CODE) {
            if (oldLanguage != -1 && oldTheme != -1) {
                if (oldLanguage != settings.getLanguage() || oldTheme != settings.getTheme()) {
                    handler.post(() -> recreate());
                }
            }
        }
    }

    @Override
    public void onNewIntent(@SuppressWarnings("null") Intent intent) {
        super.onNewIntent(intent);

        // Handle new incoming RFID tag messages
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            var tag = IntentCompat.getParcelableExtra(intent, NfcAdapter.EXTRA_TAG, Tag.class);

            // Create an output string add uid line
            var sb = new StringBuilder();
            sb.append("Tag UID: ");
            var uid = tag.getId();
            for (var i = 0; i < uid.length; i++) {
                sb.append(String.format("%02x", uid[i]));
                sb.append(i < uid.length - 1 ? " " : "\n");
            }

            // Check if tag is Mifare Classic
            if (Arrays.asList(tag.getTechList()).contains(MifareClassic.class.getName())) {
                openPage(readingPage);

                // Read Mifare Classic tag async
                var mfc = MifareClassic.get(tag);
                mifareReadTask =
                    MifareReadTask.with(this, mfc)
                        .then(
                            data
                            -> {
                                try {
                                    // Generate output lines
                                    sb.append("Mifare Classic (" + data.length + " bytes):\n\n");
                                    for (var i = 0; i < mfc.getSize() / 16; i++) {
                                        sb.append("Block " + i + ":\n");
                                        // First half off the block
                                        for (var j = 0; j < 8; j++) {
                                            sb.append(String.format("%02x ", data[i * 16 + j]));
                                        }
                                        sb.append(" ");
                                        for (var j = 0; j < 8; j++) {
                                            sb.append(data[i * 16 + j] >= 40 && data[i * 16 + j] <= 176
                                                    ? new String(new byte[] {data[i * 16 + j]}, StandardCharsets.UTF_8)
                                                    : ".");
                                            sb.append(j < 8 - 1 ? " " : "\n");
                                        }

                                        // Second half off the block
                                        for (var j = 0; j < 8; j++) {
                                            sb.append(String.format("%02x ", data[i * 16 + 8 + j]));
                                        }
                                        sb.append(" ");
                                        for (var j = 0; j < 8; j++) {
                                            sb.append(data[i * 16 + 8 + j] >= 40 && data[i * 16 + 8 + j] <= 176
                                                    ? new String(
                                                          new byte[] {data[i * 16 + 8 + j]}, StandardCharsets.UTF_8)
                                                    : ".");
                                            sb.append(j < 8 - 1 ? " " : "\n");
                                        }
                                    }

                                    // Set lines in data output label and show data page
                                    dataOutputLabel.setText(sb.toString());
                                    openPage(dataPage);
                                } catch (Exception exception) {
                                    Log.e(getPackageName(), "Can't read Mifare Classic tag", exception);
                                }
                            },
                            exception -> {
                                Log.e(getPackageName(), "Can't read Mifare Classic tag", exception);
                                openPage(errorPage);
                            })
                        .read();
            } else {
                // Not an Mifare Classic tag print techs list, set data output string and show
                // data page
                sb.append("Not Mifare Classic: " + String.join(",", tag.getTechList()));
                dataOutputLabel.setText(sb.toString());
                openPage(dataPage);
            }
        }
    }

    @Override
    protected boolean shouldBackOverride() {
        if (landingPage.getVisibility() != View.VISIBLE)
            return true;
        return false;
    }

    @Override
    protected void onBack() {
        // When back button press go back to landing page
        if (landingPage.getVisibility() != View.VISIBLE) {
            // When a Mifare task is running cancel it
            if (readingPage.getVisibility() == View.VISIBLE && mifareReadTask != null)
                mifareReadTask.cancel();
            openPage(landingPage);
        }
    }

    private void openPage(ScrollView page) {
        landingPage.setVisibility(page.equals(landingPage) ? View.VISIBLE : View.GONE);
        readingPage.setVisibility(page.equals(readingPage) ? View.VISIBLE : View.GONE);
        dataPage.setVisibility(page.equals(dataPage) ? View.VISIBLE : View.GONE);
        errorPage.setVisibility(page.equals(errorPage) ? View.VISIBLE : View.GONE);
        updateBackListener();
    }
}
