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
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.LongFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.yuvalshavit.todone.data.Accomplishment;
import com.yuvalshavit.todone.data.Tagger;
import com.yuvalshavit.todone.data.TodoneDao;
import com.yuvalshavit.todone.util.Aggregator;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ListView;

public class MainSceneController implements Initializable {
  private final Aggregator aggregator = Aggregator.byWeek;
  private final LocalDate today = LocalDate.now();

  @FXML private ListView<AccomplishmentsGroupController> byDayList;
  @FXML private ListView<AccomplishmentsGroupController> byTagList;
  @FXML private StackedBarChart<String,Integer> tagsChart;
  @FXML private CategoryAxis tagsChartX;
  @FXML private NumberAxis tagsChartY;
  @FXML private Parent mainTop;
  @Inject protected ZoneId zoneId;
  @Inject protected TodoneDao dao;

  private Map<Long,AccomplishmentsGroupController> groupsByDay;
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
    long accomplishmentEpochDay = aggregator.toLong(accomplishmentDate);
    // Add to the by-day list
    AccomplishmentsGroupController groupForDay = groupsByDay.get(accomplishmentEpochDay);
    if (groupForDay == null) {
      groupForDay = createGroupForDay(accomplishmentDate);
      groupsByDay.put(accomplishmentEpochDay, groupForDay);
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
      XYChart.Series<String, Integer> series = chartByTag.get(tag);
      if (series == null) {
        series = new XYChart.Series<>();
        series.setName(tag);
        chartByTag.put(tag, series);
        tagsChart.getData().add(series);
      }
      ObservableList<XYChart.Data<String, Integer>> seriesData = series.getData();
      XYChart.Data<String, Integer> dataPoint = insertToListSortedByEpochDays(
        seriesData,
        accomplishmentEpochDay,
        data -> aggregator.toDays().applyAsLong(data.getXValue()),
        day -> createTagPoint(aggregator.fromDays().apply(day)));
      int newY = 1 + dataPoint.getYValue();
      double newChartHeight = newY + 1.5;
      dataPoint.setYValue(newY);
      if (newChartHeight > tagsChartY.getUpperBound()) {
        tagsChartY.setUpperBound(newChartHeight);
      }
      insertToListSortedByEpochDays(tagsChartX.getCategories(), accomplishmentEpochDay, aggregator.toDays(), aggregator.fromDays());
    }

    // set the chart range and sort the lists
    Collections.sort(byDayList.getItems());
    Collections.sort(byTagList.getItems());

    updateChartCategories(aggregator.toDays(), aggregator.fromDays());
  }

  private void updateChartCategories(ToLongFunction<String> toDays, LongFunction<String> fromDays) {
    ObservableList<String> categories = tagsChartX.getCategories();
    if (!categories.isEmpty()) {
      ListIterator<String> categoriesIter = categories.listIterator();
      long previousDay = toDays.applyAsLong(categoriesIter.next());
      while (categoriesIter.hasNext()) {
        long expectedNextDay = previousDay + 1;
        long currentDay = toDays.applyAsLong(categoriesIter.next());
        if (currentDay > expectedNextDay) {
          // Go back one, add the days we need, then fast-forward (so that we don't see this same day again)
          categoriesIter.previous();
          for (long day = expectedNextDay; day < currentDay; ++day) {
            categoriesIter.add(fromDays.apply(day));
          }
          categoriesIter.next();
        }
        previousDay = currentDay;
      }
    }
    tagsChartX.setCategories(categories); // forces a refresh of the categories order
  }

  private static <T> T insertToListSortedByEpochDays(List<T> list, long epochDay, ToLongFunction<T> toDays, LongFunction<T> fromDays) {
    List<Long> listAsDays = list.stream().mapToLong(toDays).boxed().collect(Collectors.toList());
    int searchResult = Collections.binarySearch(listAsDays, epochDay);
    if (searchResult >= 0) {
      return list.get(searchResult);
    } else {
      // See JavaDoc for binarySearch
      // searchResult         = -(insertion point) - 1
      // searchResult + 1     = -(insertion point)
      // -(searchResult + 1)  = (insertion point)
      int insertionPoint = -(searchResult + 1);
      T dataPoint = fromDays.apply(epochDay);
      list.add(insertionPoint, dataPoint);
      return dataPoint;
    }
  }

  private static XYChart.Data<String, Integer> createTagPoint(String epochDay) {
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
    long epochDay = aggregator.toLong(date);
    long daysAgo = aggregator.toLong(today) - epochDay;
    final String header;
    if (daysAgo == 0) {
      header = aggregator.thisUnit();
    } else if (daysAgo == 1) {
      header = aggregator.oneUnitAgo();
    } else {
      header = String.format("%s (%d %s ago)", DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(date), daysAgo, aggregator.unitNamePlural());
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
