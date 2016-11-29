package pgc;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Pair;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

public class Controller {
    public TextField limitField;
    public TextField radiusField;
    public TextField densityField;
    public TextField stepsField;
    public CheckBox prettyExponentBox;

    private boolean prettyExponents;

    public void goAction(ActionEvent actionEvent) {
        prettyExponents = prettyExponentBox.isSelected();
        String limits = limitField.getText();
        String radiuss = radiusField.getText();
        String densitys = densityField.getText();
        String stepss = stepsField.getText();

        int limit;
        long steps, radius;
        double density;

        if (limits.equals("")) {
            limit = 45;
        } else {
            limits = limits.replace(",", "");
            try {
                limit = Integer.parseInt(limits);
            } catch (NumberFormatException e) {
                displayAlert("Please enter a valid integer for limit");
                return ;
            }
        }

        if (radiuss.equals("")) {
            radius = 23193333;
        } else {
            radiuss = radiuss.replace(",", "");
            try {
                radius = Long.parseLong(radiuss);
            } catch (NumberFormatException e) {
                displayAlert("Please enter a valid long integer for radius");
                return ;
            }
        }

        if (stepss.equals("")) {
            steps = 45000000;
        } else {
            stepss = stepss.replace(",", "");
            try {
                steps = Long.parseLong(stepss);
            } catch (NumberFormatException e) {
                displayAlert("Please enter a valid long integer for steps");
                return ;
            }
        }

        if (densitys.equals("")) {
            density = 4.5;
        } else {
            try {
                density = Double.parseDouble(densitys);
            }catch (NumberFormatException e) {
                displayAlert("Please enter a valid double precision floating point number for density");
                return;
            }
        }

        Stage main = new Stage();
        main.setTitle("Results");
        FlowPane pane = new FlowPane();
        pane.setPadding(new Insets(5));
        pane.setAlignment(Pos.TOP_CENTER);
        pane.setVgap(5);
        pane.setHgap(5);

        TextArea area = new TextArea();
        area.setPrefRowCount(30);
        Button saveButton = new Button("Save as");
        Button closeButton = new Button("Close");

        saveButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {

            Stage nstage = new Stage();
            nstage.setTitle("Save Options");
            VBox mainpane = new VBox();
            mainpane.setSpacing(10);
            mainpane.setPadding(new Insets(10));

            Label label = new Label("Save Options");
            label.setFont(new Font("Arial Bold", 14));

            CheckBox includeBox = new CheckBox("Include Initial Parameters?");

            RadioButton textButton = new RadioButton("Plain Text Format");
            RadioButton excelButton = new RadioButton("Excel Format");

            ToggleGroup toggleGroup = new ToggleGroup();
            textButton.setSelected(true);
            textButton.setToggleGroup(toggleGroup);
            excelButton.setToggleGroup(toggleGroup);

            Button closeButton1 = new Button("Go");

            closeButton1.addEventHandler(MouseEvent.MOUSE_CLICKED, event1 -> {
                nstage.close();
                performSave(main, area.getText(), excelButton.isSelected() ?
                  SaveType.EXCEL_TYPE : SaveType.TEXT_TYPE, includeBox.isSelected(),
                  limit, steps, radius, density);
            });

            mainpane.getChildren().addAll(label, includeBox, textButton,
              excelButton, closeButton1);

            nstage.setScene(new Scene(mainpane));
            nstage.show();
        });

        Pair<Double, String> res = calculateMainSequence(limit, radius, steps, density);
        area.setText(res.getValue());

        String totalString = String.valueOf(res.getKey());
        if (totalString.contains("E") && prettyExponents)
            totalString = totalString.replace("E", " * (10^") + ")";

