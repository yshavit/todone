package com.yuvalshavit.todone.ui;

import java.time.ZoneId;
import java.util.Collections;

import javax.inject.Inject;

import com.gluonhq.ignite.guice.GuiceContext;
import com.google.inject.AbstractModule;
import com.yuvalshavit.todone.data.FileBasedDao;
import com.yuvalshavit.todone.data.TodoneDao;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

  @Inject protected FXMLLoader fxmlLoader;

  public void start(Stage primaryStage) throws Exception {
    GuiceContext context = new GuiceContext(this, () -> Collections.singletonList(new GuiceModule()));
    context.init();

    Parent root;
    if (this.getParameters().getRaw().contains("--add_window")) {
      fxmlLoader.setLocation(getClass().getResource("add_accomplishment.fxml"));
      root = fxmlLoader.load();
      primaryStage.setTitle("Add Accomplishment");
      primaryStage.setScene(new Scene(root));

      primaryStage.setY(0);
      primaryStage.setAlwaysOnTop(true);
      primaryStage.setOnShown(event -> primaryStage.setX(Screen.getPrimary().getVisualBounds().getWidth() - primaryStage.getWidth()));
    } else {
      fxmlLoader.setLocation(getClass().getResource("main_scene.fxml"));
      root = fxmlLoader.load();
      primaryStage.setTitle("ToDone");
      primaryStage.setScene(new Scene(root, 600, 700));
    }
    root.getStylesheets().add(getClass().getResource("main.css").toExternalForm());
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }

  static class GuiceModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(TodoneDao.class).toInstance(new FileBasedDao());
      bind(ZoneId.class).toInstance(ZoneId.systemDefault());
    }
  }
}
