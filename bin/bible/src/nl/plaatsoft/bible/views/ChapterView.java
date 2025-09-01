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

import nl.plaatsoft.android.compat.ContextCompat;
import nl.plaatsoft.android.compat.TypefaceSpanCompat;
import nl.plaatsoft.bible.R;
import nl.plaatsoft.bible.models.ChapterWithVerses;

import org.jspecify.annotations.Nullable;

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

    public void openChapter(ChapterWithVerses chapter, int previousScrollY, int highlightVerseId) {
        scrollTo(0, 0);
        root.removeAllViews();

        // Add verses
        var spannable = new SpannableStringBuilder();
        var addSpace = false;
        var scrollToOffset = new int[] {-1};
        for (var i = 0; i < chapter.verses().size(); i++) {
            var verse = chapter.verses().get(i);

            // Add subtitle
            if (verse.isSubtitle()) {
                var subtitleSpannable = new SpannableString(verse.text() + "\n");
                subtitleSpannable.setSpan(new TypefaceSpanCompat(Typeface.create(typeface, Typeface.BOLD)), 0,
                    subtitleSpannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.append(subtitleSpannable);

                var linebreakSpannable = new SpannableString("\n");
                linebreakSpannable.setSpan(
                    new RelativeSizeSpan(0.5f), 0, linebreakSpannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.append(linebreakSpannable);
                addSpace = false;
                continue;
            }

            // Add normal verse
            if (addSpace)
                spannable.append(" ");

            var verseSpannable = new SpannableString(verse.number() + " " + verse.text());
            verseSpannable.setSpan(
                new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.secondary_text_color)), 0,
                verse.number().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            verseSpannable.setSpan(
                new RelativeSizeSpan(0.75f), 0, verse.number().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (verse.id() == highlightVerseId) {
                scrollToOffset[0] = spannable.length();
                verseSpannable.setSpan(
                    new BackgroundColorSpan(ContextCompat.getColor(getContext(), R.color.highlight_text_color)), 0,
                    verseSpannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            spannable.append(verseSpannable);
            addSpace = true;

            // Add normal verse endings
            if (verse.isLast() && i != chapter.verses().size() - 1) {
                spannable.append("\n");

                var linebreakSpannable = new SpannableString("\n");
                linebreakSpannable.setSpan(
                    new RelativeSizeSpan(0.5f), 0, linebreakSpannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.append(linebreakSpannable);
                addSpace = false;
            }
        }

        // Add verses text view
        var versesView = new TextView(getContext(), null, 0, R.style.ChapterViewVerses);
        versesView.setTypeface(typeface);
        versesView.setText(spannable);
        root.addView(versesView);

        // Add buttons container
        addButtonsContainer();

        // Restore previous scroll position
        if (previousScrollY > 0 && highlightVerseId == -1)
            handler.post(() -> scrollTo(0, previousScrollY));

        // Or scroll to offset
        if (scrollToOffset[0] != -1) {
            handler.post(() -> {
                var layout = versesView.getLayout();
                var line = layout.getLineForOffset(scrollToOffset[0]);
                scrollTo(0, layout.getLineTop(line));
            });
        }
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
}
