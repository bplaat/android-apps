package nl.plaatsoft.bible.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.ScrollView;
import java.util.ArrayList;

import nl.plaatsoft.bible.models.Bible;
import nl.plaatsoft.bible.models.Book;
import nl.plaatsoft.bible.models.Chapter;
import nl.plaatsoft.bible.services.BibleService;
import nl.plaatsoft.bible.Consts;
import nl.plaatsoft.bible.R;

public class MainActivity extends BaseActivity implements PopupMenu.OnMenuItemClickListener {
    private static final int SETTINGS_REQUEST_CODE = 1;

    private TextView bibleButton;
    private TextView bookButton;
    private TextView chapterButton;
    private ScrollView chapterScroll;
    private LinearLayout chapterContents;

    private Handler handler = new Handler(Looper.getMainLooper());
    private int oldFont = -1;
    private int oldLanguage = -1;
    private int oldTheme = -1;
    private BibleService bibleService = BibleService.getInstance();
    private ArrayList<Bible> bibles;
    private Bible openBible;
    private Book openBook;
    private Chapter openChapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bibleButton = findViewById(R.id.main_bible_button);
        bookButton = findViewById(R.id.main_book_button);
        chapterButton = findViewById(R.id.main_chapter_button);
        chapterScroll = findViewById(R.id.main_chapter_scroll);
        chapterContents = findViewById(R.id.main_chapter_contents);

        // Bible button
        bibleButton.setOnClickListener(view -> {
            var bibleNames = new String[bibles.size()];
            var openBibleIndex = -1;
            for (var i = 0; i < bibles.size(); i++) {
                Bible bible = bibles.get(i);
                bibleNames[i] = bible.name() + " (" + bible.language() +")";
                if (openBible.path() == bible.path())
                    openBibleIndex = i;
            }

            new AlertDialog.Builder(this)
                .setTitle(R.string.main_bible_alert_title_label)
                .setSingleChoiceItems(bibleNames, openBibleIndex, (dialog, which) -> {
                    dialog.dismiss();
                    var chosenBible = bibles.get(which);
                    if (chosenBible.path() != openBible.path()) {
                        var settingsEditor = settings.edit();
                        settingsEditor.putString("open_bible", chosenBible.path());
                        settingsEditor.apply();
                        openBible();
                    }
                })
                .setNegativeButton(R.string.main_bible_alert_cancel_button, null)
                .show();
        });

        // Book button
        bookButton.setOnClickListener(view -> {
            int booksSize = 0;
            for (var testament : openBible.testaments())
                booksSize += testament.books().size();
            var books = new Book[booksSize];
            var bookNames = new String[booksSize];
            var openBookIndex = -1;
            int i = 0;
            for (var testament : openBible.testaments()) {
                for (var book : testament.books()) {
                    if (book.id() == openBook.id())
                        openBookIndex = i;
                    books[i] = book;
                    bookNames[i++] = book.name();
                }
            }

            new AlertDialog.Builder(this)
                .setTitle(R.string.main_book_alert_title_label)
                .setSingleChoiceItems(bookNames, openBookIndex, (dialog, which) -> {
                    dialog.dismiss();
                    var chosenBook = books[which];
                    if (chosenBook.id() != openBook.id()) {
                        var settingsEditor = settings.edit();
                        settingsEditor.putString("open_book", chosenBook.key());
                        settingsEditor.putInt("open_chapter", 1);
                        settingsEditor.apply();
                        openChapter(true);
                    }
                })
                .setNegativeButton(R.string.main_book_alert_cancel_button, null)
                .show();
        });

        // Chapter button
        chapterButton.setOnClickListener(view -> {
            var chapterNames = new String[openBook.chapters().size()];
            for (var i = 0; i < openBook.chapters().size(); i++)
                chapterNames[i] = String.valueOf(openBook.chapters().get(i).number());

            new AlertDialog.Builder(this)
                .setTitle(R.string.main_chapter_alert_title_label)
                .setSingleChoiceItems(chapterNames, openChapter.number() - 1, (dialog, which) -> {
                    dialog.dismiss();
                    var chosenChapter = openBook.chapters().get(which);
                    if (chosenChapter.id() != openChapter.id()) {
                        var settingsEditor = settings.edit();
                        settingsEditor.putInt("open_chapter", chosenChapter.number());
                        settingsEditor.apply();
                        openChapter(true);
                    }
                })
                .setNegativeButton(R.string.main_chapter_alert_cancel_button, null)
                .show();
        });

