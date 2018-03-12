package com.yuvalshavit.todone.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.function.LongFunction;
import java.util.function.ToLongFunction;

public interface Aggregator {

  Aggregator byDay = new AggregatorImpl(
    "days",
    LocalDate::toEpochDay,
    string -> LocalDate.from(DateTimeFormatter.ISO_DATE.parse(string)).toEpochDay(),
    day -> DateTimeFormatter.ISO_DATE.format(LocalDate.ofEpochDay(day)),
    "Yesterday"
  );

  Aggregator byWeek = new AggregatorImpl(
    "weeks",
    date -> ChronoUnit.WEEKS.between(AggregatorImpl.mondayBeforeEpoch, date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))),
    string -> ChronoUnit.WEEKS.between(AggregatorImpl.mondayBeforeEpoch, LocalDate.from(DateTimeFormatter.ISO_DATE.parse(string))),
    day -> DateTimeFormatter.ISO_DATE.format(AggregatorImpl.mondayBeforeEpoch.plusWeeks(day)),
    "Last week"
  );

  ToLongFunction<String> toDays();
  LongFunction<String> fromDays();
  long toLong(LocalDate date);
  String oneUnitAgo();
  String unitNamePlural();
}
