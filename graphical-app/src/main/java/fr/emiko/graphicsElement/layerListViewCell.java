package fr.emiko.graphicsElement;

import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class layerListViewCell extends ListCell<Canvas> {

    @Override
    protected void updateItem(Canvas item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {
            updateItem(item);
        }
    }


    private void updateItem(Canvas item) {
        ImageView imageView = new ImageView();
        imageView.setImage(item.snapshot(null, null));
        imageView.setFitHeight(25);
        imageView.setFitWidth(25);
        Text text = new Text(item.toString());
        HBox hbox = new HBox(imageView, text);
        hbox.setSpacing(10);
        hbox.setAlignment(Pos.CENTER_LEFT);
        setGraphic(hbox);
    }
}
