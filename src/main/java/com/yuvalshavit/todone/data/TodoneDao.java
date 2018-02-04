package com.yuvalshavit.todone.data;

import java.util.NavigableSet;

public interface TodoneDao {
  void add(Accomplishment accomplishment);
  NavigableSet<Accomplishment> fetchAll();
}
