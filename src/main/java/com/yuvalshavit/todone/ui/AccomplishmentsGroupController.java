package com.yuvalshavit.todone.ui;

import java.io.IOException;
import java.util.Comparator;

import com.yuvalshavit.todone.data.Accomplishment;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;

public class AccomplishmentsGroupController {
  public static final ControllerUtil.Renderer<AccomplishmentsGroupController> renderer = ControllerUtil.rendererFor(t -> t.top);
  @FXML protected Pane top;
  @FXML protected Label header;
  @FXML protected ListView<AccomplishmentController> accomplishments;

  public static Comparator<AccomplishmentsGroupController> biggestFirst = (a, b) -> Integer.compare(b.accomplishments.getItems().size(), a.accomplishments.getItems().size());

  public AccomplishmentsGroupController() {
    try {
      FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("accomplishment_group.fxml"));
      fxmlLoader.setController(this);
      top = fxmlLoader.load();
      DoubleProperty fixedCellSizeProperty = accomplishments.fixedCellSizeProperty();
      fixedCellSizeProperty.setValue(40);
      accomplishments.prefHeightProperty().bind(Bindings.size(accomplishments.getItems()).multiply(fixedCellSizeProperty).add(2));
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

  @Override
  public String toString() {
    int n = accomplishments.getItems().size();
    return String.format("%s: %d accomplishment%s", header, n, n == 1 ? "" : "s");
  }
}
