/*
 * Copyright (c) 2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.views;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import javax.annotation.ParametersAreNonnullByDefault;

import nl.plaatsoft.bible.models.Book;
import nl.plaatsoft.bible.models.Testament;
import nl.plaatsoft.bible.R;

@ParametersAreNonnullByDefault
public class BooksDialogBuilder extends AlertDialog.Builder {
    public static interface OnResultListener {
        void onResult(Book book);
    }

    private Handler handler = new Handler(Looper.getMainLooper());

    @SuppressWarnings("this-escape")
    public BooksDialogBuilder(Context context, ArrayList<Testament> testaments, String currentBookKey,
            OnResultListener onResultListener) {
        super(context);

        setTitle(R.string.main_book_alert_title_label);
        var root = new ScrollView(context);
        setView(root);

        var list = new LinearLayout(context, null, 0, R.style.BooksDialog);
        root.addView(list);

        for (var testament : testaments) {
            var testamentSubtitle = new TextView(context, null, 0, R.style.BooksDialogSubtitle);
            testamentSubtitle.setText(testament.name());
            list.addView(testamentSubtitle);

            var booksFlowLayout = new FlowLayout(context, null, 0, R.style.BooksDialogRow);
            list.addView(booksFlowLayout);

            for (var book : testament.books()) {
                var bookButton = new TextView(context, null, 0,
                        book.key().equals(currentBookKey) ? R.style.BooksDialogButtonActive
                                : R.style.BooksDialogButton);
                bookButton.setText(book.name());
                bookButton.setOnClickListener(view -> onResultListener.onResult(book));
                booksFlowLayout.addView(bookButton);

                if (book.key().equals(currentBookKey))
                    handler.post(() -> root.scrollTo(0, bookButton.getTop()));
            }
        }
    }
}
