package fr.emiko.net;

import fr.emiko.graphicsElement.Line;

import java.util.Vector;

public class User {
    private String username;
    private String hashedPassword = "";
    private Vector<Line> lines = new Vector<Line>();
    private boolean connected;

    public User(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Vector<Line> getLines() {
        return lines;
    }

    public void setLines(Vector<Line> lines) {
        this.lines = lines;
    }
}
