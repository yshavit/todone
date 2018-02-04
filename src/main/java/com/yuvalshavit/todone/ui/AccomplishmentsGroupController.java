package com.yuvalshavit.todone.ui;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;

import javax.inject.Inject;

import com.yuvalshavit.todone.data.Accomplishment;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

public class AccomplishmentsGroupController {
  public static final Callback<ListView<AccomplishmentsGroupController>, ListCell<AccomplishmentsGroupController>> renderer = param -> new ListCell<AccomplishmentsGroupController>() {
    @Override
    protected void updateItem(AccomplishmentsGroupController item, boolean empty) {
      if (empty) {
        setGraphic(null);
      } else {
        setGraphic(item.top);
      }
    }
  };
  @FXML protected Pane top;
  @FXML protected Label header = new Label();
  @FXML protected ListView<Accomplishment> accomplishments = new ListView<>();

  public static Comparator<AccomplishmentsGroupController> biggestFirst = (a, b) -> Integer.compare(b.accomplishments.getItems().size(), a.accomplishments.getItems().size());

  public AccomplishmentsGroupController() {
    try {
      FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("accomplishment_group.fxml"));
      fxmlLoader.setController(this);
      top = fxmlLoader.load();
      DoubleProperty fixedCellSizeProperty = accomplishments.fixedCellSizeProperty();
      fixedCellSizeProperty.setValue(40);
      accomplishments.prefHeightProperty().bind(Bindings.size(accomplishments.getItems()).multiply(fixedCellSizeProperty));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void setHeader(String header) {
    this.header.setText(header);
  }

  public void addAccomplishment(Accomplishment accomplishment) {
    accomplishments.getItems().add(accomplishment);
  }

  @Override
  public String toString() {
    int n = accomplishments.getItems().size();
    return String.format("%s: %d accomplishment%s", header, n, n == 1 ? "" : "s");
  }
}
