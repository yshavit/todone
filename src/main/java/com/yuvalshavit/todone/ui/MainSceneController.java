package com.yuvalshavit.todone.ui;

import java.io.IOException;
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

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.yuvalshavit.todone.data.Accomplishment;
import com.yuvalshavit.todone.data.Tagger;
import com.yuvalshavit.todone.data.TodoneDao;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ListView;
import javafx.util.StringConverter;

public class MainSceneController implements Initializable {
  private static final int TAG_CHART_BUFFER = 1;
  private static final StringConverter<Number> EPOCH_DAY_TICK_FORMATTER = new StringConverter<Number>() {
    final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;

    @Override
    public String toString(Number object) {
      return formatter.format(LocalDate.ofEpochDay(object.longValue()));
    }

    @Override
    public Number fromString(String string) {
      return LocalDate.from(formatter.parse(string)).toEpochDay();
    }
  };
  private final LocalDate today = LocalDate.now();

  @FXML private ListView<AccomplishmentsGroupController> byDayList;
  @FXML private ListView<AccomplishmentsGroupController> byTagList;
  @FXML private XYChart<Long,Integer> tagsChart;
  @FXML private NumberAxis tagsChartX;
  @FXML private NumberAxis tagsChartY;
  @FXML private Parent mainTop;
  @Inject protected ZoneId zoneId;
  @Inject protected TodoneDao dao;

  private Map<LocalDate,AccomplishmentsGroupController> groupsByDay;
  private Map<String,AccomplishmentsGroupController> groupsByTag;
  /**
   * Key is a tag. Series is epoch-day to count.
   */
  private Map<String,XYChart.Series<Long,Integer>> chartByTag;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    groupsByDay = new HashMap<>();
    groupsByTag = new HashMap<>();
    chartByTag = new HashMap<>();
    byTagList.setCellFactory(AccomplishmentsGroupController.renderer);
    byDayList.setCellFactory(AccomplishmentsGroupController.renderer);

    long todayEpoch = today.toEpochDay();
    tagsChartX.setAutoRanging(false);
    tagsChartX.setLowerBound(todayEpoch - TAG_CHART_BUFFER);
    tagsChartX.setUpperBound(todayEpoch + TAG_CHART_BUFFER);
    tagsChartX.setTickLabelFormatter(EPOCH_DAY_TICK_FORMATTER);
    tagsChartY.setForceZeroInRange(true);

    mainTop.addEventHandler(TagEvents.TAG_ENTER, this::handleTagEvent);
    mainTop.addEventHandler(TagEvents.TAG_EXIT, this::handleTagEvent);

    try {
      dao.fetchAll().forEach(this::addAccomplishment);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void handleTagEvent(TagEvents.TagEvent event) {
    final String chartAreaDimmedClass = "chart-dimmed";
    final String chartLineHighlightedClass = "chart-highlighted-line";
    chartByTag.forEach((tag, series) -> {
      ObservableList<String> fillStyles = series.getNode().lookup(".chart-series-area-fill").getStyleClass();
      ObservableList<String> lineStyles = series.getNode().lookup(".chart-series-area-line").getStyleClass();
      if (event.getEventType() == TagEvents.TAG_EXIT) {
        fillStyles.remove(chartAreaDimmedClass);
        lineStyles.remove(chartAreaDimmedClass);
        lineStyles.remove(chartLineHighlightedClass);
      } else if (Objects.equals(tag, event.tag)) {
        fillStyles.remove(chartAreaDimmedClass);
        lineStyles.remove(chartAreaDimmedClass);
        lineStyles.add(chartLineHighlightedClass);
      } else {
        fillStyles.add(chartAreaDimmedClass);
        lineStyles.add(chartAreaDimmedClass);
        lineStyles.remove(chartLineHighlightedClass);
      }
    });
  }

  private void setChartRange(long epochDay) {
    double newRangeLower = epochDay - TAG_CHART_BUFFER;
    double newRangeUpper = epochDay + TAG_CHART_BUFFER;

    if (tagsChartX.getLowerBound() < newRangeLower) {
      tagsChartX.setLowerBound(newRangeLower);
    }
    if (tagsChartX.getUpperBound() > newRangeUpper) {
      tagsChartX.setUpperBound(newRangeUpper);
    }
  }

  public void addAccomplishment(Accomplishment accomplishment) {
    LocalDate accomplishmentDate = Instant.ofEpochMilli(accomplishment.getTimestamp()).atZone(zoneId).toLocalDate();
    long accomplishmentEpochDay = accomplishmentDate.toEpochDay();
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
      final XYChart.Data<Long, Integer> dataPoint;
      XYChart.Series<Long, Integer> series = chartByTag.get(tag);
      if (series == null) {
        series = new XYChart.Series<>();
        series.setName(tag);
        chartByTag.put(tag, series);
        tagsChart.getData().add(series);
      }
      ObservableList<XYChart.Data<Long, Integer>> seriesData = series.getData();
      List<Long> seriesDataEpochDay = Lists.transform(seriesData, XYChart.Data::getXValue);
      int searchResult = Collections.binarySearch(seriesDataEpochDay, accomplishmentEpochDay);
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
    setChartRange(accomplishmentEpochDay);
    Collections.sort(byDayList.getItems());
    Collections.sort(byTagList.getItems());
  }

  private XYChart.Data<Long, Integer> createTagPoint(long epochDay) {
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
