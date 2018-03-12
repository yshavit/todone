package com.yuvalshavit.todone.ui;

import java.net.URL;
import java.util.ResourceBundle;

import javax.inject.Inject;

import com.yuvalshavit.todone.data.Accomplishment;
import com.yuvalshavit.todone.data.TodoneDao;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class AddAccomplishmentController implements Initializable {
  @FXML private Button submitButton;
  @FXML private TextField accomplishmentText;
  @Inject protected TodoneDao dao;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    Autocomplete autocomplete = new Autocomplete();
    dao.tags().forEach(autocomplete::addOption);
    autocomplete.wrap(accomplishmentText);
    submitButton.setOnAction(event -> submit());
    accomplishmentText.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
      if (event.getCode() == KeyCode.ESCAPE) {
        closeWindow();
      }
    });
  }

  public void submit() {
    String text = accomplishmentText.getText().trim();
    if (!text.isEmpty()) {
      dao.add(new Accomplishment(System.currentTimeMillis(), text));
      closeWindow();
    }
  }

  public void closeWindow() {
    Stage stage = (Stage) accomplishmentText.getScene().getWindow();
    stage.close();
  }
}
