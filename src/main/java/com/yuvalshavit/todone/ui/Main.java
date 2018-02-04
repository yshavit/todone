package com.yuvalshavit.todone.ui;

import java.time.ZoneId;
import java.util.Collections;

import javax.inject.Inject;

import com.gluonhq.ignite.guice.GuiceContext;
import com.google.inject.AbstractModule;
import com.yuvalshavit.todone.data.DummyDao;
import com.yuvalshavit.todone.data.TodoneDao;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

  @Inject protected FXMLLoader fxmlLoader;

  public void start(Stage primaryStage) throws Exception {
    GuiceContext context = new GuiceContext(this, () -> Collections.singletonList(new GuiceModule()));
    context.init();
    fxmlLoader.setLocation(getClass().getResource("main_scene.fxml"));
    Parent root = fxmlLoader.load();

    primaryStage.setTitle("ToDone");
    primaryStage.setScene(new Scene(root, 600, 700));
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }

  static class GuiceModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(TodoneDao.class).toInstance(DummyDao.prePopulated());
      bind(ZoneId.class).toInstance(ZoneId.systemDefault());
    }
  }
}
