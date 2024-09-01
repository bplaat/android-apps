/*
 * Copyright (c) 2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.views;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import nl.plaatsoft.bible.R;
import nl.plaatsoft.bible.Utils;
import nl.plaatsoft.bible.models.Chapter;

public class ChapterView extends ScrollView {
    private LinearLayout root;
    private Typeface typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL);

    public ChapterView(Context context, AttributeSet attrs) {
        super(context, attrs);

        root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        var density = getResources().getDisplayMetrics().density;
        root.setPadding((int) (16 * density), (int) (8 * density), (int) (16 * density), (int) (8 * density));
        addView(root);
    }

    public void setTypeface(Typeface typeface) {
        this.typeface = typeface;
    }

    public void openChapter(Chapter chapter) {
        root.removeAllViews();
        var ssb = new SpannableStringBuilder();
        for (var verse : chapter.verses()) {
            if (verse.isSubtitle()) {
                addVerseBlock(new SpannableString(verse.text()), true);
                continue;
            }

            if (ssb.length() > 0)
                ssb.append(" ");
            var verseNumberSpannable = new SpannableString(verse.number());
            verseNumberSpannable.setSpan(
                    new ForegroundColorSpan(Utils.contextGetColor(getContext(), R.color.secondary_text_color)), 0,
                    verse.number().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            verseNumberSpannable.setSpan(new RelativeSizeSpan(0.75f), 0, verse.number().length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.append(verseNumberSpannable);
            ssb.append(" ");
            ssb.append(verse.text());

            if (verse.isLast()) {
                addVerseBlock(ssb, false);
                ssb = new SpannableStringBuilder();
            }
        }
    }

    private void addVerseBlock(Spannable spannable, boolean isSubtitle) {
        var verseBlock = new TextView(getContext());
        verseBlock.setTextSize(18);
        verseBlock.setTypeface(isSubtitle ? Typeface.create(typeface, Typeface.BOLD) : typeface);
        if (!isSubtitle)
            verseBlock.setLineSpacing(0, 1.2f);
        var layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        var density = getResources().getDisplayMetrics().density;
        layoutParams.setMargins(0, (int) (8 * density), 0, (int) (8 * density));
        verseBlock.setLayoutParams(layoutParams);
        verseBlock.setText(spannable);
        verseBlock.setTextIsSelectable(true);
        root.addView(verseBlock);
    }
}
