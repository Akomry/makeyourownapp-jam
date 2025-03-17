package fr.emiko.graphicsElement;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Path;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

import java.awt.*;

public class Stroke {
    private final double fromX;
    private final double fromY;
    private final double toX;
    private final double toY;
    private final double brushSize;

    public Stroke (double fromX, double fromY, double toX, double toY, double brushSize) {
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
        this.brushSize = brushSize;
    }

    public void draw (GraphicsContext g) {
        g.setStroke(javafx.scene.paint.Color.BLACK);
        g.setLineCap(StrokeLineCap.ROUND);
        g.setMiterLimit(1);
        g.setLineWidth(brushSize);
        g.setLineJoin(StrokeLineJoin.ROUND);
        g.beginPath();
        g.moveTo(fromX, fromY);
        g.lineTo(toX, toY);
        g.closePath();
        g.stroke();
        g.fill();
    }

    @Override
    public String toString () {
        return "Stroke{fromX=%f, fromY=%f, toX=%f, toY=%f, brushSize=%f}".formatted(fromX, fromY, toX, toY, brushSize);
    }
}
