/*
 * Copyright (c) 2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.nos.android;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import android.util.Xml;

import nl.plaatsoft.nos.android.models.Article;

import org.xmlpull.v1.XmlPullParser;

public class RssParser {
    public static List<Article> parse(byte[] input) throws Exception {
        var articles = new ArrayList<Article>();
        var parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(new ByteArrayInputStream(input), null);

        String title = "", imageUrl = "", date = "", content = "", externUrl = "";
        var insideItem = false;

        var eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            var tagName = parser.getName();
            switch (eventType) {
                case XmlPullParser.START_TAG -> {
                    if ("item".equals(tagName)) {
                        insideItem = true;
                        title = imageUrl = date = content = "";
                    } else if (insideItem) {
                        switch (tagName) {
                            case "title", "pubDate", "description", "guid" -> {
                                parser.next();
                                if (parser.getEventType() == XmlPullParser.TEXT) {
                                    var text = parser.getText();
                                    switch (tagName) {
                                        case "title" -> title = stripCdata(text);
                                        case "pubDate" -> date = text.trim();
                                        case "description" -> content = stripCdata(text);
                                        case "guid" -> externUrl = text.trim();
                                    }
                                }
                            }
                            case "enclosure" -> imageUrl = parser.getAttributeValue(null, "url");
                        }
                    }
                }
                case XmlPullParser.END_TAG -> {
                    if ("item".equals(tagName) && insideItem) {
                        articles.add(new Article(title, imageUrl, date, content, externUrl));
                        insideItem = false;
                    }
                }
            }
            eventType = parser.next();
        }
        return articles;
    }

    private static String stripCdata(String s) {
        if (s.startsWith("<![CDATA[")) {
            s = s.substring(9, s.length() - 3);
        }
        return s.trim();
    }
}
