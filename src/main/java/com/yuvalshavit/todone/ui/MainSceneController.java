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
import java.util.ResourceBundle;
import java.util.TreeMap;

import javax.inject.Inject;

import com.yuvalshavit.todone.data.Accomplishment;
import com.yuvalshavit.todone.data.Tagger;
import com.yuvalshavit.todone.data.TodoneDao;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

public class MainSceneController implements Initializable {
  private final LocalDate today = LocalDate.now();

  @FXML private ListView<AccomplishmentsGroupController> byDayList;
  @FXML private ListView<AccomplishmentsGroupController> byTagList;
  @Inject protected ZoneId zoneId;
  @Inject protected TodoneDao dao;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    Map<LocalDate,AccomplishmentsGroupController> byDayGroups = new TreeMap<>(Comparator.reverseOrder());
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
