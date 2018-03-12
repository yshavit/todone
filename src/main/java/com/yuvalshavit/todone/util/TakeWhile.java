package com.yuvalshavit.todone.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class TakeWhile {
  private TakeWhile() {}

  public static <T> Iterable<T> wrap(Iterable<? extends T> delegate, Predicate<? super T> predicate) {
    return () -> wrap(delegate.iterator(), predicate);
  }
  
  public static <T> Iterator<T> wrap(Iterator<? extends T> delegate, Predicate<? super T> predicate) {
    return new Iterator<T>() {
      T pending = null;
      @Override
      public boolean hasNext() {
        if (pending != null) {
          return true;
        }
        if (!delegate.hasNext()) {
          return false;
        }
        T next = delegate.next();
        if (predicate.test(next)) {
          pending = next;
          return true;
        } else {
          return false;
        }
      }

      @Override
      public T next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        T result = pending;
        pending = null;
        return result;
      }
    };
  }
}
