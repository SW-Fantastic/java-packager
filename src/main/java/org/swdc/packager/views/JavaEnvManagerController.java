package org.swdc.packager.views;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import org.swdc.dependency.annotations.EventListener;
import org.swdc.fx.view.ViewController;
import org.swdc.packager.core.builder.JavaVersionExtractor;
import org.swdc.packager.core.entity.JavaEnvironment;
import org.swdc.packager.core.service.JavaBuildService;
import org.swdc.packager.views.event.JavaEnvRefreshEvent;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

@Singleton
public class JavaEnvManagerController extends ViewController<JavaEnvManageView> {

    @Inject
    private JavaBuildService javaBuildService;

    @FXML
    private TableView<JavaEnvironment> javaenvTable;

    @FXML
    private TableColumn<JavaEnvironment, String> columnPath;

    @FXML
    private TableColumn<JavaEnvironment, String> columnVersion;

    @Override
    protected void viewReady(URL url, ResourceBundle resourceBundle) {
        refresh(null);
        columnPath.setCellValueFactory(new PropertyValueFactory<>("path"));
        columnVersion.setCellValueFactory(new PropertyValueFactory<>("version"));
    }

    @EventListener(type = JavaEnvRefreshEvent.class)
    public void refresh(JavaEnvRefreshEvent event) {
        Platform.runLater(() -> {
            ObservableList<JavaEnvironment> items = javaenvTable.getItems();
            items.clear();
            items.addAll(javaBuildService.getAllJavaEnvironments());
        });
    }

    @FXML
    public void addJavaEnv() {

        DirectoryChooser chooser = new DirectoryChooser();
        File selected = chooser.showDialog(getView().getStage());
        if (selected == null || !selected.exists()) {
            return;
        }

        JavaVersionExtractor extractor = new JavaVersionExtractor(selected);
        String version = extractor.extractVersion();
        if (version == null || version.isBlank()) {
            // 无法解析Java版本，可能是Javac调用失败了。
            return;
        }
        JavaEnvironment environment = new JavaEnvironment();
        environment.setVersion(version);
        environment.setPath(selected.getAbsolutePath());
        javaBuildService.saveJavaEnvironment(environment);

    }

    @FXML
    public void removeJavaEnv() {
        JavaEnvironment selected = javaenvTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        javaBuildService.removeJavaEnvironment(selected.getId());
    }

}
