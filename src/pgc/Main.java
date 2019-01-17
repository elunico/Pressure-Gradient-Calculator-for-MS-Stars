package pgc;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import static tom.utils.javafx.JavaFXUtilsKt.getControllerFromFXML;
import static tom.utils.javafx.JavaFXUtilsKt.getRootFromFXML;

public class Main extends Application {

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

    primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
      if (e.getCode() == KeyCode.ENTER) {
        controller.goAction();
      } else if (e.getCode() == KeyCode.F1) {
        controller.helpAction();
      }
    });
    primaryStage.setTitle("Main Sequence Calculator");
    primaryStage.setScene(new Scene(root));
    primaryStage.show();
  }
}
