package com.yuvalshavit.todone.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.LongFunction;
import java.util.function.ToLongFunction;

public interface Aggregator {

  Aggregator byDay = new AggregatorImpl(
    "days",
    LocalDate::toEpochDay,
    string -> LocalDate.from(DateTimeFormatter.ISO_DATE.parse(string)).toEpochDay(),
    day -> DateTimeFormatter.ISO_DATE.format(LocalDate.ofEpochDay(day)),
    "Yesterday");

  ToLongFunction<String> toDays();
  LongFunction<String> fromDays();
  long toLong(LocalDate date);
  String oneUnitAgo();
  String unitNamePlural();
}
