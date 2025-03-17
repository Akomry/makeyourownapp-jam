package fr.emiko.graphicalapp;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import fr.emiko.graphicsElement.Stroke;

import java.awt.*;
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
    private double posX = 0;
    private double posY = 0;
    private Vector<Stroke> strokes = new Vector<>();
    private Vector<Stroke> lastSaved = new Vector<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        drawingCanvas.setOnMouseDragged(this::printLine);
        drawingCanvas.setOnMouseClicked(this::resetPos);
        saveButton.setOnAction(this::onActionSave);
        loadButton.setOnAction(this::onActionLoad);
        newCanvasButton.setOnAction(this::onActionCreateCanvas);
        scrollPane.setOnScroll(this::onScrollZoom);
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

            drawingCanvas.setScaleX(drawingCanvas.getScaleX() * scaleFactor);
            drawingCanvas.setScaleY(drawingCanvas.getScaleY() * scaleFactor);
            scrollPane.setFitToHeight(true);
            scrollPane.setFitToWidth(true);

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
    }

    private void printLine(MouseEvent mouseEvent) {
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
//        gc.setFill(Color.BLACK);
//        gc.setStroke(Color.BLACK);
//        gc.beginPath();
//        if (posX == 0 || posY == 0) {
//            posX = mouseEvent.getX();
//            posY = mouseEvent.getY();
//        }
//        gc.moveTo(posX, posY);
//        gc.lineTo(mouseEvent.getX(), mouseEvent.getY());
//        gc.closePath();
//        gc.stroke();
//        posX = mouseEvent.getX();
//        posY = mouseEvent.getY();

        if (posX == 0 || posY == 0) {
            posX = mouseEvent.getX();
            posY = mouseEvent.getY();
        }

        Stroke stroke = new Stroke(posX, posY, mouseEvent.getX(), mouseEvent.getY());
        strokes.add(stroke);
        stroke.draw(gc);

        posX = mouseEvent.getX();
        posY = mouseEvent.getY();

        System.out.println(stroke);

    }

}