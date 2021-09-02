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
import java.util.Arrays;
import java.util.List;

public class MainActivity extends BaseActivity {
    public static final int SETTINGS_REQUEST_CODE = 1;

    private Handler handler = new Handler(Looper.getMainLooper());
    private int oldLanguage = -1;
    private int oldTheme = -1;
    private TextView outputText;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init settings button
        ((ImageButton)findViewById(R.id.main_settings_button)).setOnClickListener(view -> {
            oldLanguage = settings.getInt("language", Config.SETTINGS_LANGUAGE_DEFAULT);
            oldTheme = settings.getInt("theme", Config.SETTINGS_THEME_DEFAULT);
            startActivityForResult(new Intent(this, SettingsActivity.class), MainActivity.SETTINGS_REQUEST_CODE);
        });

        outputText = (TextView)findViewById(R.id.main_output);
    }

    // When come back of the settings activity check for restart
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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

    // On new intent
    public void onNewIntent(Intent intent) {
        if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            StringBuilder output = new StringBuilder();
            output.append("Tag UID: ");
            byte[] uid = tag.getId();
            for (int i = 0; i < uid.length; i++) {
                output.append(String.format("%02x ", uid[i]));
            }
            output.append("\n");

            // Check if tag is mifare classic
            if ((Arrays.asList(tag.getTechList())).contains(MifareClassic.class.getName())) {
                outputText.setText(getResources().getString(R.string.main_reading_tag));

                MifareClassic mc = MifareClassic.get(tag);
                ReadMifareTask.with(mc).then(data -> {
                    try {
                        output.append("Mifare Classic (" + data.length + " bytes):\n\n");
                        for (int i = 0; i < mc.getSize() / 16; i++) {
                            output.append("Block " + i + ":\n");
                            // First half
                            for (int j = 0; j < 8; j++) {
                                output.append(String.format("%02x ", data[i * 16 + j]));
                            }
                            output.append(" ");
                            for (int j = 0; j < 8; j++) {
                                output.append(data[i * 16 + j] >= 40 && data[i * 16 + j] <= 176 ? new String(new byte[] { data[i * 16 + j] }, "UTF-8") : ".");
                                output.append(j < 8 - 1 ? " " : "\n");
                            }

                            // Second half
                            for (int j = 0; j < 8; j++) {
                                output.append(String.format("%02x ", data[i * 16 + 8 + j]));
                            }
                            output.append(" ");
                            for (int j = 0; j < 8; j++) {
                                output.append(data[i * 16 + 8 + j] >= 40 && data[i * 16 + 8 + j] <= 176 ? new String(new byte[] { data[i * 16 + 8 + j] }, "UTF-8") : ".");
                                output.append(j < 8 - 1 ? " " : "\n");
                            }
                        }

                        outputText.setText(output.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, exception -> {
                    outputText.setText(getResources().getString(R.string.main_reading_error));
                }).read();
            } else {
                // Print general tag stuff
                output.append("Not Mifare Classic: " + String.join(",", Arrays.asList(tag.getTechList())));

                outputText.setText(output.toString());
            }
        }
    }
}
