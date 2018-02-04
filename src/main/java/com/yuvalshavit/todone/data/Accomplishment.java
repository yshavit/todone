package com.yuvalshavit.todone.data;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import lombok.Data;
import lombok.NonNull;

@Data
public class Accomplishment implements Comparable<Accomplishment> {

  private final long timestamp;
  @NonNull
  private final String text;

  @Override
  public String toString() {
    String timestampStr = DateTimeFormatter.ISO_DATE_TIME
      .withLocale(Locale.US)
      .withZone(ZoneOffset.UTC)
      .format(Instant.ofEpochMilli(timestamp));
    return timestampStr + ": " + text;
  }

  @Override
  public int compareTo(Accomplishment o) {
    // compare timestamp in reverse, so that the most recent ones come first
    int cmp = Long.compare(o.timestamp, timestamp);
    return cmp == 0
      ? text.compareTo(o.text)
      : cmp;
  }
}
