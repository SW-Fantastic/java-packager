package org.swdc.packager.views;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import org.swdc.fx.FXResources;
import org.swdc.fx.view.ViewController;
import org.swdc.packager.core.LangKeys;
import org.swdc.packager.core.PackageProject;
import org.swdc.packager.core.builder.JavaExecuteLauncher;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class TerminalController extends ViewController<TerminalView> {

    private File workingDirectory;

    private PackageProject project;

    private JavaExecuteLauncher javaExecuteLauncher;

    @Inject
    private FXResources resources;

    @FXML
    private TextField workingDirectoryField;

    @FXML
    private TextFlow terminalTextArea;

    @FXML
    private ScrollPane terminalScrollPane;

    @FXML
    private void selectWorkingDirectory() {

        ResourceBundle bundle = resources.getResourceBundle();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(bundle.getString(LangKeys.SELECT_WORKING_DIR));
        File selectedDirectory = directoryChooser.showDialog(getView().getStage());
        if (selectedDirectory != null) {
            workingDirectory = selectedDirectory;
            workingDirectoryField.setText(selectedDirectory.getAbsolutePath());
        }
    }

    @FXML
    private void runCommand() {
        if (project == null) {
            return;
        }
        if (workingDirectory == null) {
            selectWorkingDirectory();
            if (workingDirectory == null) {
                return;
            }
        }
        if (this.javaExecuteLauncher != null) {
            this.javaExecuteLauncher.stop();
            this.javaExecuteLauncher = null;
            Platform.runLater(() -> {
                getView().setRunning(false);
            });
            return;
        }
        terminalTextArea.getChildren().clear();
        this.javaExecuteLauncher = new JavaExecuteLauncher(project, workingDirectory, str -> {
            Platform.runLater(() -> {
                terminalTextArea.getChildren().addAll(createNode(str ));
                terminalScrollPane.setVvalue(terminalScrollPane.getVmax());
            });
        });
        resources.getExecutor().submit(() -> {
            Platform.runLater(() -> {
                getView().setRunning(true);
            });
            javaExecuteLauncher.execute();
            javaExecuteLauncher =  null;
            Platform.runLater(() -> {
                getView().setRunning(false);
            });
        });
    }

    public List<Text> createNode(String ansi) {

        Pattern pattern = Pattern.compile("(\\u001B\\[[0-9;]*m)");
        Matcher matcher = pattern.matcher(ansi);
        List<Text> texts = new ArrayList<>();
        String style = "terminal-text";
        int pos = 0;
        while (matcher.find()) {

            int start = matcher.start();
            int end = matcher.end();
            String text = ansi.substring(start, end);
            text = text.replace("\u001B[", "")
                    .replace("m", "");
            String colorNext = getAnsiColor(text);

            Text textNode = new Text(ansi.substring(pos, start));
            textNode.getStyleClass().add(style);
            texts.add(textNode);

            style = colorNext;
            pos = end;

        }

        if (pos < ansi.length()) {
            Text textNode = new Text(ansi.substring(pos));
            textNode.getStyleClass().add(style);
            texts.add(textNode);
        }

        if (!ansi.endsWith("\n")) {
            texts.add(new Text("\n"));
        }

        return texts;
    }

    private String getAnsiColor(String ansi) {
        if (ansi.indexOf(";") > 0) {
            ansi = ansi.substring(ansi.indexOf(";") + 1);
        }
        if (ansi.equals("31")) {
            return "terminal-red";
        } else if (ansi.equals("32")) {
            return "terminal-green";
        } else if (ansi.equals("33")) {
            return "terminal-yellow";
        } else if (ansi.equals("34")) {
            return "terminal-blue";
        } else if (ansi.equals("35")) {
            return "terminal-purple";
        } else if (ansi.equals("36")) {
            return "terminal-cyan";
        } else if (ansi.equals("37")) {
            return "terminal-white";
        }
        return "terminal-text";
    }

    public void setProject(PackageProject project) {
        workingDirectoryField.setText("");
        workingDirectory = null;
        this.project = project;
    }
}
