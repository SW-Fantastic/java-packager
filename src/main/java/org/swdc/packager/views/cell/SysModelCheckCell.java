package org.swdc.packager.views.cell;

import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import org.swdc.packager.core.JavaSysModule;

public class SysModelCheckCell extends TableCell<JavaSysModule, Void> {

    private HBox root;
    private CheckBox check;

    public SysModelCheckCell(TableColumn<JavaSysModule, Void> param) {
        check = new CheckBox();
        root = new HBox();
        root.getChildren().add(check);
        root.setAlignment(Pos.CENTER);
        root.maxWidthProperty().bindBidirectional(param.maxWidthProperty());
        root.prefWidthProperty().bindBidirectional(param.prefWidthProperty());
    }

    @Override
    protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            check.setOnAction(event -> {
                JavaSysModule module = getTableRow().getItem();
                module.setEnabled(check.isSelected());
            });
            check.setSelected(getTableRow().getItem().isEnabled());
            setGraphic(root);
        }
    }
}
