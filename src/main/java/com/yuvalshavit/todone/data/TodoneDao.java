package com.yuvalshavit.todone.data;

import java.io.IOException;
import java.util.NavigableSet;

public interface TodoneDao {
  void add(Accomplishment accomplishment) throws IOException;
  NavigableSet<Accomplishment> fetchAll() throws IOException;
}
