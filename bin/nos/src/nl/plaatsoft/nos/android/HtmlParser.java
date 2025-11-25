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
            var text = unescapeHtml(stripTags(matcher.group(2)).trim());
            if (!text.isEmpty()) {
                results.add(new Element(tag, text));
            }
        }
        return results;
    }

    private static String stripTags(String text) {
        return text.replaceAll("<[^>]*>", "").trim();
    }

    private static String unescapeHtml(String text) {
        // Unescape common named entities
        text = text.replace("&lt;", "<")
                   .replace("&gt;", ">")
                   .replace("&amp;", "&")
                   .replace("&quot;", "\"")
                   .replace("&apos;", "'")
                   .replace("&nbsp;", " ")
                   .replace("&copy;", "©")
                   .replace("&reg;", "®")
                   .replace("&euro;", "€")
                   .replace("&cent;", "¢")
                   .replace("&pound;", "£")
                   .replace("&yen;", "¥")
                   .replace("&sect;", "§")
                   .replace("&deg;", "°")
                   .replace("&plusmn;", "±")
                   .replace("&para;", "¶")
                   .replace("&middot;", "·")
                   .replace("&ndash;", "–")
                   .replace("&mdash;", "—")
                   .replace("&lsquo;", "‘")
                   .replace("&rsquo;", "’")
                   .replace("&ldquo;", "“")
                   .replace("&rdquo;", "”")
                   .replace("&bull;", "•")
                   .replace("&hellip;", "…")
                   .replace("&trade;", "™")
                   .replace("&frasl;", "⁄");

        // Unescape decimal numeric entities using Pattern
        var decimalPattern = Pattern.compile("&#(\\d+);");
        var decimalMatcher = decimalPattern.matcher(text);
        var sb = new StringBuffer();
        while (decimalMatcher.find()) {
            int codePoint = Integer.parseInt(decimalMatcher.group(1));
            decimalMatcher.appendReplacement(sb, new String(Character.toChars(codePoint)));
        }
        decimalMatcher.appendTail(sb);
        text = sb.toString();

        // Unescape hexadecimal numeric entities using Pattern
        var hexPattern = Pattern.compile("&#x([0-9a-fA-F]+);");
        var hexMatcher = hexPattern.matcher(text);
        sb = new StringBuffer();
        while (hexMatcher.find()) {
            int codePoint = Integer.parseInt(hexMatcher.group(1), 16);
            hexMatcher.appendReplacement(sb, new String(Character.toChars(codePoint)));
        }
        hexMatcher.appendTail(sb);
        text = sb.toString();

        return text;
    }
}
