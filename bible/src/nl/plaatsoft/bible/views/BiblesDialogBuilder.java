/*
 * Copyright (c) 2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.views;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;

import nl.plaatsoft.bible.models.Bible;
import nl.plaatsoft.bible.R;
import nl.plaatsoft.bible.Utils;

public class BiblesDialogBuilder extends AlertDialog.Builder {
    public static interface OnResultListener {
        void onResult(Bible bible);
    }

    public BiblesDialogBuilder(Context context, ArrayList<Bible> bibles, Bible currentBible,
            OnResultListener onResultListener) {
        super(context);

        var selectableItemBackground = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, selectableItemBackground, true);
        var density = context.getResources().getDisplayMetrics().density;

        setTitle(R.string.main_bible_alert_title_label);
        var root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(0, (int) (16 * density), 0, (int) (16 * density));
        setView(root);

        var previousLanguage = "";
        for (var bible : bibles) {
            if (!previousLanguage.equals(bible.language())) {
                previousLanguage = bible.language();
                var languageSubtitle = new TextView(context);
                if (bible.language().equals("en"))
                    languageSubtitle.setText(R.string.settings_language_english);
                if (bible.language().equals("nl"))
                    languageSubtitle.setText(R.string.settings_language_dutch);
                languageSubtitle.setTextSize(16);
                languageSubtitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                languageSubtitle.setTextColor(Utils.contextGetColor(context, R.color.secondary_text_color));
                languageSubtitle.setPadding((int) (24 * density), (int) (16 * density), (int) (24 * density),
                        (int) (16 * density));
                root.addView(languageSubtitle);
            }

            var bibleButton = new TextView(context);
            bibleButton.setText(bible.name());
            bibleButton.setTextSize(16);
            bibleButton.setPadding((int) (24 * density), (int) (16 * density), (int) (24 * density),
                    (int) (16 * density));
            if (bible.path().equals(currentBible.path()))
                bibleButton.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
            bibleButton.setBackgroundResource(selectableItemBackground.resourceId);
            bibleButton.setOnClickListener(view -> onResultListener.onResult(bible));
            root.addView(bibleButton);
        }
    }
}
