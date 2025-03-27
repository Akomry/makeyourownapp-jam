package fr.emiko.graphicsElement;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import org.json.JSONObject;

import java.awt.*;
import java.util.Objects;

public class Stroke {
    private final double fromX;
    private final double fromY;
    private final double toX;
    private final double toY;
    private final double brushSize;
    private final Color color;

    public Stroke (double fromX, double fromY, double toX, double toY, double brushSize, Color color) {
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
        this.brushSize = brushSize;
        this.color = color;
    }

    public static Stroke fromJSON(String jsonStroke) {
        JSONObject jsonObject = new JSONObject(jsonStroke);
        return new Stroke(
                jsonObject.getDouble("fromX"),
                jsonObject.getDouble("fromY"),
                jsonObject.getDouble("toX"),
                jsonObject.getDouble("toY"),
                jsonObject.getDouble("brushSize"),
                Color.valueOf(jsonObject.get("color").toString())
        );
    }

    public void draw (GraphicsContext g, javafx.scene.paint.Color color) {
        g.setStroke(color);
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
//        return "Stroke{fromX=%f, fromY=%f, toX=%f, toY=%f, brushSize=%f}".formatted(fromX, fromY, toX, toY, brushSize);
        return this.toJSON();
    }

    public JSONObject toJSONObject() {
        return new JSONObject()
                .put("fromX", fromX)
                .put("fromY", fromY)
                .put("toX", toX)
                .put("toY", toY)
                .put("brushSize", brushSize)
                .put("color", color);
    }

    public String toJSON() {
        return toJSONObject().toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stroke stroke = (Stroke) o;
        return Double.compare(fromX, stroke.fromX) == 0 && Double.compare(fromY, stroke.fromY) == 0 && Double.compare(toX, stroke.toX) == 0 && Double.compare(toY, stroke.toY) == 0 && Double.compare(brushSize, stroke.brushSize) == 0;
    }

    public Color getColor() {
        return this.color;
    }
}
