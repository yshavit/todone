package com.yuvalshavit.todone.ui;

import java.util.function.Consumer;
import java.util.function.Function;

import javafx.scene.control.Cell;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

public class ControllerUtil {
  private ControllerUtil() {}

  public static <T> Renderer<T> rendererFor(Function<T, Pane> getTop, Consumer<? super Cell> hook) {
    return list -> {
      ListCell<T> cell = new ListCell<>();
      cell.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
      cell.itemProperty().addListener(((observable, oldValue, newValue) -> {
        cell.layout();
        if (newValue == null) {
          cell.setGraphic(null);
        } else {
          Pane pane = getTop.apply(newValue);
          cell.setGraphic(pane);
        }
        hook.accept(cell);
      }));
      return cell;
    };
  }

  public static <T> Renderer<T> rendererFor(Function<T, Pane> getTop) {
    return rendererFor(getTop, p -> {});
  }

  /**
   * Convenience interface for a cell renderer callback
   * @param <T>
   */
  public interface Renderer<T> extends Callback<ListView<T>, ListCell<T>> {}
}
