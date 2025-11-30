package org.swdc.packager.views;

import jakarta.annotation.PostConstruct;
import javafx.event.Event;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.swdc.fx.view.AbstractView;
import org.swdc.fx.view.View;

@View(
        viewLocation = "views/main/ProgressModal.fxml",
        title = "Progress",
        multiple = true,
        closeable = false,
        resizeable = false
)
public class ProgressModal extends AbstractView {

    private Label progressLabel;

    private ProgressBar progressBar;

    @PostConstruct
    public void initView() {

        getStage().setOnCloseRequest(Event::consume);
        progressLabel = findById("label");
        progressBar = findById("progress");

    }

    public void reset() {
        progressLabel.setText("");
        progressBar.setProgress(0);
    }

    public void update(String message, double progress) {

        progressLabel.setText(message);
        progressBar.setProgress(progress);

    }


}
