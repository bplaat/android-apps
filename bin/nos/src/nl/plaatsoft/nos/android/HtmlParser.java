/*
 * Copyright (c) 2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.nos.android;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class HtmlParser {
    public static record Element(String tag, String text) {}

    private HtmlParser() {}

    public static List<Element> parse(String html) {
        var results = new ArrayList<Element>();
        var pattern = Pattern.compile("<(\\w+)[^>]*>(.*?)</\\1>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        var matcher = pattern.matcher(html);
        while (matcher.find()) {
            var tag = matcher.group(1).toLowerCase();
            var text = stripTags(matcher.group(2)).trim();
            if (!text.isEmpty()) {
                results.add(new Element(tag, text));
            }
        }
        return results;
    }

    private static String stripTags(String text) {
        return text.replaceAll("<[^>]*>", "").trim();
    }
}
