/*
 * Copyright (c) 2024-2025 Bastiaan van der Plaat
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
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import javax.annotation.Nullable;

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
    private final LinearLayout root;
    private Typeface typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL);
    private @Nullable OnPreviousListener onPreviousListener;
    private @Nullable OnNextListener onNextListener;

    @SuppressWarnings("this-escape")
    public ChapterView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        root = new LinearLayout(context, null, 0, R.style.ChapterView);
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

    public void openChapter(Chapter chapter, int scrollY, int highlightVerseId) {
        scrollTo(0, 0);
        root.removeAllViews();

        // Add verse text blocks
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

        // Add buttons container
        addButtonsContainer();

        // Restore scroll position
        if (scrollY > 0 && highlightVerseId == -1)
            handler.post(() -> scrollTo(0, scrollY));
    }

    private void addButtonsContainer() {
        var buttonsContainer = new LinearLayout(getContext(), null, 0, R.style.ChapterViewButtons);
        root.addView(buttonsContainer);

        var previousButton = new ImageButton(getContext(), null, 0, R.style.ChapterViewButtonsButton);
        previousButton.setImageResource(R.drawable.ic_arrow_left);
        previousButton.setOnClickListener(v -> {
            if (onPreviousListener != null)
                onPreviousListener.onPrevious();
        });
        buttonsContainer.addView(previousButton);

        var flex = new View(getContext());
        flex.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 1));
        buttonsContainer.addView(flex);

        var nextButton = new ImageButton(getContext(), null, 0, R.style.ChapterViewButtonsButton);
        nextButton.setImageResource(R.drawable.ic_arrow_right);
        nextButton.setOnClickListener(v -> {
            if (onNextListener != null)
                onNextListener.onNext();
        });
        buttonsContainer.addView(nextButton);
    }

    private void addVerseBlock(Spannable spannable, boolean isSubtitle, boolean scrollToVerse) {
        var verseView = new TextView(getContext(), null, 0,
                isSubtitle ? R.style.ChapterViewSubtitle : R.style.ChapterViewVerse);
        verseView.setTypeface(isSubtitle ? Typeface.create(typeface, Typeface.BOLD) : typeface);
        verseView.setText(spannable);
        root.addView(verseView);

        if (scrollToVerse)
            handler.post(() -> scrollTo(0, verseView.getTop() - (root.getPaddingTop() + verseView.getPaddingTop())));
    }
}
