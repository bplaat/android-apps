package nl.plaatsoft.rfidviewer.activities;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.tech.NfcA;
import android.nfc.tech.MifareClassic;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ScrollView;
import java.util.Arrays;

import nl.plaatsoft.rfidviewer.tasks.MifareReadTask;
import nl.plaatsoft.rfidviewer.Consts;
import nl.plaatsoft.rfidviewer.Utils;
import nl.plaatsoft.rfidviewer.R;

public class MainActivity extends BaseActivity {
    public static final int SETTINGS_REQUEST_CODE = 1;

    private Handler handler;
    private int oldLanguage = -1;
    private int oldTheme = -1;
    private MifareReadTask mifareReadTask;

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;

    private ScrollView landingPage;
    private ScrollView readingPage;
    private ScrollView dataPage;
    private TextView dataOutputLabel;
    private ScrollView errorPage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler(Looper.getMainLooper());

        // Select all page views
        landingPage = (ScrollView)findViewById(R.id.main_landing_page);
        readingPage = (ScrollView)findViewById(R.id.main_reading_page);
        dataPage = (ScrollView)findViewById(R.id.main_data_page);
        dataOutputLabel = (TextView)findViewById(R.id.main_data_output_label);
        errorPage = (ScrollView)findViewById(R.id.main_error_page);

        // Init write button
        ((ImageButton)findViewById(R.id.main_write_button)).setOnClickListener(view -> {
            startActivity(new Intent(this, WriteActivity.class));
        });

        // Init settings button
        ((ImageButton)findViewById(R.id.main_settings_button)).setOnClickListener(view -> {
            oldLanguage = settings.getInt("language", Consts.Settings.LANGUAGE_DEFAULT);
            oldTheme = settings.getInt("theme", Consts.Settings.THEME_DEFAULT);
            startActivityForResult(new Intent(this, SettingsActivity.class), MainActivity.SETTINGS_REQUEST_CODE);
        });

        // Variables for NFC foreground intent dispatch
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null) {
            pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_IMMUTABLE);
            intentFiltersArray = new IntentFilter[] { new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED) };
            techListsArray = new String[][] { new String[] { NfcA.class.getName(), MifareClassic.class.getName() } };
        }

        // Pass intent of to intent handler
        Intent intent = getIntent();
        if (intent != null) onNewIntent(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Restart activity when the langage or theme change in settings activity
        if (requestCode == MainActivity.SETTINGS_REQUEST_CODE) {
            if (oldLanguage != -1 && oldTheme != -1) {
                if (
                    oldLanguage != settings.getInt("language", Consts.Settings.LANGUAGE_DEFAULT) ||
                    oldTheme != settings.getInt("theme", Consts.Settings.THEME_DEFAULT)
                ) {
                    handler.post(() -> {
                        recreate();
                    });
                }
            }
        }
    }

    // When back button press go back to landing page
    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        if (landingPage.getVisibility() != View.VISIBLE) {
            // When a mifare task is runningcancel it
            if (readingPage.getVisibility() == View.VISIBLE && mifareReadTask != null) {
                mifareReadTask.cancel();
            }
            openPage(landingPage);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Handle new incoming RFID tag messages
        if (intent.getAction() != null && intent.getAction().equals(NfcAdapter.ACTION_TECH_DISCOVERED)) {
            Tag tag = Utils.intentGetParcelableExtra(intent, NfcAdapter.EXTRA_TAG, Tag.class);

            // Create an output string add uid line
            StringBuilder output = new StringBuilder();
            output.append("Tag UID: ");
            byte[] uid = tag.getId();
            for (int i = 0; i < uid.length; i++) {
                output.append(String.format("%02x", uid[i]));
                output.append(i < uid.length - 1 ? " " : "\n");
            }

            // Check if tag is mifare classic
            if (Arrays.asList(tag.getTechList()).contains(MifareClassic.class.getName())) {
                openPage(readingPage);

                // Read Mifare Classic tag async
                MifareClassic mfc = MifareClassic.get(tag);
                mifareReadTask = MifareReadTask.with(mfc).then(data -> {
                    try {
                        // Generate output lines
                        output.append("Mifare Classic (" + data.length + " bytes):\n\n");
                        for (int i = 0; i < mfc.getSize() / 16; i++) {
                            output.append("Block " + i + ":\n");
                            // First half off the block
                            for (int j = 0; j < 8; j++) {
                                output.append(String.format("%02x ", data[i * 16 + j]));
                            }
                            output.append(" ");
                            for (int j = 0; j < 8; j++) {
                                output.append(data[i * 16 + j] >= 40 && data[i * 16 + j] <= 176 ? new String(new byte[] { data[i * 16 + j] }, "UTF-8") : ".");
                                output.append(j < 8 - 1 ? " " : "\n");
                            }

                            // Second half off the block
                            for (int j = 0; j < 8; j++) {
                                output.append(String.format("%02x ", data[i * 16 + 8 + j]));
                            }
                            output.append(" ");
                            for (int j = 0; j < 8; j++) {
                                output.append(data[i * 16 + 8 + j] >= 40 && data[i * 16 + 8 + j] <= 176 ? new String(new byte[] { data[i * 16 + 8 + j] }, "UTF-8") : ".");
                                output.append(j < 8 - 1 ? " " : "\n");
                            }
                        }

                        // Set lines in data output label and show data page
                        dataOutputLabel.setText(output.toString());
                        openPage(dataPage);
                    } catch (Exception exception) {
                        Log.e(Consts.LOG_TAG, "Can't read Mifare Classic tag", exception);
                    }
                }, exception -> {
                    Log.e(Consts.LOG_TAG, "Can't read Mifare Classic tag", exception);
                    openPage(errorPage);
                }).read();
            } else {
                // Not an mifare classic tag print techs list, set data output string and show data page
                output.append("Not Mifare Classic: " + String.join(",", tag.getTechList()));
                dataOutputLabel.setText(output.toString());
                openPage(dataPage);
            }
        }
    }

    private void openPage(ScrollView page) {
        landingPage.setVisibility(page.equals(landingPage) ? View.VISIBLE : View.GONE);
        readingPage.setVisibility(page.equals(readingPage) ? View.VISIBLE : View.GONE);
        dataPage.setVisibility(page.equals(dataPage) ? View.VISIBLE : View.GONE);
        errorPage.setVisibility(page.equals(errorPage) ? View.VISIBLE : View.GONE);
    }
}
