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
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import nl.plaatsoft.bible.models.Chapter;
import nl.plaatsoft.bible.R;
import nl.plaatsoft.bible.Utils;

public class ChapterView extends ScrollView implements View.OnTouchListener {
    public static interface OnSwipeLeftListener {
        void onSwipeLeft();
    }

    public static interface OnSwipeRightListener {
        void onSwipeRight();
    }

    private static class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        private final ChapterView view;

        public SwipeGestureDetector(ChapterView view) {
            this.view = view;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1 == null || e2 == null)
                return false;
            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();
            if (Math.abs(diffX) > Math.abs(diffY) && Math.abs(diffX) > SWIPE_THRESHOLD
                    && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX < 0) {
                    view.onSwipeLeftListener.onSwipeLeft();
                } else {
                    view.onSwipeRightListener.onSwipeRight();
                }
                return true;
            }
            return false;
        }
    }

    private Handler handler = new Handler(Looper.getMainLooper());
    private Typeface typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL);
    private final LinearLayout root;
    private final GestureDetector gestureDetector;
    private OnSwipeLeftListener onSwipeLeftListener;
    private OnSwipeRightListener onSwipeRightListener;

    public ChapterView(Context context, AttributeSet attrs) {
        super(context, attrs);

        root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        var density = getResources().getDisplayMetrics().density;
        root.setPadding((int) (16 * density), (int) (8 * density), (int) (16 * density), (int) (8 * density));
        addView(root);

        setOnTouchListener(this);
        gestureDetector = new GestureDetector(context, new SwipeGestureDetector(this));
    }

    public void setTypeface(Typeface typeface) {
        this.typeface = typeface;
    }

    public void setOnSwipeLeftListener(OnSwipeLeftListener onSwipeLeftListener) {
        this.onSwipeLeftListener = onSwipeLeftListener;
    }

    public void setOnSwipeRightListener(OnSwipeRightListener onSwipeRightListener) {
        this.onSwipeRightListener = onSwipeRightListener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    public void openChapter(Chapter chapter, int highlightVerseId) {
        root.removeAllViews();
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
    }

    private void addVerseBlock(Spannable spannable, boolean isSubtitle, boolean scrollToVerse) {
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

        if (scrollToVerse)
            handler.post(() -> scrollTo(0, verseBlock.getTop() - (int) (16 * density)));
    }
}
