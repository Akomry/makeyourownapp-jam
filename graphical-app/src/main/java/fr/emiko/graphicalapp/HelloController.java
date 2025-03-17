package fr.emiko.graphicalapp;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import fr.emiko.graphicsElement.Stroke;
import javafx.scene.robot.Robot;
import javafx.scene.transform.Scale;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Vector;

public class HelloController implements Initializable {
    public Canvas drawingCanvas;
    public MenuItem saveButton;
    public MenuItem loadButton;
    public MenuItem newCanvasButton;
    public Slider brushSizeSlider;
    public Slider zoomSlider;
    public ScrollPane scrollPane;
    public Label brushSizeLabel;
    public Pane pane;
    private double posX = 0;
    private double posY = 0;
    private Vector<Stroke> strokes = new Vector<>();
    private Vector<Stroke> lastSaved = new Vector<>();
    private Vector<Vector<Stroke>> lines = new Vector<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        drawingCanvas.setOnMouseDragged(this::printLine);
        drawingCanvas.setOnMouseClicked(this::resetPos);
        saveButton.setOnAction(this::onActionSave);
        loadButton.setOnAction(this::onActionLoad);
        newCanvasButton.setOnAction(this::onActionCreateCanvas);
        scrollPane.setOnScroll(this::onScrollZoom);
        scrollPane.getParent().setOnKeyPressed(this::onActionKeyPressed);
        brushSizeLabel.textProperty().bind(brushSizeSlider.valueProperty().asString());
        setupCanvas();
        scrollPane.prefViewportHeightProperty().bind(pane.layoutYProperty());
        scrollPane.prefViewportWidthProperty().bind(pane.layoutXProperty());
    }

    private void setupCanvas() {
        drawingCanvas.requestFocus();
        drawingCanvas.getGraphicsContext2D().setFill(Color.WHITE);
        drawingCanvas.getGraphicsContext2D().fillRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
        brushSizeSlider.setValue(1);
        drawingCanvas.setTranslateX(scrollPane.getWidth()/2);
        drawingCanvas.setTranslateY(scrollPane.getHeight()/2);
        scrollPane.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                onScrollZoom(event);
                event.consume();
            }});
    }

    private void onActionKeyPressed(KeyEvent keyEvent) {
        keyEvent.consume();
        if (keyEvent.isControlDown() && keyEvent.getCode().equals(KeyCode.Z)) {
            System.out.println("CTRL Z");
            System.out.println(lines);
            lines.remove(lines.lastElement());
            GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
            gc.setFill(Color.WHITE);
            gc.fillRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
            for (Vector<Stroke> strokeVector : lines) {
                for (Stroke stroke: strokeVector) {
                    stroke.draw(gc);
                    System.out.println(stroke);
                }
            }
        }
        if (keyEvent.isControlDown() && keyEvent.getCode().equals(KeyCode.Y)) {
            System.out.println("CTRL Y");
        }
    }

    private void onScrollZoom(ScrollEvent event) {

        event.consume();
        double SCALE_DELTA = 1.1;
        if (event.getDeltaY() == 0) {
            return;
        }
        if (event.isControlDown()) {
            double scaleFactor =
                    (event.getDeltaY() > 0) ? SCALE_DELTA : 1 / SCALE_DELTA;


            Scale newScale = new Scale();
            newScale.setX(drawingCanvas.getScaleX() * scaleFactor);
            newScale.setY(drawingCanvas.getScaleY() * scaleFactor);
            newScale.setPivotX(drawingCanvas.getScaleX());
            newScale.setPivotY(drawingCanvas.getScaleY());
            drawingCanvas.getTransforms().add(newScale);


            System.out.println(pane.getHeight());
            System.out.println(pane.getWidth());
            pane.setPrefHeight(pane.getHeight()*scaleFactor);
            pane.setPrefWidth(pane.getWidth()*scaleFactor);
        }
    }

    private void onActionCreateCanvas(ActionEvent actionEvent) {
    }

    private void onActionLoad(ActionEvent actionEvent) {
//        drawingCanvas.getGraphicsContext2D().drawImage(lastSaved, 0, 0);
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
        for (Stroke stroke : lastSaved) {
            stroke.draw(gc);
        }
        strokes = (Vector<Stroke>) lastSaved.clone();
    }

    private void onActionSave(ActionEvent actionEvent) {
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
        lastSaved = (Vector<Stroke>) strokes.clone();
        
    }

    private void resetPos(MouseEvent mouseEvent) {
        posX = 0;
        posY = 0;
        lines.add((Vector<Stroke>) strokes.clone());
        System.out.println(lines);
        strokes.clear();
    }

    private void printLine(MouseEvent mouseEvent) {
        if (mouseEvent.isPrimaryButtonDown()) {
            GraphicsContext gc = drawingCanvas.getGraphicsContext2D();

            if (posX == 0 || posY == 0) {
                posX = mouseEvent.getX();
                posY = mouseEvent.getY();
            }

            Stroke stroke = new Stroke(posX, posY, mouseEvent.getX(), mouseEvent.getY(), brushSizeSlider.getValue());
            strokes.add(stroke);
            stroke.draw(gc);

            posX = mouseEvent.getX();
            posY = mouseEvent.getY();
        } else if (mouseEvent.isSecondaryButtonDown()) {

        }
    }

}