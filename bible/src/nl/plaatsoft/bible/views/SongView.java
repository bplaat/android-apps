/*
 * Copyright (c) 2024-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import javax.annotation.Nullable;

import nl.plaatsoft.bible.models.SongWithText;
import nl.plaatsoft.bible.R;

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

    public void openSong(SongWithText song, int scrollY) {
        scrollTo(0, 0);
        root.removeAllViews();

        // Add song content
        var title = new TextView(getContext(), null, 0, R.style.SongViewTitle);
        title.setTypeface(typeface, Typeface.BOLD);
        title.setText(song.number() + ". " + song.title());
        root.addView(title);

        var text = new TextView(getContext(), null, 0, R.style.SongViewText);
        text.setTypeface(typeface);
        text.setText(song.text());
        root.addView(text);

        var copyright = new TextView(getContext(), null, 0, R.style.SongViewCopyright);
        copyright.setTypeface(typeface);
        copyright.setText(song.copyright());
        root.addView(copyright);

        // Add buttons container
        addButtonsContainer();

        // Restore scroll position
        if (scrollY > 0)
            post(() -> scrollTo(0, scrollY));
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
