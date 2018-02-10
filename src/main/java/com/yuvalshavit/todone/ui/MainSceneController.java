package com.yuvalshavit.todone.ui;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.ResourceBundle;
import java.util.TreeMap;

import javax.inject.Inject;

import com.yuvalshavit.todone.data.Accomplishment;
import com.yuvalshavit.todone.data.Tagger;
import com.yuvalshavit.todone.data.TodoneDao;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;

public class MainSceneController implements Initializable {
  private static final int TAG_CHART_BUFFER = 1;
  private final LocalDate today = LocalDate.now();

  @FXML private ListView<AccomplishmentsGroupController> byDayList;
  @FXML private ListView<AccomplishmentsGroupController> byTagList;
  @FXML protected LineChart<Long,Integer> tagsChart;
  @FXML protected NumberAxis tagsChartX;
  @FXML protected NumberAxis tagsChartY;
  @Inject protected ZoneId zoneId;
  @Inject protected TodoneDao dao;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    NavigableMap<LocalDate,AccomplishmentsGroupController> byDayGroups = new TreeMap<>(Comparator.reverseOrder());
    Map<String,AccomplishmentsGroupController> byTagGroups = new HashMap<>();
    for (Accomplishment accomplishment : dao.fetchAll()) {
      Instant instant = Instant.ofEpochMilli(accomplishment.getTimestamp());
      LocalDate accomplishmentDate = instant.atZone(zoneId).toLocalDate();
      byDayGroups.computeIfAbsent(accomplishmentDate, this::createGroupForDay).addAccomplishment(accomplishment);
      for (String tag : Tagger.tagsFor(accomplishment.getText())) {
        byTagGroups.computeIfAbsent(tag, this::createGroupForTag).addAccomplishment(accomplishment);
      }
    }
    byDayList.getItems().addAll(byDayGroups.values());
    XYChart.Series<Long,Integer> tagsChartSeries = new XYChart.Series<>();// TODO one series per tag
    byDayGroups.descendingMap().forEach((date, group) -> {
      XYChart.Data<Long, Integer> dataPoint = new XYChart.Data<>(date.toEpochDay(), group.accomplishmentsCount());
      Label hoverLabel = new Label(String.valueOf(group.accomplishmentsCount()));

      StackPane hoverPane = new StackPane();
      hoverPane.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
        hoverPane.getChildren().add(hoverLabel);
      });
      hoverPane.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
        hoverPane.getChildren().clear();
      });
      dataPoint.setNode(hoverPane);
      tagsChartSeries.getData().add(dataPoint);
    });
    tagsChartX.setTickLabelFormatter(new StringConverter<Number>() {
      final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
      @Override
      public String toString(Number object) {
        return formatter.format(LocalDate.ofEpochDay(object.longValue()));
      }

      @Override
      public Number fromString(String string) {
        return LocalDate.from(formatter.parse(string)).toEpochDay();
      }
    });
    tagsChartX.setAutoRanging(false);
    tagsChartX.setLowerBound(byDayGroups.lastKey().toEpochDay() - TAG_CHART_BUFFER);
    tagsChartX.setUpperBound(LocalDate.now().toEpochDay() + TAG_CHART_BUFFER);
    tagsChart.getData().add(tagsChartSeries);
    byDayList.getItems().addAll(byDayGroups.values());
    List<AccomplishmentsGroupController> tagGroups = new ArrayList<>(byTagGroups.values());
    tagGroups.sort(AccomplishmentsGroupController.biggestFirst);
    for (AccomplishmentsGroupController group : tagGroups) {
      String header = group.header.getText();
      int n = group.accomplishmentsCount();
      header = String.format("%s (%d accomplishment%s)", header, n, n == 1 ? "" : "s");
      group.header.setText(header);
    }
    byTagList.getItems().addAll(tagGroups);
    byTagList.setCellFactory(AccomplishmentsGroupController.renderer);
    byDayList.setCellFactory(AccomplishmentsGroupController.renderer);
  }

  private AccomplishmentsGroupController createGroupForDay(LocalDate date) {
    final String header;
    if (date.equals(today)) {
      header = "Today";
    } else if (date.equals(today.minusDays(1))) {
      header = "Yesterday";
    } else {
      long daysAgo = today.toEpochDay() - date.toEpochDay();
      header = String.format("%s (%d days ago)", DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(date), daysAgo);
    }
    return createAccomplishmentGroup(header);
  }

  private AccomplishmentsGroupController createAccomplishmentGroup(String header) {
    AccomplishmentsGroupController group = new AccomplishmentsGroupController();
    group.setHeader(header);
    return group;
  }

  private AccomplishmentsGroupController createGroupForTag(String tag) {
    String header = "#" + tag;
    return createAccomplishmentGroup(header);
  }

}
