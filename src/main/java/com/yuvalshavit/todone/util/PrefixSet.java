package com.yuvalshavit.todone.util;

import java.util.NavigableSet;
import java.util.TreeSet;

public class PrefixSet {
  private final NavigableSet<String> options = new TreeSet<>();
  public void addOption(String option) {
    options.add(option);
  }

  public Iterable<String> optionsWithPrefix(String prefix) {
    return TakeWhile.wrap(options.tailSet(prefix), item -> item.startsWith(prefix));
  }
}
