package com.yuvalshavit.todone.data;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;

import javafx.util.Pair;

public class Tagger {
  private static final Pattern tagPattern = Pattern.compile("#([0-9A-Za-z_-]+)");

  public static Set<String> tagsFor(String text) {
    Matcher matcher = tagPattern.matcher(text);
    Set<String> tags = new HashSet<>();
    while (matcher.find()) {
      tags.add(matcher.group(1));
    }
    return tags;
  }

  public static Pair<String,Set<String>> escapeAndTag(String text) {
    // Note: Matcher#appendReplacement won't work because it doesn't let us plug into the non-matching parts, which we need so that we can escape them.
    StringEscapeUtils.Builder sb = StringEscapeUtils.builder(StringEscapeUtils.ESCAPE_HTML4);
    Set<String> tags = new HashSet<>();
    Matcher matcher = tagPattern.matcher(text);
    int prevStart = 0;
    while (matcher.find()) {
      int currentStart = matcher.start();
      sb.escape(text.substring(prevStart, currentStart));
      sb.append("<span class=\"tag\">");
      sb.escape(matcher.group());
      sb.append("</span>");
      tags.add(matcher.group(1));
      prevStart = matcher.end();
    }
    // append the ending
    if (prevStart < text.length()) {
      sb.append(text.substring(prevStart));
    }
    return new Pair<>(sb.toString(), tags);
  }
}
