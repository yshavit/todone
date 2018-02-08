package com.yuvalshavit.todone.data;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Objects;

public class Tagger {
  private static final Pattern tagPattern = Pattern.compile("#([0-9A-Za-z_-]+)");

  public static Set<String> tagsFor(String text) {
    Set<String> tags = new HashSet<>();
    escapeAndTag(text, null, tags::add);
    return tags;
  }

  public static void escapeAndTag(String text, Consumer<String> plainText, Consumer<String> tag) {
    plainText = Objects.firstNonNull(plainText, s -> {});
    tag = Objects.firstNonNull(tag, s -> {});
    Matcher matcher = tagPattern.matcher(text);
    int prevStart = 0;
    while (matcher.find()) {
      int currentStart = matcher.start();
      String prefix = text.substring(prevStart, currentStart);
      if (!prefix.isEmpty()) {
        plainText.accept(prefix);
      }
      tag.accept(matcher.group(1));
      prevStart = matcher.end();
    }
    // append the ending, if there is one that's non-empty
    if (prevStart < text.length() - 1) {
      plainText.accept(text.substring(prevStart));
    }
  }
}
