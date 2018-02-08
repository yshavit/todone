package com.yuvalshavit.todone.ui;

import java.io.IOException;
import java.util.Comparator;

import com.yuvalshavit.todone.data.Accomplishment;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class AccomplishmentsGroupController {
  public static final ControllerUtil.Renderer<AccomplishmentsGroupController> renderer = ControllerUtil.rendererFor(t -> t.top);
  @FXML protected Pane top;
  @FXML protected Label header;
  @FXML protected VBox accomplishments; //maybe a VBox? with TextViews and possibly autosize?

  private int size; // TODO replace with a list

  public static Comparator<AccomplishmentsGroupController> biggestFirst = (a, b) -> Integer.compare(b.size, a.size);

  public AccomplishmentsGroupController() {
    try {
      FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("accomplishment_group.fxml"));
      fxmlLoader.setController(this);
      top = fxmlLoader.load();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void setHeader(String header) {
    this.header.setText(header);
  }

  public void addAccomplishment(Accomplishment accomplishment) {
    ++size;
    AccomplishmentController ac = new AccomplishmentController(accomplishment);
    ac.top.autosize();
    accomplishments.getChildren().add(ac.top);
    top.autosize();
  }

  public int accomplishmentsCount() {
    return size;
  }

  @Override
  public String toString() {
    int n = size;
    return String.format("%s: %d accomplishment%s", header, n, n == 1 ? "" : "s");
  }
}
