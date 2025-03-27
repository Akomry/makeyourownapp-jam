package fr.emiko.graphicalapp;

import fr.emiko.graphicsElement.Line;
import fr.emiko.graphicsElement.layerListViewCell;
import fr.emiko.net.DrawClient;
import fr.emiko.net.DrawServer;
import fr.emiko.net.Event;
import fr.emiko.net.User;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import fr.emiko.graphicsElement.Stroke;
import javafx.scene.robot.Robot;
import javafx.scene.transform.Scale;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HelloController implements Initializable {
    private final Pattern hostPortPattern = Pattern.compile("^([-.a-zA-Z0-9]+)(?::([0-9]{1,5}))?$");
    public Canvas drawingCanvas;
    public MenuItem saveButton;
    public MenuItem loadButton;
    public MenuItem newCanvasButton;
    public Slider brushSizeSlider;
    public ScrollPane scrollPane;
    public Label brushSizeLabel;
    public Pane pane;
    public MenuItem hostButton;
    public MenuItem joinButton;
    public MenuItem disconnectButton;
    public SplitPane mainPane;
    public MenuItem stopHostButton;
    public ColorPicker colorPicker;
    public ListView<Canvas> layerListView;
    public Button addLayerButton;
    public Button removeLayerButton;
    private double posX = 0;
    private double posY = 0;
    private double mouseX = 0;
    private double mouseY = 0;
    private Vector<Stroke> strokes = new Vector<>();
    private Vector<Line> lastSaved = new Vector<>();
    private Vector<Line> lines = new Vector<>();
    private User user;
    private boolean connected;
    private DrawClient client;
    private ToggleButton hostButtonToggle = new ToggleButton();
    private DrawServer server;
    private ObservableList<Canvas> layerObservableList = FXCollections.observableArrayList();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        saveButton.setOnAction(this::onActionSave);
        loadButton.setOnAction(this::onActionLoad);
        newCanvasButton.setOnAction(this::onActionCreateCanvas);
        scrollPane.setOnScroll(this::onScrollZoom);
        scrollPane.setOnKeyPressed(this::onActionKeyPressed);
        brushSizeLabel.textProperty().bind(brushSizeSlider.valueProperty().asString());
        setupCanvas(drawingCanvas);
        scrollPane.prefViewportHeightProperty().bind(pane.layoutYProperty());
        scrollPane.prefViewportWidthProperty().bind(pane.layoutXProperty());

        stopHostButton.setOnAction(this::onActionStopHost);
        hostButton.setOnAction(this::onActionHost);
        joinButton.setOnAction(this::onActionJoin);
        disconnectButton.setOnAction(this::onActionDisconnect);

        newCanvasButton.disableProperty().bind(hostButtonToggle.selectedProperty().not());
        stopHostButton.disableProperty().bind(hostButtonToggle.selectedProperty().not());
        disconnectButton.disableProperty().bind(hostButtonToggle.selectedProperty().not());
        hostButtonToggle.setSelected(false);
        mainPane.disableProperty().bind(hostButtonToggle.selectedProperty().not());

        layerListView.setCellFactory(layerListView -> new layerListViewCell());
        layerListView.setItems(layerObservableList);
        layerListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        //addLayerButton.setOnAction(this::onActionAddLayer);
        //removeLayerButton.setOnAction(this::onActionRemoveLayer);
        //layerListView.setOnMouseClicked(this::onActionSelectCanvas);
    }

    private void onActionSelectCanvas(MouseEvent mouseEvent) {
        layerListView.getSelectionModel().getSelectedItem().requestFocus();
        layerListView.getSelectionModel().getSelectedItem().toFront();
    }

    private void onActionRemoveLayer(ActionEvent actionEvent) {
        pane.getChildren().remove(layerListView.getSelectionModel().getSelectedItem());
        layerObservableList.remove(layerListView.getSelectionModel().getSelectedItem());
        layerListView.refresh();
        layerListView.getSelectionModel().select(layerObservableList.getFirst());
    }

    private void onActionAddLayer(ActionEvent actionEvent) {
        Canvas newLayer = new Canvas(
                layerListView.getSelectionModel().getSelectedItem().getWidth(),
                layerListView.getSelectionModel().getSelectedItem().getHeight()
        );
        pane.getChildren().add(newLayer);
        layerObservableList.addFirst(newLayer);
        layerListView.getSelectionModel().select(newLayer);
        setupCanvas(newLayer);
        layerListView.refresh();
    }

    private void onActionStopHost(ActionEvent actionEvent) {
        client.close();
        if (this.server != null) {
            try {
                server.close();
            } catch (IOException e) {
                showErrorDialog(e, "Could not close server instance");
            }
        }
        hostButtonToggle.setSelected(false);
    }

    private void onActionDisconnect(ActionEvent actionEvent) {
        client.close();
        hostButtonToggle.setSelected(false);
    }


    private void onActionJoin(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Join");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter distant address");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                Matcher matcher = hostPortPattern.matcher(result.get());
                matcher.matches();
                String host = matcher.group(1);
                String port = matcher.group(2);
                connectClient(host, port == null ? 8090 : Integer.parseInt(port));
                client.sendEvent(new Event(Event.LINELST, new JSONObject()));
            } catch (NumberFormatException e) {
                showErrorDialog(e, "Invalid distant address");
            } catch (IOException e) {
                showErrorDialog(e, "Could not connect to host");
            }
        }
    }

    private void onActionHost(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Host");
        dialog.setContentText("Which port do you want to use? (default: 8090)");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {

            try {
                server = new DrawServer(result.get().isEmpty() ? 8090 : Integer.parseInt(result.get()));
                Thread thread = new Thread(server::acceptClients);
                thread.setDaemon(true);
                thread.start();
                connectClient("localhost", result.get().isEmpty() ? 8090 : Integer.parseInt(result.get()));
            } catch (NumberFormatException | IOException e) {
                showErrorDialog(e, "Invalid port number");
            }
        }
    }

    private void connectClient(String host, int port) throws IOException {
        this.client = new DrawClient(host, port, this);
        hostButtonToggle.setSelected(true);
        client.sendAuthEvent(String.valueOf(new Random().nextInt()));
    }

    private void showErrorDialog(Exception ex, String context) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("An error occured!");
        alert.setHeaderText(null);
        alert.setContentText(context);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
    }

    private void setupCanvas(Canvas canvas) {
        canvas.requestFocus();
        canvas.getGraphicsContext2D().setFill(Color.WHITE);
        canvas.getGraphicsContext2D().fillRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
        brushSizeSlider.setValue(1);
//        canvas.setTranslateX(scrollPane.getWidth()/2);
//        canvas.setTranslateY(scrollPane.getHeight()/2);
        colorPicker.setValue(Color.BLACK);
        canvas.setOnMouseDragged(this::printLine);
        canvas.setOnMouseClicked(this::resetPos);

        layerListView.getSelectionModel().select(drawingCanvas);
        layerObservableList.add(drawingCanvas);
        layerListView.refresh();
        scrollPane.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                onScrollZoom(event);
                event.consume();
            }});
        BoxBlur blur = new BoxBlur();
        blur.setHeight(1);
        blur.setWidth(1);
        blur.setIterations(1);
        drawingCanvas.getGraphicsContext2D().setEffect(blur);
    }

    private void onActionKeyPressed(KeyEvent keyEvent) {
        keyEvent.consume();
        if (keyEvent.isControlDown() && keyEvent.getCode().equals(KeyCode.Z)) {
            System.out.println("CTRL Z");
            System.out.println(lines);
            System.out.println(lines);
            lines.remove(lines.lastElement());
            Canvas currentLayer = layerListView.getSelectionModel().getSelectedItem();
            GraphicsContext gc = currentLayer.getGraphicsContext2D();
            gc.setFill(Color.WHITE);
            gc.fillRect(0, 0, currentLayer.getWidth(), currentLayer.getHeight());
            gc.clearRect(0, 0, currentLayer.getWidth(), currentLayer.getHeight());
            gc.fill();
            for (Vector<Stroke> strokeVector : lines) {
                for (Stroke stroke: strokeVector) {
                    stroke.draw(gc, stroke.getColor());
                    //System.out.println(stroke);
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

            pane.setPrefHeight(pane.getHeight()*scaleFactor);
            pane.setPrefWidth(pane.getWidth()*scaleFactor);
        }
    }

    private void onActionCreateCanvas(ActionEvent actionEvent) {
        try {
            NewCanvasController controller = showNewStage("New canvas...", "new-canvas-view.fxml");

            if (controller.isOk()) {
                //drawingCanvas = new Canvas(controller.getCanvasWidth(), controller.getCanvasHeight());
                //setupCanvas();
                System.out.println(controller.getCanvasHeight());
                System.out.println(controller.getCanvasWidth());
                drawingCanvas.setWidth(controller.getCanvasWidth());
                drawingCanvas.setHeight(controller.getCanvasHeight());
                drawingCanvas.getGraphicsContext2D().setFill(Color.WHITE);
                drawingCanvas.getGraphicsContext2D().fillRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
                drawingCanvas.getGraphicsContext2D().fill();
                pane.setScaleX(1);
                pane.setScaleY(1);
                client.sendEvent(new Event(Event.ADDCANVAS, new JSONObject().put("width", drawingCanvas.getWidth()).put("height", drawingCanvas.getHeight())));
                System.out.println("New canvas created");
            }
        } catch (IOException ignored) {
        }

    }

    public <T> T showNewStage(String title, String fxmlFileName) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFileName));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(title);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.showAndWait();
        return fxmlLoader.getController();
    }

    private void onActionLoad(ActionEvent actionEvent) {
//        drawingCanvas.getGraphicsContext2D().drawImage(lastSaved, 0, 0);
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
        System.out.println(lastSaved.size());
        for (Vector<Stroke> strokeVector : lastSaved) {
            for (Stroke stroke: strokeVector) {
                stroke.draw(gc, colorPicker.getValue());
                System.out.println(stroke);
            }
        }
        strokes = (Vector<Stroke>) lastSaved.clone();
    }

    private void onActionSave(ActionEvent actionEvent) {
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
        lastSaved = (Vector<Line>) lines.clone();
        System.out.println(lastSaved.size());
    }

    private void resetPos(MouseEvent mouseEvent) {
        posX = 0;
        posY = 0;
        mouseX = 0;
        mouseY = 0;
        Line line = new Line();
        for (Stroke stroke: strokes) {
            line.add(stroke);
        }
        lines.add((Line) line.clone());
        System.out.println(lines.size());
        System.out.println(lines);
        System.out.println(new Event("ADDLINE", line.toJSONObject()));
        strokes.clear();

        client.sendEvent(new Event(Event.ADDLINE, line.toJSONObject()));
    }

    private void printLine(MouseEvent mouseEvent) {
        Canvas currentLayer = layerListView.getSelectionModel().getSelectedItem();
        if (mouseEvent.isPrimaryButtonDown()) {
            GraphicsContext gc = currentLayer.getGraphicsContext2D();

            if (posX == 0 || posY == 0) {
                posX = mouseEvent.getX();
                posY = mouseEvent.getY();
            }

            Stroke stroke = new Stroke(posX, posY, mouseEvent.getX(), mouseEvent.getY(), brushSizeSlider.getValue(), colorPicker.getValue());
            strokes.add(stroke);
            stroke.draw(gc, colorPicker.getValue());

            posX = mouseEvent.getX();
            posY = mouseEvent.getY();


        } else if (mouseEvent.isSecondaryButtonDown()) {
            GraphicsContext gc = currentLayer.getGraphicsContext2D();

            if (posX == 0 || posY == 0) {
                posX = mouseEvent.getX();
                posY = mouseEvent.getY();
            }

            Stroke stroke = new Stroke(posX, posY, mouseEvent.getX(), mouseEvent.getY(), brushSizeSlider.getValue(), colorPicker.getValue());
            strokes.add(stroke);
            stroke.draw(gc, Color.WHITE);

            posX = mouseEvent.getX();
            posY = mouseEvent.getY();
        }
    }

    public void handleEvent(Event event) {
        System.out.println("Received new event !:" + event.toJSON());
        String type = event.getType();
        switch (type) {
            case Event.LINE -> {
                doImportLine(event.getContent());
            }
            case Event.DELLINE -> {
                doDeleteLine(event.getContent());
            }
            case Event.CNVS -> {
                doAddCanvas(event.getContent());
            }
            default -> {}
        }
    }

    private void doAddCanvas(JSONObject content) {
        drawingCanvas.setWidth(content.getDouble("width"));
        drawingCanvas.setHeight(content.getDouble("height"));
        drawingCanvas.getGraphicsContext2D().setFill(Color.WHITE);
        drawingCanvas.getGraphicsContext2D().fillRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
        drawingCanvas.getGraphicsContext2D().fill();
        pane.setScaleX(1);
        pane.setScaleY(1);

        setupCanvas(drawingCanvas);
    }

    private void doDeleteLine(JSONObject content) {
        lines.remove(Line.fromJSONArray(content.getJSONArray("line")));

        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
        lines.sort(new Comparator<Line>() {
            @Override
            public int compare(Line o1, Line o2) {
                if (o1.getTimestamp() < o2.getTimestamp()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        for (Line line: lines) {
            for (Stroke stroke: line) {
                stroke.draw(gc, colorPicker.getValue());
            }
        }

    }

    private void doImportLine(JSONObject content) {
        Line importedLine = Line.fromJSONArray(content.getJSONArray("line"));
        this.lines.add(importedLine);
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
        lines.sort(new Comparator<Line>() {
            @Override
            public int compare(Line o1, Line o2) {
                if (o1.getTimestamp() < o2.getTimestamp()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        for (Stroke stroke: importedLine) {
            stroke.draw(gc, stroke.getColor());
        }
    }
}