        area.appendText("\nTotal: " + totalString);
        String fontFamily = Font.getFamilies().contains("SF Mono") ? "SF Mono Medium" : "Courier New Bold";
        int height = fontFamily.equals("SF Mono Medium") ? 540 : 525;
        int fontSize = fontFamily.equals("SF Mono Medium") ? 12 : 13;
        area.setFont(new Font(fontFamily, fontSize));
        closeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> main.close());

        pane.getChildren().addAll(area, saveButton, closeButton);

        main.setScene(new Scene(pane, 360, height));
        main.show();

    }

    private Pair<Double, String> calculateMainSequence(int limit, long radius, long steps, double density) {
        int basemass = 10;
        int exponent = 1;
        double total = 0;
        LinkedList<Double> pressures = new LinkedList<>();
        while (exponent <= limit) {
            Double pressure = (((6.6738 * (Math.pow(10, -11))) * (Math.pow(basemass, exponent))) / ((radius << 1))) * density;
            pressures.add(pressure);
            total += pressure;
            exponent += 1;
            radius += steps;
        }

        LinkedList<String> result = new LinkedList<>();

        for (Double number: pressures) {
            String stringValue = String.valueOf(number);
            if (stringValue.contains("E") && prettyExponents)
                result.add(stringValue.replace("E", " * (10^") + ")");
            else
                result.add(stringValue);
        }
        return new Pair<>(total, String.join("\n", result));
    }

    private void performSave(Window usingWindow, String usingText, SaveType type,
                             boolean withParameters, int limit, long steps,
                             long radius, double density) {
        switch (type) {
            case TEXT_TYPE:
                FileChooser fc = new FileChooser();
                fc.setInitialFileName("untitled.txt");
                File saveFile = fc.showSaveDialog(usingWindow);
                if (saveFile == null)
                    return;
                try {
                    if (!saveFile.getName().contains(".txt"))
                        saveFile = new File(saveFile.getAbsolutePath() + ".txt");

                    if (saveFile.exists()) {
                        boolean doOverwrite = displayPrompt("The file " + saveFile.getName() + " already exists\n" +
                          "Do you want to overwrite it?", "Yes", "No");

                        if (!doOverwrite)
                            return;
                    }

                    BufferedWriter br = new BufferedWriter(new FileWriter(saveFile));
                    if (withParameters) {
                        br.write("Limit: " + limit);
                        br.write("\nSteps: " + steps);
                        br.write("\nRadius: " + radius);
                        br.write("\nDensity: " + density);
                        br.write("\n\n");
                    }
                    br.write(usingText);
                    br.close();
                } catch (IOException e) {
                    displayAlert("File does not exist. Save failed!");
                }
                break;

            case EXCEL_TYPE:
                usingText = usingText.replace("Total: ", "");
                String[] rawLines = usingText.split("\n");
                String[] csvLines = new String[rawLines.length];
                for (int i = 0; i < rawLines.length - 1; i++) {
                    csvLines[i] = "\"" + rawLines[i] + "\"" + ",";
                }

                fc = new FileChooser();
                fc.setInitialFileName("untitled.csv");
                saveFile = fc.showSaveDialog(usingWindow);
                if (saveFile == null)
                    return;
                try {
                    if (!saveFile.getName().contains(".csv"))
                        saveFile = new File(saveFile.getAbsolutePath() + ".csv");

                    if (saveFile.exists()) {
                        boolean doOverwrite = displayPrompt("The file " + saveFile.getName() + " already exists\n" +
                          "Do you want to overwrite it?", "Yes", "No");

                        if (!doOverwrite)
                            return;
                    }

                    BufferedWriter br = new BufferedWriter(new FileWriter(saveFile));
                    if (withParameters) {
                        br.write("Limit," + limit);
                        br.write("\nSteps," + steps);
                        br.write("\nRadius," + radius);
                        br.write("\nDensity," + density);
                        br.write("\n,,\n");
                    }
                    for (int i = 0; i < csvLines.length - 1; i++) {
                        br.write(csvLines[i] + "\n");
                    }
                    br.write("Total," + rawLines[rawLines.length - 1]);
                    br.close();
                } catch (IOException e) {
                    displayAlert("File does not exist. Save failed!");
                }
        }
    }

    private static boolean displayPrompt(String message, String confirmName, String refuseName) {
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


    private static void displayAlert(String message) {
        Stage stage = new Stage();
        VBox main = new VBox();

        main.setPadding(new Insets(5));
        main.setSpacing(5);

        main.getChildren().add(new Label(message));

        Button okButton = new Button("Dismiss");

        okButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> stage.close());
        main.getChildren().add(okButton);

        stage.setScene(new Scene(main));
        stage.show();
    }

    public void helpAction(ActionEvent actionEvent) {
        Stage stage = new Stage();
        GridPane main = new GridPane();

        stage.setTitle("Help");

        main.setAlignment(Pos.TOP_LEFT);
        main.setHgap(10);
        main.setVgap(10);
        main.setPadding(new Insets(10));

        Label l = new Label("Limit:");
        main.add(l, 0, 0);

        Label limitDescription = new Label("The limit is the limit of the \n" +
          "number of sections to divide the star into. For example\n" +
          "if you set it to the default of 45 the star would be divided \n" +
          "into 45 regions and the pressure gradient will be calculated\n" +
          "over 45 separate regions yielding 45 values");
        main.add(limitDescription, 1, 0);

        Label r = new Label("Start Radius:");
        Label radiusDescription = new Label("The radius is the starting radius\n" +
          "of the first of the sections of the star. By default it is 23 million\n" +
          "meters so the first section's pressure is calculated at 23 million \n" +
          "meters. Every iteration adds <steps> to the radius until it gets to the\n" +
          "end of the sun. In other words the entire radius of the star \n" +
          "should be equal to (limit * steps) + radius. The radius is in meters");

        main.add(r, 0, 1);
        main.add(radiusDescription, 1, 1);

        Label d = new Label("Density:");
        Label densityDescription = new Label("The average density over the \n" +
          "convective zone of the star in grams per cubic centimeter.");

        main.add(d, 0, 2);
        main.add(densityDescription, 1, 2);

        Label s = new Label("Steps:");
        Label stepsDescription = new Label("The steps number is a number in meters\n" +
          "Every step this number will be added to the radius and the pressure will\n" +
          "be calculated at that section of the sun. This number is added to the \n" +
          "start radius once an iteration for <limit> iterations. In other words\n" +
          "The pressure is caculated at radius <start radius> then <steps> is \n" +
          "added to the <start radius> and the pressure is calculated again\n" +
          "Then this repeats <limit> times and all results plus a total are output\n" +
          "to the screen.");

        main.add(s, 0, 3);
        main.add(stepsDescription, 1, 3);

        Label note = new Label("Note:");
        main.add(note, 0, 4);

        Label comma = new Label("You may use commas in numbers. Only density allows\n" +
          "precision beyond the decimal point. The rest are integers");

        main.add(comma, 1, 4);

        Button closeButton = new Button("Close");
        closeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                stage.close();
            }
        });

        main.add(closeButton, 1, 5);

        stage.setScene(new Scene(main));
        stage.show();
    }

    private enum SaveType { EXCEL_TYPE, TEXT_TYPE }
}
