package com.yuvalshavit.todone.ui;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Set;
import java.util.TreeSet;

import com.yuvalshavit.todone.data.Accomplishment;
import com.yuvalshavit.todone.data.Tagger;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class AccomplishmentController {
  public static final ControllerUtil.Renderer<AccomplishmentController> renderer = ControllerUtil.rendererFor(t -> t.top);

  private static final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT);

  @FXML protected Pane top;
  @FXML protected TextFlow text;
  @FXML protected Text timestamp;

  public AccomplishmentController(Accomplishment accomplishment) {
    try {
      FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("accomplishment.fxml"));
      fxmlLoader.setController(this);
      top = fxmlLoader.load();

      ObservableList<Node> textSegments = text.getChildren();
      Tagger.escapeAndTag(
        accomplishment.getText(),
        plainText -> textSegments.add(new Text(plainText)),
        tag -> {
          Text segment = new Text("#" + tag);
          segment.getStyleClass().add("tag");
          TagEvents.fireOnEnterAndExit(segment, tag);
          textSegments.add(segment);
        }
      );

      timestamp.setText(formatter.format(Instant.ofEpochMilli(accomplishment.getTimestamp()).atZone(ZoneId.systemDefault()).toLocalDateTime()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
