package com.yuvalshavit.todone.ui;

import java.io.IOException;
import java.util.function.ToLongFunction;

import com.yuvalshavit.todone.data.Accomplishment;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;

public class AccomplishmentsGroupController implements Comparable<AccomplishmentsGroupController> {
  public static final ControllerUtil.Renderer<AccomplishmentsGroupController> renderer = ControllerUtil.rendererFor(t -> t.top);

  @FXML protected Pane top;
  @FXML protected Label header;
  @FXML protected ListView<AccomplishmentController> accomplishments;

  private final ToLongFunction<AccomplishmentsGroupController> comparisonKey;

  private AccomplishmentsGroupController(ToLongFunction<AccomplishmentsGroupController> comparisonKey) {
    this.comparisonKey = comparisonKey;
    try {
      FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("accomplishment_group.fxml"));
      fxmlLoader.setController(this);
      top = fxmlLoader.load();
      accomplishments.setCellFactory(AccomplishmentController.renderer);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static AccomplishmentsGroupController create(String headerPrefix, ToLongFunction<AccomplishmentsGroupController> comparisonKey) {
    AccomplishmentsGroupController created = new AccomplishmentsGroupController(comparisonKey);
    ListChangeListener<AccomplishmentController> changeListener = change -> {
      int n = created.accomplishmentsCount();
      created.header.setText(String.format("%s (%d accomplishment%s)", headerPrefix, n, n == 1 ? "" : "s"));
    };
    created.accomplishments.getItems().addListener(changeListener);
    return created;
  }

  public void addAccomplishment(Accomplishment accomplishment) {
    accomplishments.getItems().add(new AccomplishmentController(accomplishment));
  }

  public int accomplishmentsCount() {
    return accomplishments.getItems().size();
  }

  @Override
  public int compareTo(AccomplishmentsGroupController o) {
    // sort largest first
    return - Long.compare(comparisonKey.applyAsLong(this), o.comparisonKey.applyAsLong(o));
  }

  @Override
  public String toString() {
    return header.getText();
  }
}
