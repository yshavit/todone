package com.yuvalshavit.todone.data;

import java.util.Arrays;
import java.util.List;
import java.util.NavigableSet;
import java.util.Random;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;

import com.google.common.primitives.Ints;

public class DummyDao implements TodoneDao {
  private final NavigableSet<Accomplishment> accomplishments = new ConcurrentSkipListSet<>();

  public static DummyDao prePopulated() {
    DummyDao dao = new DummyDao();
    long now = System.currentTimeMillis();
    Random random = new Random(0); // consistent seed, why not

    List<String> texts = Arrays.asList(
      "fixed a #bug (12345)",
      "wrote my #featureA #spec",
      "#support call for Acme Inc",
      "tracked down #bug about something",
      "worked on another #support thing",
      "design meeting for #featureA",
      "groomed #spec for #featureA",
      "had a #featureA pitching meeting");
    int minTimeMillis = Ints.checkedCast(TimeUnit.HOURS.toMillis(2));
    int maxTimeMillis = Ints.checkedCast(TimeUnit.HOURS.toMillis(36));
    for (String text : texts) {
      now -= random.nextInt(maxTimeMillis - minTimeMillis) + minTimeMillis ; // between 2 hours and 2 days
      dao.add(new Accomplishment(now, text));
    }
    return dao;
  }

  @Override
  public void add(Accomplishment accomplishment) {
    accomplishments.add(accomplishment);
  }

  @Override
  public NavigableSet<Accomplishment> fetchAll() {
    return new TreeSet<>(accomplishments);
  }
}
