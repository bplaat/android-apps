/*
 * Copyright (c) 2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.views;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import javax.annotation.ParametersAreNonnullByDefault;

import nl.plaatsoft.bible.models.Chapter;
import nl.plaatsoft.bible.R;

@ParametersAreNonnullByDefault
public class ChaptersDialogBuilder extends AlertDialog.Builder {
    public static interface OnResultListener {
        void onResult(Chapter chapter);
    }

    public ChaptersDialogBuilder(Context context, ArrayList<Chapter> chapters, int currentChapterNumber,
            OnResultListener onResultListener) {
        super(context);

        setTitle(R.string.main_chapter_alert_title_label);
        var root = new ScrollView(context);
        setView(root);

        var grid = new FixedGridLayout(context, null, 0, R.style.IndexDialog);
        grid.setColumnCount(6);
        root.addView(grid);

        for (var chapter : chapters) {
            var chapterButton = new TextView(context, null, 0,
                    chapter.number() == currentChapterNumber ? R.style.IndexDialogButtonActive
                            : R.style.IndexDialogButton);
            chapterButton.setText(String.valueOf(chapter.number()));
            chapterButton.setOnClickListener(view -> onResultListener.onResult(chapter));
            grid.addView(chapterButton);
        }
    }
}
