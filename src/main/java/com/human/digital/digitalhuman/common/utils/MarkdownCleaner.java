package com.human.digital.digitalhuman.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @USER taoHouChao
 * @DATE 17:36 2025/6/22
 */
public class MarkdownCleaner {

    public static String cleanMarkdown(String text) {
        List<String> result = new ArrayList<>();
        String regex = "[\\u4e00-\\u9fa5\\u3000-\\u303F\\uFF00-\\uFFEF]+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return String.join("", result);
    }
}
