package org.swdc.packager.views.cell;

import javafx.beans.Observable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;

import java.util.function.Function;


public class TextEditableCell<T> extends TableCell<T,String> {

    private TextField field;

    private HBox labelGraph;

    private Label label;

    private TextEditListener<T> cellEditListener = null;

    private Function<T, String> getter;

    public TextEditableCell(TextEditListener<T> editListener, Function<T, String> getter) {
        field = new TextField();
        field.setOnAction(e -> {
            commitEdit(field.getText());
            e.consume();
        });
        field.focusedProperty().addListener(e -> {
            if (!field.isFocused() && this.isEditing()) {
                commitEdit(field.getText());
            }
        });
        field.setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
                t.consume();
            }
        });
        this.cellEditListener = editListener;
        this.getter = getter;

        this.label = new Label();

        this.labelGraph = new HBox();
        this.labelGraph.setAlignment(Pos.CENTER_LEFT);
        this.labelGraph.setPadding(new Insets(4));
        this.labelGraph.getChildren().add(label);
        this.labelGraph.getStyleClass().add("field-border");
    }

    private void changed(Observable observable) {
        TableRow<T> row = getTableRow();
        if (row == null || row.getItem() == null) {
            field.setText("");
            return;
        }
        T item = row.getItem();
        cellEditListener.changed(item,this.field.getText());
    }

    @Override
    public void startEdit() {
        super.startEdit();
        if (!isEditable()) {
            return;
        }
        setGraphic(field);
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setGraphic(labelGraph);
    }

    @Override
    public void commitEdit(String newValue) {
        super.commitEdit(newValue);
        setGraphic(labelGraph);
        cellEditListener.changed(getTableRow().getItem(), newValue);
        label.setText(newValue);
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {
            field.setText(getter.apply(getTableRow().getItem()));
            label.setText(getter.apply(getTableRow().getItem()));
            if (isEditing()) {
                setGraphic(field);
            } else {
                setGraphic(labelGraph);
            }
        }
    }
}
