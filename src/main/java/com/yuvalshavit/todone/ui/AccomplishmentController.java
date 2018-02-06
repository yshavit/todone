package com.yuvalshavit.todone.ui;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Set;
import java.util.stream.Collectors;

import com.yuvalshavit.todone.data.Accomplishment;
import com.yuvalshavit.todone.data.Tagger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.util.Pair;

public class AccomplishmentController {
  public static final ControllerUtil.Renderer<AccomplishmentController> renderer = ControllerUtil.rendererFor(t -> t.top);

  private static final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT);

  @FXML protected Pane top;
  @FXML protected WebView text;
  @FXML protected Text timestamp;
  @FXML protected Text tags;

  public AccomplishmentController(Accomplishment accomplishment) {
    try {
      FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("accomplishment.fxml"));
      fxmlLoader.setController(this);
      top = fxmlLoader.load();
      Pair<String, Set<String>> escapedAndTagged = Tagger.escapeAndTag(accomplishment.getText());
      text.getEngine().loadContent("<p>" + escapedAndTagged.getKey() + "</p>");
      timestamp.setText(formatter.format(Instant.ofEpochMilli(accomplishment.getTimestamp()).atZone(ZoneId.systemDefault()).toLocalDateTime()));
      tags.setText(escapedAndTagged.getValue().stream().sorted().collect(Collectors.joining(", ")));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