        // Options menu button
        findViewById(R.id.main_options_menu_button).setOnClickListener(view -> {
            var optionsMenu = new PopupMenu(this, view, Gravity.TOP | Gravity.RIGHT);
            optionsMenu.getMenuInflater().inflate(R.menu.options, optionsMenu.getMenu());
            optionsMenu.setOnMenuItemClickListener(this);
            optionsMenu.show();
        });

        // Install bibles from assets and open last opened bible
        bibleService.installBiblesFromAssets(this);
        bibles = bibleService.getInstalledBibles(this);
        openBible();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.menu_options_settings) {
            oldFont = settings.getInt("font", Consts.Settings.FONT_DEFAULT);
            oldLanguage = settings.getInt("language", Consts.Settings.LANGUAGE_DEFAULT);
            oldTheme = settings.getInt("theme", Consts.Settings.THEME_DEFAULT);
            startActivityForResult(new Intent(this, SettingsActivity.class), SETTINGS_REQUEST_CODE);
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // When settings activity is closed check for restarts
        if (requestCode == SETTINGS_REQUEST_CODE) {
            if (oldFont != -1 && oldLanguage != -1 && oldTheme != -1) {
                if (oldFont != settings.getInt("font", Consts.Settings.FONT_DEFAULT))
                    openChapter(false);

                if (
                    oldLanguage != settings.getInt("language", Consts.Settings.LANGUAGE_DEFAULT) ||
                    oldTheme != settings.getInt("theme", Consts.Settings.THEME_DEFAULT)
                ) {
                    handler.post(() -> recreate());
                }
            }
        }
    }

    private void openBible() {
        String defaultBiblePath = Consts.Settings.BIBLE_DEFAULT.get("en");
        var locales = getResources().getConfiguration().getLocales();
        for (var i = 0; i < locales.size(); i++) {
            var language = locales.get(i).getLanguage();
            if (Consts.Settings.BIBLE_DEFAULT.containsKey(language)) {
                defaultBiblePath = Consts.Settings.BIBLE_DEFAULT.get(language);
                break;
            }
        }

        openBible = bibleService.readBible(this, settings.getString("open_bible", defaultBiblePath), true);
        bibleButton.setText(openBible.name());
        openChapter(true);
    }

    private void openChapter(boolean scrollToTop) {
        openChapter = bibleService.readChapter(this, openBible.path(), settings.getString("open_book", "GEN"), settings.getInt("open_chapter", 1));
        chapterButton.setText(String.valueOf(openChapter.number()));

        for (var testament : openBible.testaments()) {
            for (var book : testament.books()) {
                if (book.id() == openChapter.book().id()) {
                    openBook = book;
                    break;
                }
            }
        }
        bookButton.setText(openBook.name());

        // Create verse views
        var typefaceBold = Typeface.create(Typeface.SERIF, Typeface.BOLD);
        var typeface =Typeface.create(Typeface.SERIF, Typeface.NORMAL);
        if (settings.getInt("font", Consts.Settings.FONT_SERIF) == Consts.Settings.FONT_SANS_SERIF) {
            typefaceBold = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
        }
        if (settings.getInt("font", Consts.Settings.FONT_SERIF) == Consts.Settings.FONT_MONOSPACE) {
            typefaceBold = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
        }

        if (scrollToTop)
            chapterScroll.scrollTo(0, 0);
        chapterContents.removeAllViews();
        var sb = new StringBuilder();
        for (var verse : openChapter.verses()) {
            if (verse.isSubtitle()) {
                addVerseBlock(verse.text(), typefaceBold);
                continue;
            }
            if (verse.isNewParagraph() && sb.length() > 0) {
                addVerseBlock(sb.toString(), typeface);
                sb.setLength(0);
            }
            if (sb.length() > 0)
                sb.append(" ");
            sb.append(verse.number() + ". " + verse.text());
        }
        if (sb.length() > 0)
            addVerseBlock(sb.toString(), typeface);
    }

    private void addVerseBlock(String text, Typeface typeface) {
        var verseBlock = new TextView(this);
        verseBlock.setTypeface(typeface);
        verseBlock.setTextSize(18);
        var layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        var scale = getResources().getDisplayMetrics().density;
        layoutParams.setMargins(0, (int)(8 * scale), 0, (int)(8 * scale));
        verseBlock.setLayoutParams(layoutParams);
        verseBlock.setText(text);
        chapterContents.addView(verseBlock);
    }
}
