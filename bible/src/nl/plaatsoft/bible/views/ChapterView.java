/*
 * Copyright (c) 2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.views;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import nl.plaatsoft.bible.models.Chapter;
import nl.plaatsoft.bible.R;
import nl.plaatsoft.bible.Utils;

public class ChapterView extends ScrollView {
    public static interface OnPreviousListener {
        void onPrevious();
    }

    public static interface OnNextListener {
        void onNext();
    }

    private Handler handler = new Handler(Looper.getMainLooper());
    private Typeface typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL);
    private final LinearLayout root;
    private OnPreviousListener onPreviousListener;
    private OnNextListener onNextListener;

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

    public void setOnPreviousListener(OnPreviousListener onPreviousListener) {
        this.onPreviousListener = onPreviousListener;
    }

    public void setOnNextListener(OnNextListener onNextListener) {
        this.onNextListener = onNextListener;
    }

    public void openChapter(Chapter chapter, int highlightVerseId) {
        root.removeAllViews();

        // Create verse text blocks
        var ssb = new SpannableStringBuilder();
        var scrollToVerse = false;
        for (var verse : chapter.verses()) {
            if (verse.isSubtitle()) {
                addVerseBlock(new SpannableString(verse.text()), true, scrollToVerse);
                scrollToVerse = false;
                continue;
            }

            if (ssb.length() > 0)
                ssb.append(" ");

            var verseSpannable = new SpannableString(verse.number() + " " + verse.text());
            verseSpannable.setSpan(
                    new ForegroundColorSpan(Utils.contextGetColor(getContext(), R.color.secondary_text_color)), 0,
                    verse.number().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            verseSpannable.setSpan(new RelativeSizeSpan(0.75f), 0, verse.number().length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (highlightVerseId != -1 && verse.id() == highlightVerseId) {
                scrollToVerse = true;
                verseSpannable.setSpan(
                        new BackgroundColorSpan(Utils.contextGetColor(getContext(), R.color.highlight_text_color)),
                        0, verseSpannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            ssb.append(verseSpannable);

            if (verse.isLast()) {
                addVerseBlock(ssb, false, scrollToVerse);
                scrollToVerse = false;
                ssb = new SpannableStringBuilder();
            }
        }

        // Create next and privous buttons
        var selectableItemBackgroundBorderless = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless,
                selectableItemBackgroundBorderless, true);
        var density = getResources().getDisplayMetrics().density;

        var buttonsContainer = new LinearLayout(getContext());
        var layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, (int) (8 * density), 0, (int) (8 * density));
        buttonsContainer.setLayoutParams(layoutParams);
        root.addView(buttonsContainer);

        var previousButton = new ImageButton(getContext());
        previousButton.setImageResource(R.drawable.ic_arrow_left);
        previousButton.setBackgroundResource(selectableItemBackgroundBorderless.resourceId);
        previousButton.setOnClickListener(v -> {
            if (onPreviousListener != null)
                onPreviousListener.onPrevious();
        });
        buttonsContainer.addView(previousButton);

        var flex = new View(getContext());
        flex.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 1));
        buttonsContainer.addView(flex);

        var nextButton = new ImageButton(getContext());
        nextButton.setImageResource(R.drawable.ic_arrow_right);
        nextButton.setBackgroundResource(selectableItemBackgroundBorderless.resourceId);
        nextButton.setOnClickListener(v -> {
            if (onNextListener != null)
                onNextListener.onNext();
        });
        buttonsContainer.addView(nextButton);
    }

    private void addVerseBlock(Spannable spannable, boolean isSubtitle, boolean scrollToVerse) {
        var density = getResources().getDisplayMetrics().density;

        var verseBlock = new TextView(getContext());
        verseBlock.setTextSize(18);
        verseBlock.setTypeface(isSubtitle ? Typeface.create(typeface, Typeface.BOLD) : typeface);
        if (!isSubtitle)
            verseBlock.setLineSpacing(0, 1.2f);
        var layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, (int) (8 * density), 0, (int) (8 * density));
        verseBlock.setLayoutParams(layoutParams);
        verseBlock.setText(spannable);
        verseBlock.setTextIsSelectable(true);
        root.addView(verseBlock);

        if (scrollToVerse)
            handler.post(() -> scrollTo(0, verseBlock.getTop() - (int) (16 * density)));
    }
}
