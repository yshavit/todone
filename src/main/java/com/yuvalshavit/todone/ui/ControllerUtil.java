package com.yuvalshavit.todone.ui;

import java.util.function.Function;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

public class ControllerUtil {
  private ControllerUtil() {}

  public static <T> Renderer<T> rendererFor(Function<T, Pane> getTop) {
    return list -> {
      ListCell<T> cell = new ListCell<>();
      cell.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
      cell.itemProperty().addListener(((observable, oldValue, newValue) -> {
        if (newValue == null) {
          cell.setGraphic(null);
        } else {
          cell.setGraphic(getTop.apply(newValue));
        }
      }));
      return cell;
    };
  }

  /**
   * Convenience interface for a cell renderer callback
   * @param <T>
   */
  public interface Renderer<T> extends Callback<ListView<T>, ListCell<T>> {}
}
