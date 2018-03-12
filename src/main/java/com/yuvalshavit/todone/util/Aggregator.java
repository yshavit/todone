package com.yuvalshavit.todone.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.LongFunction;
import java.util.function.ToLongFunction;

import javafx.util.StringConverter;

public class Aggregator {

  public static Aggregator instance = new Aggregator();

  private static final StringConverter<Long> EPOCH_DAY_FORMATTER = new StringConverter<Long>() {
    private final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_DATE;

    @Override
    public String toString(Long object) {
      return ISO_DATE.format(LocalDate.ofEpochDay(object));
    }

    @Override
    public Long fromString(String string) {
      return LocalDate.from(ISO_DATE.parse(string)).toEpochDay();
    }
  };

  public ToLongFunction<String> toDays() {
    return EPOCH_DAY_FORMATTER::fromString;
  }

  public LongFunction<String> fromDays() {
    return EPOCH_DAY_FORMATTER::toString;
  }

  public long toLong(LocalDate date) {
    return date.toEpochDay();
  }

  public String oneUnitAgo() {
    return "Yesterday";
  }

  public String unitNamePlural() {
    return "days";
  }
}
