/*
 * Copyright (c) 2021-2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.rfidviewer.activities;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.tech.NfcA;
import android.nfc.tech.MifareClassic;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import java.util.Arrays;
import javax.annotation.Nullable;

import nl.plaatsoft.rfidviewer.tasks.MifareWriteTask;
import nl.plaatsoft.rfidviewer.Utils;
import nl.plaatsoft.rfidviewer.R;

public class WriteActivity extends BaseActivity {
    private static final int PENDING_INTENT_REQUEST_CODE = 1;

    private @SuppressWarnings("null") ScrollView formPage;
    private @SuppressWarnings("null") EditText formBlockIdInput;
    private @SuppressWarnings("null") ScrollView waitingPage;
    private @SuppressWarnings("null") ScrollView writingPage;
    private @SuppressWarnings("null") ScrollView successPage;
    private @SuppressWarnings("null") ScrollView errorPage;

    private @Nullable NfcAdapter nfcAdapter;
    private @Nullable PendingIntent pendingIntent;
    private @Nullable IntentFilter[] intentFiltersArray;
    private @Nullable String[][] techListsArray;
    private @Nullable MifareWriteTask mifareWriteTask;
    private boolean isWritePending;
    private int pendingBlockId;
    private byte[] pendingBlockData = new byte[16];

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        // Select all page views
        formPage = findViewById(R.id.write_form_page);
        formBlockIdInput = findViewById(R.id.write_form_block_id_input);
        var formDataAsciiInput = (EditText) findViewById(R.id.write_form_data_ascii_input);
        var formDataHexInput = (EditText) findViewById(R.id.write_form_data_hex_input);
        waitingPage = findViewById(R.id.write_waiting_page);
        writingPage = findViewById(R.id.write_writing_page);
        successPage = findViewById(R.id.write_success_page);
        errorPage = findViewById(R.id.write_error_page);
        useWindowInsets(formPage, waitingPage, writingPage, successPage, errorPage);

        // Set back button click listener
        findViewById(R.id.write_back_button).setOnClickListener(view -> {
            finish();
        });

        // Set write button click listener
        findViewById(R.id.write_form_button).setOnClickListener(view -> {
            try {
                pendingBlockId = Integer.parseInt(formBlockIdInput.getText().toString());

                Arrays.fill(pendingBlockData, (byte) 0);
                var dataAscii = formDataAsciiInput.getText().toString();
                if (dataAscii.length() > 0) {
                    for (var i = 0; i < dataAscii.length(); i++) {
                        pendingBlockData[i] = (byte) dataAscii.charAt(i);
                    }
                }
                var dataHex = formDataHexInput.getText().toString();
                if (dataHex.length() > 0) {
                    for (var i = 0; i < dataHex.length(); i += 2) {
                        if (i / 2 < 16) {
                            pendingBlockData[i / 2] = (byte) ((Character.digit(dataHex.charAt(i), 16) << 4) |
                                    Character.digit(dataHex.charAt(i + 1), 16));
                        }
                    }
                }

                // Enable writing
                isWritePending = true;
                openPage(waitingPage);
            } catch (Exception exception) {
                Log.e(getPackageName(), "Can't parse input", exception);
            }
        });

        // Set write success button click listener
        findViewById(R.id.write_success_button).setOnClickListener(view -> {
            // Clear inputs
            formBlockIdInput.getText().clear();
            formDataAsciiInput.getText().clear();
            formDataHexInput.getText().clear();

            // Show form page
            openPage(formPage);
        });

        // Set write error button click listener
        findViewById(R.id.write_error_button).setOnClickListener(view -> {
            // Enable writing
            isWritePending = true;
            openPage(waitingPage);
        });

        // Variables for NFC foreground intent dispatch
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null) {
            pendingIntent = PendingIntent.getActivity(this, PENDING_INTENT_REQUEST_CODE,
                    new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);
            intentFiltersArray = new IntentFilter[] { new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED) };
            techListsArray = new String[][] { new String[] { NfcA.class.getName(), MifareClassic.class.getName() } };
        }
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
    public void onNewIntent(@SuppressWarnings("null") Intent intent) {
        super.onNewIntent(intent);

        // Handle new incoming RFID tag messages when a write is pending
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()) && isWritePending) {
            isWritePending = false;
            var tag = Utils.intentGetParcelableExtra(intent, NfcAdapter.EXTRA_TAG, Tag.class);

            // Check if tag is Mifare Classic
            if (Arrays.asList(tag.getTechList()).contains(MifareClassic.class.getName())) {
                openPage(writingPage);

                // Write Mifare Classic tag async
                var mfc = MifareClassic.get(tag);
                mifareWriteTask = MifareWriteTask.with(this, mfc).writeBlock(pendingBlockId, pendingBlockData)
                        .then(() -> {
                            openPage(successPage);
                        }, exception -> {
                            Log.e(getPackageName(), "Can't write Mifare Classic tag", exception);
                            openPage(errorPage);
                        }).write();
            }
        }
    }

    @Override
    protected boolean shouldBackOverride() {
        if (formPage.getVisibility() != View.VISIBLE)
            return true;
        return false;
    }

    @Override
    protected void onBack() {
        // When back button press go back to form page
        if (formPage.getVisibility() != View.VISIBLE) {
            // When a mifare task is running cancel it
            if (writingPage.getVisibility() == View.VISIBLE && mifareWriteTask != null) {
                mifareWriteTask.cancel();
            }
            openPage(formPage);
        }
    }

    private void openPage(ScrollView page) {
        if (page.equals(formPage)) {
            // Show keyboard with focus on block id input
            formBlockIdInput.requestFocus();
            var imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(formBlockIdInput, 0);

            formPage.setVisibility(View.VISIBLE);
        } else {
            // Hide keyboard
            var focusView = getCurrentFocus();
            if (focusView != null) {
                var imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
            }

            formPage.setVisibility(View.GONE);
        }

        waitingPage.setVisibility(page.equals(waitingPage) ? View.VISIBLE : View.GONE);
        writingPage.setVisibility(page.equals(writingPage) ? View.VISIBLE : View.GONE);
        successPage.setVisibility(page.equals(successPage) ? View.VISIBLE : View.GONE);
        errorPage.setVisibility(page.equals(errorPage) ? View.VISIBLE : View.GONE);
        updateBackListener();
    }
}
