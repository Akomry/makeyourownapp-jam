package fr.emiko.graphicsElement;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.UUID;
import java.util.Vector;

public class Line extends Vector<Stroke> {
    private int timestamp;

    public JSONObject toJSONObject() {
        JSONArray jsonArray = new JSONArray();
        for (Stroke stroke: this) {
            jsonArray.put(stroke.toJSON());
        }
        return new JSONObject().put("line", jsonArray).put("timestamp", timestamp);
    }

    public static Line fromJSONArray(JSONArray jsonArray) {
        Line line = new Line();
        for (int i = 0; i < jsonArray.length(); i++) {
            line.add(Stroke.fromJSON(jsonArray.getString(i)));
        }
        return line;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }
}
