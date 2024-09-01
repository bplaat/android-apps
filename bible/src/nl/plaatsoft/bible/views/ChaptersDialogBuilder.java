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
import android.view.Gravity;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;

import nl.plaatsoft.bible.models.Chapter;
import nl.plaatsoft.bible.R;

public class ChaptersDialogBuilder extends AlertDialog.Builder {
    public static interface OnResultListener {
        void onResult(Chapter chapter);
    }

    public ChaptersDialogBuilder(Context context, ArrayList<Chapter> chapters, Chapter currentChapter,
            OnResultListener onResultListener) {
        super(context);

        var selectableItemBackgroundBorderless = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless,
                selectableItemBackgroundBorderless, true);
        var density = context.getResources().getDisplayMetrics().density;

        setTitle(R.string.main_chapter_alert_title_label);
        var root = new ScrollView(context);
        setView(root);

        var grid = new FixedGridLayout(context, 6);
        grid.setPadding((int) (8 * density), (int) (16 * density), (int) (8 *
                density), (int) (16 * density));
        root.addView(grid);

        for (var chapter : chapters) {
            var chapterButton = new TextView(context);
            chapterButton.setText(String.valueOf(chapter.number()));
            chapterButton.setTextSize(20);
            if (chapter.number() == currentChapter.number())
                chapterButton.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
            chapterButton.setGravity(Gravity.CENTER);
            chapterButton.setBackgroundResource(selectableItemBackgroundBorderless.resourceId);
            chapterButton.setOnClickListener(view -> onResultListener.onResult(chapter));
            grid.addView(chapterButton);
        }
    }
}
