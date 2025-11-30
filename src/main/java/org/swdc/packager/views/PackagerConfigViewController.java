package org.swdc.packager.views;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import org.slf4j.Logger;
import org.swdc.dependency.annotations.EventListener;
import org.swdc.fx.FXResources;
import org.swdc.fx.view.ViewController;
import org.swdc.packager.PackagerConfigure;
import org.swdc.packager.core.FileUtils;
import org.swdc.packager.core.LangKeys;
import org.swdc.packager.core.Language;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@Singleton
public class PackagerConfigViewController extends ViewController<PackagerConfView> {

    @Inject
    private FXResources resources;

    @Inject
    private Logger logger;

    @Inject
    private PackagerConfigure configure;

    @FXML
    private ComboBox<Language> langComboBox;

    @FXML
    private ComboBox<String> skinComboBox;

    @Override
    protected void viewReady(URL url, ResourceBundle resourceBundle) {
       reloadConfigItems();
    }

    @FXML
    private void saveChanges() {
        try {
            ResourceBundle bundle = resources.getResourceBundle();
            Language language = langComboBox.getSelectionModel().getSelectedItem();
            String skin = skinComboBox.getSelectionModel().getSelectedItem();
            if (!configure.getLanguage().equals("unavailable")) {
                configure.setLanguage(language.getLocal());
            }
            configure.setTheme(skin);
            configure.save();
            Alert alert = getView().alert(
                    bundle.getString(LangKeys.SUCCESS),
                    bundle.getString(LangKeys.SUCCESS_CONFIG_SAVE),
                    Alert.AlertType.INFORMATION
            );
            alert.showAndWait();
        } catch (Exception e) {
            logger.error("Failed to save configs.", e);
        }
    }

    private void reloadConfigItems() {
        try {

            InputStream supportedLangs = getClass().getModule().getResourceAsStream("lang/lang.json");
            List<Language> languages = FileUtils.loadAsList(supportedLangs, Language.class);
            langComboBox.getItems().addAll(languages);
            supportedLangs.close();

            for (Language lang : languages) {
                if (lang.getLocal().equals(configure.getLanguage())) {
                    langComboBox.getSelectionModel().select(lang);
                    break;
                }
            }

            if (configure.getLanguage().equals("unavailable")) {
                langComboBox.setDisable(true);
            }

            File assetFolder = new File(resources.getAssetsFolder(), "skin");
            File[] files = assetFolder.listFiles();
            List<String> skins = new ArrayList<>();

            for (File file : files) {
                File styleFile = new File(file, "stage.less");
                if (file.isDirectory() && styleFile.exists()) {
                    skins.add(file.getName());
                    if (file.getName().equals(configure.getTheme())) {
                        skinComboBox.getSelectionModel().select(file.getName());
                    }
                }
            }
            skinComboBox.getItems().addAll(skins);

        } catch (Exception e) {
            logger.error("Failed to load system resources.", e);
        }
    }
}
