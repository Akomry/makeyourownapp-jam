package fr.emiko.net;

import org.json.JSONObject;

public class Event {
    public static final String AUTH = "AUTH";
    public static final String LSTLINE = "LSTLINE";
    public static final String ADDLINE = "ADDLINE";
    public static final String DELLINE = "DELLINE";
    public static final String LINE = "LINE";
    public static final String LINELST = "LINELST";
    public static final String ADDCANVAS = "ADDCANVAS";
    public static final String CNVS = "CNVS";

    private String type;
    private JSONObject content;

    public Event(String type, JSONObject content) {
        this.type = type;
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public JSONObject getContent() {
        return content;
    }

    public JSONObject toJSONObject() {
        return new JSONObject()
                .put("type", type)
                .put("content", content);
    }

    public String toJSON() {
        return toJSONObject().toString();
    }

    public static Event fromJSON(String obj) {
        JSONObject jobj = new JSONObject(obj);
        String type = jobj.getString("type");
        JSONObject content = jobj.getJSONObject("content");
        return new Event(type, content);
    }

    @Override
    public String toString() {
        return this.toJSON();
    }
}
