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
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;

import nl.plaatsoft.bible.models.Book;
import nl.plaatsoft.bible.models.Testament;
import nl.plaatsoft.bible.R;
import nl.plaatsoft.bible.Utils;

public class BooksDialogBuilder extends AlertDialog.Builder {
    public static interface OnResultListener {
        void onResult(Book book);
    }

    public BooksDialogBuilder(Context context, ArrayList<Testament> testaments, String currentBookKey,
            OnResultListener onResultListener) {
        super(context);

        var selectableItemBackground = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, selectableItemBackground, true);
        var density = context.getResources().getDisplayMetrics().density;

        setTitle(R.string.main_book_alert_title_label);
        var root = new ScrollView(context);
        setView(root);

        var list = new LinearLayout(context);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(0, (int) (16 * density), 0, (int) (16 * density));
        root.addView(list);

        for (var testament : testaments) {
            var testamentSubtitle = new TextView(context);
            testamentSubtitle.setText(testament.name());
            testamentSubtitle.setTextSize(16);
            testamentSubtitle.setTypeface(Typeface.create("sans-serif-medium",
                    Typeface.NORMAL));
            testamentSubtitle.setTextColor(Utils.contextGetColor(context,
                    R.color.secondary_text_color));
            testamentSubtitle.setPadding((int) (24 * density), (int) (16 * density), (int) (24 * density),
                    (int) (16 * density));
            list.addView(testamentSubtitle);

            var booksFlowLayout = new FlowLayout(context);
            booksFlowLayout.setPadding((int) (16 * density), 0, (int) (16 * density),
                    (int) (16 * density));
            list.addView(booksFlowLayout);

            for (var book : testament.books()) {
                var bookButton = new TextView(context);
                bookButton.setText(book.name());
                bookButton.setTextSize(16);
                bookButton.setPadding((int) (8 * density), (int) (8 * density), (int) (8 *
                        density), (int) (8 * density));
                if (book.key().equals(currentBookKey))
                    bookButton.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
                bookButton.setBackgroundResource(selectableItemBackground.resourceId);
                bookButton.setOnClickListener(view -> onResultListener.onResult(book));
                booksFlowLayout.addView(bookButton);
            }
        }
    }
}
