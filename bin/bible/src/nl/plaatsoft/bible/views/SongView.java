/*
 * Copyright (c) 2024-2025 Bastiaan van der Plaat
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
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import nl.plaatsoft.android.compat.ContextCompat;
import nl.plaatsoft.android.compat.TypefaceSpanCompat;
import nl.plaatsoft.bible.R;
import nl.plaatsoft.bible.models.SongWithText;

import org.jspecify.annotations.Nullable;

public class SongView extends ScrollView {
    public static interface OnPreviousListener {
        void onPrevious();
    }

    public static interface OnNextListener {
        void onNext();
    }

    private final LinearLayout root;
    private Typeface typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL);
    private @Nullable OnPreviousListener onPreviousListener;
    private @Nullable OnNextListener onNextListener;

    @SuppressWarnings("this-escape")
    public SongView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        root = new LinearLayout(context, null, 0, R.style.SongView);
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

    public void openSong(SongWithText song, int previousScrollY) {
        scrollTo(0, 0);
        root.removeAllViews();

        // Add song content
        var spannable = new SpannableStringBuilder();

        var titleSpannable = new SpannableString(song.number() + ". " + song.title() + "\n");
        titleSpannable.setSpan(new TypefaceSpanCompat(Typeface.create(typeface, Typeface.BOLD)),
                0, titleSpannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.append(titleSpannable);

        var linebreakSpannable = new SpannableString("\n");
        linebreakSpannable.setSpan(new RelativeSizeSpan(0.5f), 0, linebreakSpannable.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.append(linebreakSpannable);

        var songText = song.text();
        if (!songText.endsWith("\n"))
            songText += "\n";
        spannable.append(songText);

        linebreakSpannable = new SpannableString("\n");
        linebreakSpannable.setSpan(new RelativeSizeSpan(0.5f), 0, linebreakSpannable.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.append(linebreakSpannable);

        var copyrightSpannable = new SpannableString(song.copyright());
        copyrightSpannable.setSpan(
                new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.secondary_text_color)), 0,
                copyrightSpannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        copyrightSpannable.setSpan(new RelativeSizeSpan(0.85f), 0, copyrightSpannable.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.append(copyrightSpannable);

        var text = new TextView(getContext(), null, 0, R.style.SongViewText);
        text.setTypeface(typeface);
        text.setText(spannable);
        root.addView(text);

        // Add buttons container
        addButtonsContainer();

        // Restore scroll position
        if (previousScrollY > 0)
            post(() -> scrollTo(0, previousScrollY));
    }

    private void addButtonsContainer() {
        var buttonsContainer = new LinearLayout(getContext(), null, 0, R.style.SongViewButtons);
        root.addView(buttonsContainer);

        var previousButton = new ImageButton(getContext(), null, 0, R.style.SongViewButtonsButton);
        previousButton.setImageResource(R.drawable.ic_arrow_left);
        previousButton.setOnClickListener(v -> {
            if (onPreviousListener != null)
                onPreviousListener.onPrevious();
        });
        buttonsContainer.addView(previousButton);

        var flex = new View(getContext());
        flex.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 1));
        buttonsContainer.addView(flex);

        var nextButton = new ImageButton(getContext(), null, 0, R.style.SongViewButtonsButton);
        nextButton.setImageResource(R.drawable.ic_arrow_right);
        nextButton.setOnClickListener(v -> {
            if (onNextListener != null)
                onNextListener.onNext();
        });
        buttonsContainer.addView(nextButton);
    }
}
