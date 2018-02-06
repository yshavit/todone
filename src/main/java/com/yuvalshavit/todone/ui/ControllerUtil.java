package com.yuvalshavit.todone.ui;

import java.util.function.Function;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

public class ControllerUtil {
  private ControllerUtil() {}

  public static <T> Renderer<T> rendererFor(Function<T, Pane> getTop) {
    return list -> new ListCell<T>() {
      @Override
      protected void updateItem(T item, boolean empty) {
        if (empty) {
          setGraphic(null);
        } else {
          setGraphic(getTop.apply(item));
        }
      }
    };
  }

  /**
   * Convenience interface for a cell renderer callback
   * @param <T>
   */
  public interface Renderer<T> extends Callback<ListView<T>, ListCell<T>> {}
}
