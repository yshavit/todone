package com.yuvalshavit.todone.data;

import java.util.stream.StreamSupport;

public interface TodoneDao {
  void add(Accomplishment accomplishment);
  Iterable<Accomplishment> fetchAll();

  default Iterable<String> tags() {
    return () -> StreamSupport.stream(fetchAll().spliterator(), false)
      .flatMap(a -> Tagger.tagsFor(a.getText()).stream())
      .iterator();
  }
}
