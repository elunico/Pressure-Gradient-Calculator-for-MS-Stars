package com.tom.pgc;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
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
import java.util.concurrent.atomic.AtomicReference;

import static javafx.scene.input.KeyEvent.KEY_PRESSED;
import static javafx.scene.input.MouseEvent.MOUSE_CLICKED;
import static javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST;

public class Controller {
  static final String lineSeparator = System.lineSeparator();

  static void displayAlert(@NotNull String message) {
    Stage stage = new Stage();
    VBox main = new VBox();

    main.setPadding(new Insets(5));
    main.setSpacing(5);

    main.getChildren().add(new Label(message));

    Button okButton = new Button("Dismiss");

    okButton.addEventHandler(
        MOUSE_CLICKED,
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

  private static void augmentTextNode(final Node child, final double v) {
    if (child instanceof Labeled) {
      ((Labeled) child).setFont(Font.font(((Labeled) child).getFont().getSize() * v));
    }
    else if (child instanceof TextInputControl) {
      ((TextInputControl) child).setFont(Font.font(((TextInputControl) child).getFont().getSize() * v));
    }
    else if (child instanceof ScrollPane) {
      augmentTextNode(((ScrollPane) child).getContent(), v);
    }
    else if (child instanceof Parent) {
      augmentText((Parent) child, v);
    }
  }

  private static void augmentText(@NotNull final Parent parent, final double v) {
    if (parent instanceof ScrollPane) {
      augmentTextNode(((ScrollPane) parent).getContent(), v);
    }
    else {
      ObservableList<Node> childrenUnmodifiable = parent.getChildrenUnmodifiable();
      for (Node child : childrenUnmodifiable) {
        augmentTextNode(child, v);
      }
    }
  }

  public TextField limitField;
  public TextField radiusField;
  public TextField densityField;
  public TextField stepsField;
  public CheckBox prettyExponentBox;
  public FlowPane root;
  private AtomicReference<Double> currentZoom = new AtomicReference<>(1d);
  private boolean prettyExponents;

  public void increaseTextSize() {
    for (Stage s : Main.stages.keySet()) {
      augmentText(s.getScene().getRoot(), 1.2);
      Main.stages.computeIfPresent(s, (k, v) -> v * 1.2);
      s.getScene().getWindow().sizeToScene();
    }
    currentZoom.getAndUpdate(operand -> operand * 1.2);
  }

  public void decreaseTextSize() {
    for (Stage s : Main.stages.keySet()) {
      augmentText(s.getScene().getRoot(), 1 / 1.2);
      Main.stages.computeIfPresent(s, (k, v) -> v * (1 / 1.2));
      s.getScene().getWindow().sizeToScene();
    }
    currentZoom.getAndUpdate(operand -> operand * (1 / 1.2));
  }

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
      }
      else {
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
    Main.stages.put(main, 1.0);
    main.setTitle("Results");
    main.addEventHandler(KEY_PRESSED, e -> {
      if (e.getCode() == KeyCode.ESCAPE || e.getCode() == KeyCode.ENTER) {
        main.close();
      }
    });
    main.addEventHandler(WINDOW_CLOSE_REQUEST, e -> Main.stages.remove(main));

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

    saveButton.addEventHandler(MOUSE_CLICKED, event -> showSaveStageAction(parameters, main, area));
    closeButton.addEventHandler(MOUSE_CLICKED, event -> main.close());

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
    checkCurrentZoom(main);
    main.show();
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

    closeButton1.addEventHandler(MOUSE_CLICKED, event1 -> {
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
    switch (type) {
      case TEXT_TYPE:
        extension = ".txt";
        break;
      case EXCEL_TYPE:
        extension = ".csv";
        usingText = formatTextForCSV(usingText);
        break;
      default:
        throw new RuntimeException("Unexpected SaveType value for enum");
    }

    File saveFile = promptForSaveFile(usingWindow, extension);
    if (saveFile == null) {
      return;
    }

    try (BufferedWriter br = new BufferedWriter(new FileWriter(saveFile))) {
      if (withParameters) {
        writeParameters(br, parameters);
      }
      br.write(usingText);
    } catch (IOException e) {
      displayAlert("File could not be saved" + lineSeparator + e.getMessage());
    }
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
    HelpDialog h = new HelpDialog();
    h.setOnCloseRequest(event -> Main.stages.remove(h));
    Main.stages.put(h, 1.0);
    checkCurrentZoom(h);
    h.show();
  }

  private void checkCurrentZoom(final Stage stage) {
    System.out.println(currentZoom.get());
    System.out.println(Main.stages.get(stage));
    while (Main.stages.get(stage) > currentZoom.get()) {
      augmentText(stage.getScene().getRoot(), 1 / 1.2);
      Main.stages.compute(stage, (k, v) -> v == null ? 1 : v * (1 / 1.2));
    }
    while (Main.stages.get(stage) < currentZoom.get()) {
      augmentText(stage.getScene().getRoot(), 1.2);
      Main.stages.compute(stage, (k, v) -> v == null ? 1 : v * 1.2);
    }
  }

  public FlowPane getRoot() {
    return root;
  }

  public void setRoot(@NotNull final FlowPane root) {
    this.root = root;
  }

  private enum SaveType {EXCEL_TYPE, TEXT_TYPE}
}
