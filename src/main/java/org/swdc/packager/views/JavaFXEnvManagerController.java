package org.swdc.packager.views;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import org.swdc.dependency.annotations.EventListener;
import org.swdc.fx.FXResources;
import org.swdc.fx.view.ViewController;
import org.swdc.packager.core.LangKeys;
import org.swdc.packager.core.entity.JavaEnvironment;
import org.swdc.packager.core.entity.JavaFXEnvironment;
import org.swdc.packager.core.service.JavaBuildService;
import org.swdc.packager.views.event.JavaFXEnvRefreshEvent;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Singleton
public class JavaFXEnvManagerController extends ViewController<JavaFXEnvManagerView> {

    @Inject
    private JavaBuildService javaBuildService;

    @Inject
    private FXResources resources;

    @FXML
    private TableView<JavaFXEnvironment> javafxEnvTable;

    @FXML
    private TableColumn<JavaFXEnvironment, String> columnName;

    @FXML
    private TableColumn<JavaFXEnvironment, String> columnPath;


    @Override
    protected void viewReady(URL url, ResourceBundle resourceBundle) {
        refresh(null);
        columnName.setCellValueFactory(new PropertyValueFactory<>("name"));
        columnPath.setCellValueFactory(new PropertyValueFactory<>("path"));
    }

    @EventListener(type = JavaFXEnvRefreshEvent.class)
    public void refresh(JavaFXEnvRefreshEvent refreshEvent) {

        Platform.runLater(() -> {
            List<JavaFXEnvironment> environments = javaBuildService.getAllJavaFXEnvironments();
            ObservableList<JavaFXEnvironment> items = javafxEnvTable.getItems();
            items.clear();
            items.addAll(environments);
        });

    }

    @FXML
    public void addJavaFXEnv() {

        ResourceBundle bundle = resources.getResourceBundle();

        DirectoryChooser chooser = new DirectoryChooser();
        File selected = chooser.showDialog(getView().getStage());
        if (selected == null || !selected.exists()) {
            return;
        }

        File fxBase = new File(selected, "javafx.base.jmod");
        File fxControls = new File(selected, "javafx.controls.jmod");
        File fxGraphics = new File(selected, "javafx.graphics.jmod");
        File fxFxml = new File(selected, "javafx.fxml.jmod");
        if (!fxBase.exists() || !fxControls.exists() || !fxGraphics.exists() || !fxFxml.exists()) {
            Alert alert = getView().alert(
                    bundle.getString(LangKeys.FAILED),
                    bundle.getString(LangKeys.FAILED_INVALID_JAVAFX),
                    Alert.AlertType.ERROR
            );
            alert.showAndWait();
            return;
        }

        JavaFXEnvironment environment = new JavaFXEnvironment();
        environment.setPath(selected.getAbsolutePath());
        environment.setName(selected.getName());
        javaBuildService.saveJavaFXEnvironment(environment);

    }

    @FXML
    public void removeJavaFXEnv() {
        JavaFXEnvironment selected = javafxEnvTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        javaBuildService.removeJavaFXEnvironment(selected.getId());
    }

}
