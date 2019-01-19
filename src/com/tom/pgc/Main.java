package com.tom.pgc;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static tom.utils.javafx.JavaFXUtilsKt.getControllerFromFXML;
import static tom.utils.javafx.JavaFXUtilsKt.getRootFromFXML;

public class Main extends Application {

  static Map<Stage, Double> stages = Collections.synchronizedMap(new HashMap<>());

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(@NotNull Stage primaryStage) throws Exception {
    FlowPane root = getRootFromFXML(this, "calculator_root.fxml");
    Controller controller = getControllerFromFXML(this, "calculator_root.fxml");

    if (root == null || controller == null) {
      throw new RuntimeException("Could not load FXML file for UI. Application Terminated");
    }

    stages.put(primaryStage, 1.0);

    primaryStage.setTitle("Main Sequence Calculator");
    primaryStage.setScene(new Scene(root));
    primaryStage.show();
  }
}
