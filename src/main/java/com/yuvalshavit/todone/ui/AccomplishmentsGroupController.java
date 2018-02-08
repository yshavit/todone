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
  @FXML protected ListView<AccomplishmentController> accomplishments; //maybe a VBox? with TextViews and possibly autosize?

  public static Comparator<AccomplishmentsGroupController> biggestFirst = (a, b) -> Integer.compare(b.accomplishmentsCount(), a.accomplishmentsCount());

  public AccomplishmentsGroupController() {
    try {
      FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("accomplishment_group.fxml"));
      fxmlLoader.setController(this);
      top = fxmlLoader.load();
      accomplishments.setCellFactory(AccomplishmentController.renderer);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void setHeader(String header) {
    this.header.setText(header);
  }

  public void addAccomplishment(Accomplishment accomplishment) {
    accomplishments.getItems().add(new AccomplishmentController(accomplishment));
  }

  public int accomplishmentsCount() {
    return accomplishments.getItems().size();
  }

  @Override
  public String toString() {
    int n = accomplishmentsCount();
    return String.format("%s: %d accomplishment%s", header, n, n == 1 ? "" : "s");
  }
}
