package com.yuvalshavit.todone.util;

import java.time.LocalDate;
import java.util.function.LongFunction;
import java.util.function.ToLongFunction;

class AggregatorImpl implements Aggregator {

  private final ToLongFunction<String> toDays;
  private final LongFunction<String> fromDays;
  private final ToLongFunction<LocalDate> datePruner;
  private final String oneUnitAgo;
  private final String unitNamePlural;

  public AggregatorImpl(
    String unitNamePlural,
    ToLongFunction<LocalDate> datePruner,
    ToLongFunction<String> toDays,
    LongFunction<String> fromDays,
    String oneUnitAgo)
  {
    this.toDays = toDays;
    this.fromDays = fromDays;
    this.datePruner = datePruner;
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

  public String oneUnitAgo() {
    return oneUnitAgo;
  }

  public String unitNamePlural() {
    return unitNamePlural;
  }
}
