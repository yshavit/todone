package com.yuvalshavit.todone.data;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
}
