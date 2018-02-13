package com.yuvalshavit.todone.data;

public interface TodoneDao {
  void add(Accomplishment accomplishment);
  Iterable<Accomplishment> fetchAll();
}
