package fr.emiko.graphicalapp;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import net.synedra.validatorfx.Check;
import net.synedra.validatorfx.Validator;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewCanvasController implements Initializable {
    public TextField heightTextField;
    public TextField widthTextField;
    public Button createButton;
    public Button cancelButton;
    private double canvasWidth;
    private double canvasHeight;
    private boolean ok = false;
    private Validator validator = new Validator();
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        createButton.setOnAction(this::create);
        createButton.disableProperty().bind(validator.containsErrorsProperty());
        cancelButton.setOnAction(this::close);
        widthTextField.setOnAction(this::create);
        validator.createCheck()
                .decorates(createButton)
                .dependsOn("width", widthTextField.textProperty())
                .dependsOn("height", heightTextField.textProperty())
                .withMethod(this::checkWidthHeight)
                .immediate();
    }

    private void checkWidthHeight(Check.Context context) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher widthMatcher = pattern.matcher(widthTextField.getText());
        Matcher heightMatcher = pattern.matcher(heightTextField.getText());
        if (!widthMatcher.matches() || !heightMatcher.matches()) {
            context.error("Width and height fields must contain only numbers.");
        }
    }

    public double getCanvasWidth() {
        return canvasWidth;
    }

    public double getCanvasHeight() {
        return canvasHeight;
    }

    public boolean isOk() {
        return ok;
    }

    private void close(ActionEvent actionEvent) {
        ((Stage) createButton.getScene().getWindow()).close();
    }

    private void create(ActionEvent actionEvent) {
        this.ok = true;
        this.canvasWidth = Double.parseDouble(widthTextField.getText());
        this.canvasHeight = Double.parseDouble(heightTextField.getText());
        ((Stage) createButton.getScene().getWindow()).close();
    }
}
