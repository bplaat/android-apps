package ml.bastiaan.rfidviewer;

import android.content.Intent;
import android.nfc.tech.MifareClassic;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ScrollView;
import java.util.Arrays;

public class MainActivity extends BaseActivity {
    public static final int SETTINGS_REQUEST_CODE = 1;

    private Handler handler = new Handler(Looper.getMainLooper());
    private int oldLanguage = -1;
    private int oldTheme = -1;
    private ReadMifareTask readMifareTask;

    private ScrollView landingPage;
    private ScrollView readingPage;
    private ScrollView dataPage;
    private TextView dataOutputLabel;
    private ScrollView errorPage;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Select all page views
        landingPage = (ScrollView)findViewById(R.id.main_landing_page);
        readingPage = (ScrollView)findViewById(R.id.main_reading_page);
        dataPage = (ScrollView)findViewById(R.id.main_data_page);
        dataOutputLabel = (TextView)findViewById(R.id.main_data_output_label);
        errorPage = (ScrollView)findViewById(R.id.main_error_page);

        // Init settings button
        ((ImageButton)findViewById(R.id.main_settings_button)).setOnClickListener(view -> {
            oldLanguage = settings.getInt("language", Config.SETTINGS_LANGUAGE_DEFAULT);
            oldTheme = settings.getInt("theme", Config.SETTINGS_THEME_DEFAULT);
            startActivityForResult(new Intent(this, SettingsActivity.class), MainActivity.SETTINGS_REQUEST_CODE);
        });

        // Pass intent of to intent handler
        Intent intent = getIntent();
        if (intent != null) handleIntent(intent);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Restart activity when the langage or theme change in settings activity
        if (requestCode == MainActivity.SETTINGS_REQUEST_CODE) {
            if (oldLanguage != -1 && oldTheme != -1) {
                if (
                    oldLanguage != settings.getInt("language", Config.SETTINGS_LANGUAGE_DEFAULT) ||
                    oldTheme != settings.getInt("theme", Config.SETTINGS_THEME_DEFAULT)
                ) {
                    handler.post(() -> {
                        recreate();
                    });
                }
            }
        }
    }

    // When back button press go back to landing page
    public void onBackPressed() {
        if (landingPage.getVisibility() != View.VISIBLE) {
            // When a mifare task is runningcancel it
            if (readingPage.getVisibility() == View.VISIBLE && readMifareTask != null) {
                readMifareTask.cancel();
            }

            landingPage.setVisibility(View.VISIBLE);
            readingPage.setVisibility(View.GONE);
            dataPage.setVisibility(View.GONE);
            errorPage.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        // Handle new incoming RFID tag messages
        if (intent.getAction() != null && intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

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
                // Show reading page
                landingPage.setVisibility(View.GONE);
                readingPage.setVisibility(View.VISIBLE);
                dataPage.setVisibility(View.GONE);
                errorPage.setVisibility(View.GONE);

                // Read Mifare Classic tag async
                MifareClassic mfc = MifareClassic.get(tag);
                readMifareTask = ReadMifareTask.with(mfc).then(data -> {
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
                        landingPage.setVisibility(View.GONE);
                        readingPage.setVisibility(View.GONE);
                        dataPage.setVisibility(View.VISIBLE);
                        errorPage.setVisibility(View.GONE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, exception -> {
                    // When exception occurt show error page
                    landingPage.setVisibility(View.GONE);
                    readingPage.setVisibility(View.GONE);
                    dataPage.setVisibility(View.GONE);
                    errorPage.setVisibility(View.VISIBLE);
                }).read();
            } else {
                // Not an mifare classic tag print techs list, set data output string and show data page
                output.append("Not Mifare Classic: " + String.join(",", tag.getTechList()));
                dataOutputLabel.setText(output.toString());

                landingPage.setVisibility(View.GONE);
                readingPage.setVisibility(View.GONE);
                dataPage.setVisibility(View.VISIBLE);
                errorPage.setVisibility(View.GONE);
            }
        }
    }
}
