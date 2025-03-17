module fr.emiko.graphicalapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires org.controlsfx.controls;


    opens fr.emiko.graphicalapp to javafx.fxml;
    exports fr.emiko.graphicalapp;
}