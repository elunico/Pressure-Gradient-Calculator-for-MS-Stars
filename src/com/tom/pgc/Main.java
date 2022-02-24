package com.tom.pgc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Labeled;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;


public class Main extends Application {
  static Map<Stage, Double> stages = Collections.synchronizedMap(new HashMap<>());

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    FlowPane root = getRootFromFXML();
    Controller controller = getControllerFromFXML();
    Properties properties = new Properties();
    properties.load(getClass().getResourceAsStream("app.properties"));

    setFont(root, Font.font(Objects.requireNonNullElse(properties.getProperty("fontFamily"), "")));

    if (controller == null) {
      throw new RuntimeException("Could not load FXML file for UI. Application Terminated");
    }

    stages.put(primaryStage, 1.0);

    primaryStage.setTitle("Main Sequence Calculator");
    primaryStage.setScene(new Scene(root));
    primaryStage.show();
  }

  private void setFont(Parent root, Font font) {
    for (var child : root.getChildrenUnmodifiable()) {
      if (child instanceof Labeled labeled) {
        labeled.setFont(font);
      } else if (child instanceof Parent parent) {
        setFont(parent, font);
      }
    }
  }

  <T> T getControllerFromFXML() throws IOException {
    var fxmlLoader = new FXMLLoader();
    fxmlLoader.load(Objects.requireNonNull(this.getClass().getResource("calculator_root.fxml")).openStream());
    return fxmlLoader.getController();
  }

  <T> T getRootFromFXML() throws IOException {
    return FXMLLoader.load(Objects.requireNonNull(getClass().getResource("calculator_root.fxml")));
  }
}
