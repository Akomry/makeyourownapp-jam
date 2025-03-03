module fr.emiko.graphicalapp {
    requires javafx.controls;
    requires javafx.fxml;


    opens fr.emiko.graphicalapp to javafx.fxml;
    exports fr.emiko.graphicalapp;
}