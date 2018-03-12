package com.yuvalshavit.todone.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.function.LongFunction;
import java.util.function.ToLongFunction;

class AggregatorImpl implements Aggregator {

  public static LocalDate mondayBeforeEpoch = LocalDate.ofEpochDay(0).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

  private final ToLongFunction<String> toDays;
  private final LongFunction<String> fromDays;
  private final ToLongFunction<LocalDate> datePruner;
  private final String oneUnitAgo;
  private final String unitNamePlural;
  private final String thisUnit;

  public AggregatorImpl(
    String unitNamePlural,
    ToLongFunction<LocalDate> datePruner,
    ToLongFunction<String> toDays,
    LongFunction<String> fromDays,
    String thisUnit,
    String oneUnitAgo)
  {
    this.toDays = toDays;
    this.fromDays = fromDays;
    this.datePruner = datePruner;
    this.thisUnit = thisUnit;
    this.oneUnitAgo = oneUnitAgo;
    this.unitNamePlural = unitNamePlural;
  }

  public ToLongFunction<String> toDays() {
    return toDays;
  }

  public LongFunction<String> fromDays() {
    return fromDays;
  }

  public long toLong(LocalDate date) {
    return datePruner.applyAsLong(date);
  }

  @Override
  public String thisUnit() {
    return thisUnit;
  }

  public String oneUnitAgo() {
    return oneUnitAgo;
  }

  public String unitNamePlural() {
    return unitNamePlural;
  }
}
