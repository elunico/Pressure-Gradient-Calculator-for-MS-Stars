package pgc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("calculator_root.fxml"));
        FXMLLoader fxmlLoader = new FXMLLoader();
        Pane p = fxmlLoader.load(getClass().getResource("calculator_root.fxml")
                                           .openStream());
        Controller controller = fxmlLoader.getController();
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
