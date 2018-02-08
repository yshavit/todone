package com.yuvalshavit.todone.ui;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.base.Joiner;
import com.yuvalshavit.todone.data.Accomplishment;
import com.yuvalshavit.todone.data.Tagger;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.effect.Effect;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class AccomplishmentController {
  public static final ControllerUtil.Renderer<AccomplishmentController> renderer = ControllerUtil.rendererFor(t -> t.top);

  private static final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT);

  @FXML protected Pane top;
  @FXML protected TextFlow text;
  @FXML protected Text timestamp;
  @FXML protected Text tags;

  public AccomplishmentController(Accomplishment accomplishment) {
    try {
      FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("accomplishment.fxml"));
      fxmlLoader.setController(this);
      top = fxmlLoader.load();

      Set<String> tagsSet = new TreeSet<>();

      ObservableList<Node> textSegments = text.getChildren();
      Tagger.escapeAndTag(
        accomplishment.getText(),
        plainText -> textSegments.add(new Text(plainText)),
        tag -> {
          Text segment = new Text("#" + tag);
          segment.setUnderline(true);
          textSegments.add(segment); // TODO better styling
          tagsSet.add(tag);
        }
      );
      tags.setText(Joiner.on(", ").join(tagsSet));

      timestamp.setText(formatter.format(Instant.ofEpochMilli(accomplishment.getTimestamp()).atZone(ZoneId.systemDefault()).toLocalDateTime()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
