package com.yuvalshavit.todone.ui;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.yuvalshavit.todone.data.Accomplishment;
import com.yuvalshavit.todone.data.Tagger;
import com.yuvalshavit.todone.data.TodoneDao;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ListView;
import javafx.util.StringConverter;

public class MainSceneController implements Initializable {
  private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_DATE;
  private static final StringConverter<Long> EPOCH_DAY_TICK_FORMATTER = new StringConverter<Long>() {

    @Override
    public String toString(Long object) {
      return ISO_DATE.format(LocalDate.ofEpochDay(object));
    }

    @Override
    public Long fromString(String string) {
      return LocalDate.from(ISO_DATE.parse(string)).toEpochDay();
    }
  };
  private final LocalDate today = LocalDate.now();

  @FXML private ListView<AccomplishmentsGroupController> byDayList;
  @FXML private ListView<AccomplishmentsGroupController> byTagList;
  @FXML private StackedBarChart<String,Integer> tagsChart;
  @FXML private NumberAxis tagsChartY;
  @FXML private Parent mainTop;
  @Inject protected ZoneId zoneId;
  @Inject protected TodoneDao dao;

  private Map<LocalDate,AccomplishmentsGroupController> groupsByDay;
  private Map<String,AccomplishmentsGroupController> groupsByTag;
  /**
   * Key is a tag. Series is epoch-day to count.
   */
  private Map<String,XYChart.Series<String,Integer>> chartByTag;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    groupsByDay = new HashMap<>();
    groupsByTag = new HashMap<>();
    chartByTag = new HashMap<>();
    byTagList.setCellFactory(AccomplishmentsGroupController.renderer);
    byDayList.setCellFactory(AccomplishmentsGroupController.renderer);

    tagsChart.setCategoryGap(0);
    tagsChartY.setForceZeroInRange(true);

    mainTop.addEventHandler(TagEvents.TAG_ENTER, this::handleTagEvent);
    mainTop.addEventHandler(TagEvents.TAG_EXIT, this::handleTagEvent);

    dao.fetchAll().forEach(this::addAccomplishment);
  }

  private void handleTagEvent(TagEvents.TagEvent event) {
    final String chartAreaDimmedClass = "chart-dimmed";
    chartByTag.forEach((tag, series) -> {
      for (XYChart.Data<String, Integer> data : series.getData()) {
        Node node = data.getNode();
        ObservableList<String> fillStyles = node.getStyleClass();
        if (event.getEventType() == TagEvents.TAG_EXIT) {
          fillStyles.remove(chartAreaDimmedClass);
        } else if (Objects.equals(tag, event.tag)) {
          fillStyles.remove(chartAreaDimmedClass);
        } else {
          fillStyles.add(chartAreaDimmedClass);
        }
      }
    });
  }

  public void addAccomplishment(Accomplishment accomplishment) {
    LocalDate accomplishmentDate = Instant.ofEpochMilli(accomplishment.getTimestamp()).atZone(zoneId).toLocalDate();
    String accomplishmentEpochDay = ISO_DATE.format(accomplishmentDate);
    // Add to the by-day list
    AccomplishmentsGroupController groupForDay = groupsByDay.get(accomplishmentDate);
    if (groupForDay == null) {
      groupForDay = createGroupForDay(accomplishmentDate);
      groupsByDay.put(accomplishmentDate, groupForDay);
      byDayList.getItems().add(groupForDay);
    }
    groupForDay.addAccomplishment(accomplishment);

    // Add to the by-tag lists
    for (String tag : Tagger.tagsFor(accomplishment.getText())) {
      AccomplishmentsGroupController groupForTag = groupsByTag.get(tag);
      if (groupForTag == null) {
        groupForTag = createGroupForTag(tag);
        groupsByTag.put(tag, groupForTag);
        byTagList.getItems().add(groupForTag);
      }
      groupForTag.addAccomplishment(accomplishment);
      // add a blip to the chart
      final XYChart.Data<String, Integer> dataPoint;
      XYChart.Series<String, Integer> series = chartByTag.get(tag);
      if (series == null) {
        series = new XYChart.Series<>();
        series.setName(tag);
        chartByTag.put(tag, series);
        tagsChart.getData().add(series);
      }
      ObservableList<XYChart.Data<String, Integer>> seriesData = series.getData();
      List<String> seriesDataEpochDay = Lists.transform(seriesData, XYChart.Data::getXValue);
      int searchResult = binarySearchForEpochDay(seriesDataEpochDay, accomplishmentEpochDay);
      if (searchResult >= 0) {
        dataPoint = seriesData.get(searchResult);
      } else {
        // See JavaDoc for binarySearch
        // searchResult         = -(insertion point) - 1
        // searchResult + 1     = -(insertion point)
        // -(searchResult + 1)  = (insertion point)
        int insertionPoint = -(searchResult + 1);
        dataPoint = createTagPoint(accomplishmentEpochDay);
        seriesData.add(insertionPoint, dataPoint);
      }
      int newY = 1 + dataPoint.getYValue();
      double newChartHeight = newY + 1.5;
      dataPoint.setYValue(newY);
      if (newChartHeight > tagsChartY.getUpperBound()) {
        tagsChartY.setUpperBound(newChartHeight);
      }
    }

    // set the chart range and sort the lists
    Collections.sort(byDayList.getItems());
    Collections.sort(byTagList.getItems());
  }

  private int binarySearchForEpochDay(List<String> epochDays, String searchDay) {
    long searchNumber = EPOCH_DAY_TICK_FORMATTER.fromString(searchDay);
    List<Long> searchNumbers = epochDays.stream().map(EPOCH_DAY_TICK_FORMATTER::fromString).collect(Collectors.toList());
    return Collections.binarySearch(searchNumbers, searchNumber);
  }

  private XYChart.Data<String, Integer> createTagPoint(String epochDay) {
    // Set up the hover labels when you mouseover a point
    // Label hoverLabel = new Label("");
    // StackPane hoverPane = new StackPane();
    // hoverPane.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> hoverPane.getChildren().add(hoverLabel));
    // hoverPane.addEventHandler(MouseEvent.MOUSE_EXITED, e -> hoverPane.getChildren().clear());
    // dataPoint.YValueProperty().addListener(((observable, oldValue, newValue) -> hoverLabel.setText(String.valueOf(newValue))));
    // dataPoint.setNode(hoverPane);
    return new XYChart.Data<>(epochDay, 0);
  }

  private AccomplishmentsGroupController createGroupForDay(LocalDate date) {
    final String header;
    long epochDay = date.toEpochDay();
    if (date.equals(today)) {
      header = "Today";
    } else if (date.equals(today.minusDays(1))) {
      header = "Yesterday";
    } else {
      long daysAgo = today.toEpochDay() - epochDay;
      header = String.format("%s (%d days ago)", DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(date), daysAgo);
    }
    return createAccomplishmentGroup(header, ignored -> epochDay);
  }

  private AccomplishmentsGroupController createAccomplishmentGroup(String header, ToLongFunction<AccomplishmentsGroupController> comparisonKey) {
    return AccomplishmentsGroupController.create(header, comparisonKey);
  }

  private AccomplishmentsGroupController createGroupForTag(String tag) {
    // comparator is negated so that we sort largest-first
    return createAccomplishmentGroup("#" + tag, AccomplishmentsGroupController::accomplishmentsCount);
  }
}
