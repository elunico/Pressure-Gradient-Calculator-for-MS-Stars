package pgc;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Controller {
  static final String lineSeparator = System.lineSeparator();

  private static boolean displayPrompt(
      @NotNull String message,
      @NotNull String confirmName,
      @NotNull String refuseName
  ) {
    Stage stage = new Stage();
    VBox main = new VBox();

    main.setPadding(new Insets(5));
    main.setSpacing(5);

    main.getChildren().add(new Label(message));

    Button confirmButton = new Button(confirmName);
    Button refuseButton = new Button(refuseName);
    final boolean[] result = new boolean[1];

    confirmButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
      stage.close();
      result[0] = true;
    });

    refuseButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
      stage.close();
      result[0] = false;
    });

    main.getChildren().addAll(confirmButton, refuseButton);

    stage.setScene(new Scene(main));
    stage.show();

    return result[0];
  }

  public TextField limitField;
  public TextField radiusField;
  public TextField densityField;
  public TextField stepsField;
  public CheckBox prettyExponentBox;
  private boolean prettyExponents;

  public void goAction() {
    prettyExponents = prettyExponentBox.isSelected();

    InputData data = new InputData(limitField.getText(), radiusField.getText(),
                                   densityField.getText(), stepsField.getText());
    InputData.Parameters parameters = data.parse();
    if (parameters == null) {
      return;
    }
    try {
      Pair<Double, String> res = calculateMainSequence(parameters);
      showResultsStage(parameters, res);
    } catch (ArithmeticException e) {
      displayAlert(e.getMessage());
    }
  }

  /**
   * Uses a predetermined formula to calculate a list of pressures along a
   * gradient of a main sequence star given the certain parameters of the star.
   *
   * @param parameters contains the params for calculating the function value
   *                   limit   the number of total sections to divide the star into
   *                   radius  the radius of the star
   *                   steps   the number of steps to go through
   *                   density the density of the star in g/cm^3
   * @return a pair containing the total as a Double and a String containing
   * all the double values of each step separated by a new line
   * @throws ArithmeticException if the number of steps is high enough to overflow
   *                             {@link Double}
   */
  @NotNull
  private Pair<Double, String> calculateMainSequence(@NotNull InputData.Parameters parameters)
      throws ArithmeticException {

    int limit = parameters.getLimit();
    long steps = parameters.getSteps();
    long radius = parameters.getRadius();
    double density = parameters.getDensity();

    int basemass = 10;
    int exponent = 1;
    double total = 0;

    ArrayList<Double> pressures = new ArrayList<>(limit - exponent);
    while (exponent <= limit) {
      double pressure = (((6.6738 * (Math.pow(10, -11))) * (Math.pow(
          basemass, exponent))) / ((radius << 1))) * density;
      if (Double.isInfinite(pressure)) {
        throw new ArithmeticException("Too many sections given. Overflow!");
      }
      pressures.add(pressure);
      total += pressure;
      exponent += 1;
      radius += steps;
    }

    ArrayList<String> result = new ArrayList<>(pressures.size());

    for (Double number : pressures) {
      String stringValue = String.valueOf(number);
      if (stringValue.contains("E") && prettyExponents) {
        result.add(stringValue.replace("E", " * (10^") + ")");
      } else {
        result.add(stringValue);
      }
    }
    return new Pair<>(total, String.join(lineSeparator, result));
  }

  private void showResultsStage(
      @NotNull final InputData.Parameters parameters,
      @NotNull final Pair<Double, String> res
  ) {

    Stage main = new Stage();
    main.setTitle("Results");
    main.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
      if (e.getCode() == KeyCode.ESCAPE ||
          e.getCode() == KeyCode.ENTER)
      {
        main.close();
      }
    });

    VBox pane = new VBox();
    pane.setPadding(new Insets(5));
    pane.setAlignment(Pos.CENTER);
    pane.setSpacing(5);

    TextArea area = new TextArea();
    area.setPrefRowCount(30);
    area.setEditable(false);
    area.setText(res.getValue());
    area.setFont(getFontForArea());

    Button saveButton = new Button("Save as");
    Button closeButton = new Button("Close");

    saveButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> showSaveStageAction(parameters, main, area));
    closeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> main.close());

    String totalString = String.valueOf(res.getKey());
    if (totalString.contains("E") && prettyExponents) {
      totalString = totalString.replace("E", " * (10^") + ")";
    }

    area.appendText(lineSeparator + "Total: " + totalString);

    HBox buttons = new HBox();
    buttons.setAlignment(Pos.CENTER);
    buttons.setPadding(new Insets(5));
    buttons.setSpacing(5);
    buttons.getChildren().addAll(saveButton, closeButton);

    pane.getChildren().addAll(area, buttons);

    main.setScene(new Scene(pane));
    main.show();
  }

  static void displayAlert(@NotNull String message) {
    Stage stage = new Stage();
    VBox main = new VBox();

    main.setPadding(new Insets(5));
    main.setSpacing(5);

    main.getChildren().add(new Label(message));

    Button okButton = new Button("Dismiss");

    okButton.addEventHandler(
        MouseEvent.MOUSE_CLICKED,
        event -> stage.close()
    );
    main.getChildren().add(okButton);

    stage.setScene(new Scene(main));
    stage.show();
  }

  @NotNull
  private static Font getFontForArea() {
    String fontFamily = Font.getFamilies().contains("SF Mono") ? "SF Mono Medium" : "Courier New Bold";
    int fontSize = fontFamily.equals("SF Mono Medium") ? 12 : 13;
    return new Font(fontFamily, fontSize);
  }

  private void showSaveStageAction(final @NotNull InputData.Parameters parameters, final Stage main, final TextArea area) {
    Stage nstage = new Stage();
    nstage.setTitle("Save Options");

    VBox mainpane = new VBox();
    mainpane.setSpacing(10);
    mainpane.setPadding(new Insets(10));

    Label label = new Label("Save Options");
    label.setFont(new Font("Arial Bold", 14));

    CheckBox includeBox = new CheckBox("Include Initial Parameters?");

    ToggleGroup toggleGroup = new ToggleGroup();

    RadioButton textButton = new RadioButton("Plain Text Format");
    textButton.setSelected(true);
    textButton.setToggleGroup(toggleGroup);

    RadioButton excelButton = new RadioButton("Excel Format");
    excelButton.setToggleGroup(toggleGroup);

    Button closeButton1 = new Button("Go");

    closeButton1.addEventHandler(MouseEvent.MOUSE_CLICKED, event1 -> {
      nstage.close();
      performSave(main, area.getText().replace("\n", lineSeparator),
                  excelButton.isSelected() ? SaveType.EXCEL_TYPE : SaveType.TEXT_TYPE,
                  includeBox.isSelected(), parameters
      );
    });

    mainpane.getChildren().addAll(label, includeBox, textButton,
                                  excelButton, closeButton1);

    nstage.setScene(new Scene(mainpane));
    nstage.show();
  }

  /**
   * Writes an appropriate file (decided by SaveType type parameter) using the
   * results of the pressure gradient calculation. The last 4 parameters are passed
   * as the initial conditions of the calculation and can optionally be saved.
   * They will be ignored if {@code withParameters} is false and written otherwise
   * If {@code withParameters} is false, these values can be 0'd out
   *
   * @param usingWindow    The root window which will display the {@link FileChooser} pane
   * @param usingText      The text output to be used in the file, generated by
   *                       {@link #calculateMainSequence(InputData.Parameters)}
   * @param type           Used to determine if the file output is a {@code .txt} or {@code .csv} file
   * @param withParameters if true, initial conditions of the calculation will be written to the file as well
   * @param parameters     the parameters used to calculated the function value ignored if {@code withParameters} is false
   */
  private void performSave(
      Window usingWindow,
      @NotNull String usingText,
      SaveType type,
      boolean withParameters,
      InputData.Parameters parameters
  ) {
    String extension;
    if (type == SaveType.TEXT_TYPE) {
      extension = ".txt";
    } else if (type == SaveType.EXCEL_TYPE) {
      extension = ".csv";
      usingText = formatTextForCSV(usingText);
    } else {
      throw new RuntimeException("Unexpected SaveType value for enum");
    }

    File saveFile = promptForSaveFile(usingWindow, extension);
    if (saveFile == null) {
      return;
    }

    try {
      BufferedWriter br = new BufferedWriter(new FileWriter(saveFile));
      if (withParameters) {
        writeParameters(br, parameters);
      }
      br.write(usingText);
      br.close();
    } catch (IOException e) {
      displayAlert("File could not be saved" + lineSeparator + e.getMessage());
    }
  }

  @NotNull
  private static String formatTextForCSV(@NotNull String usingText) {
    usingText = usingText.replace("Total: ", "");
    String[] rawLines = usingText.split(lineSeparator);
    String[] csvLines = new String[rawLines.length - 1];
    for (int i = 0; i < rawLines.length - 1; i++) {
      csvLines[i] = "\"" + rawLines[i] + "\"" + ",";
    }
    usingText = String.join(lineSeparator, csvLines);
    usingText += lineSeparator + "Total," + rawLines[rawLines.length - 1];
    return usingText;
  }

  @Nullable
  private File promptForSaveFile(@Nullable final Window usingWindow, @NotNull final String extension) {
    FileChooser fc = new FileChooser();
    fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(extension.toUpperCase() + " files", "*" + extension));
    fc.setInitialFileName("untitled" + extension);
    File saveFile = fc.showSaveDialog(usingWindow);
    if (saveFile == null) {
      return null;
    }
    if (!saveFile.getName().contains(extension)) {
      saveFile = new File(saveFile.getAbsolutePath() + extension);
    }
    return saveFile;
  }

  private void writeParameters(final BufferedWriter br, final InputData.Parameters parameters) throws IOException {
    br.write("Sections: " + parameters.getLimit());
    br.write(lineSeparator + "Steps: " + parameters.getSteps());
    br.write(lineSeparator + "Radius: " + parameters.getRadius());
    br.write(lineSeparator + "Density: " + parameters.getDensity());
    br.write(lineSeparator + ",," + lineSeparator);
  }

  public void helpAction() {
    new HelpDialog();
  }

  private enum SaveType {EXCEL_TYPE, TEXT_TYPE}
}
