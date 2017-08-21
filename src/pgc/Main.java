package pgc;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import tom.utils.javafx.JavaFXUtilsKt;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = JavaFXUtilsKt.getRoot(this, "calculator_root.fxml");

        Controller controller = (Controller) JavaFXUtilsKt.getControllerForFile(this, "calculator_root.fxml");
        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER) {
                controller.goAction(null);
            } else if (e.getCode() == KeyCode.F1){
                controller.helpAction(null);
            }
        });
        primaryStage.setTitle("Main Sequence Calculator");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
