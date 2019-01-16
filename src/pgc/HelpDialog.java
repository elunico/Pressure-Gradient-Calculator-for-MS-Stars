package pgc;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 * @author Thomas Povinelli
 * Created 2019-Jan-16
 * In Pressure-Gradient-Calculator-for-MS-Stars
 */
public class HelpDialog {
  public static void showHelpDialog() {
    Stage stage = new Stage();
    GridPane main = new GridPane();

    stage.setTitle("Help");
    stage.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
      if (e.getCode() == KeyCode.ESCAPE ||
          e.getCode() == KeyCode.F1)
      {
        stage.close();
      }
    });

    main.setAlignment(Pos.TOP_LEFT);
    main.setHgap(10);
    main.setVgap(10);
    main.setPadding(new Insets(10));

    Label l = new Label("Limit:");
    main.add(l, 0, 0);

    Label limitDescription = new Label(
        "The limit is the limit of the " + Controller.lineSeparator +
        "number of sections to divide the star into. For example" +
        Controller.lineSeparator +
        "if you set it to the default of 45 the star would be divided " +
        Controller.lineSeparator +
        "into 45 regions and the pressure gradient will be calculated" +
        Controller.lineSeparator +
        "over 45 separate regions yielding 45 values");
    main.add(limitDescription, 1, 0);

    Label r = new Label("Start Radius:");
    Label radiusDescription = new Label(
        "The radius is the starting radius" + Controller.lineSeparator +
        "of the first of the sections of the star. By default it is 23 million" +
        Controller.lineSeparator +
        "meters so the first section's pressure is calculated at 23 million " +
        Controller.lineSeparator +
        "meters. Every iteration adds <steps> to the radius until it gets to the" +
        Controller.lineSeparator +
        "end of the sun. In other words the entire radius of the star " +
        Controller.lineSeparator +
        "should be equal to (limit * steps) + radius. The radius is in meters");

    main.add(r, 0, 1);
    main.add(radiusDescription, 1, 1);

    Label d = new Label("Density:");
    Label densityDescription = new Label(
        "The average density over the " + Controller.lineSeparator +
        "convective zone of the star in grams per cubic centimeter.");

    main.add(d, 0, 2);
    main.add(densityDescription, 1, 2);

    Label s = new Label("Steps:");
    Label stepsDescription = new Label(
        "The steps number is a number in meters" + Controller.lineSeparator +
        "Every step this number will be added to the radius and the pressure will" +
        Controller.lineSeparator +
        "be calculated at that section of the sun. This number is added to the " +
        Controller.lineSeparator +
        "start radius once an iteration for <limit> iterations. In other words" +
        Controller.lineSeparator +
        "The pressure is caculated at radius <start radius> then <steps> is " +
        Controller.lineSeparator +
        "added to the <start radius> and the pressure is calculated again" +
        Controller.lineSeparator +
        "Then this repeats <limit> times and all results plus a total are output" +
        Controller.lineSeparator +
        "to the screen.");

    main.add(s, 0, 3);
    main.add(stepsDescription, 1, 3);

    Label note = new Label("Note:");
    main.add(note, 0, 4);

    Label comma = new Label(
        "You may use commas in numbers. Only density allows" + Controller.lineSeparator +
        "precision beyond the decimal point. The rest are integers");

    main.add(comma, 1, 4);

    Button closeButton = new Button("Close");
    closeButton.addEventHandler(
        MouseEvent.MOUSE_CLICKED,
        event -> stage.close()
    );

    main.add(closeButton, 1, 5);

    stage.setScene(new Scene(main));
    stage.show();
  }

  private HelpDialog() { }
}
