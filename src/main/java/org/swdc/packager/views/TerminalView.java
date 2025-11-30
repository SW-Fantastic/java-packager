package org.swdc.packager.views;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBase;
import org.swdc.fx.font.FontSize;
import org.swdc.fx.font.Fontawsome5Service;
import org.swdc.fx.view.AbstractView;
import org.swdc.fx.view.View;
import org.swdc.packager.core.PackageProject;

@View(viewLocation = "views/main/TerminalView.fxml")
public class TerminalView extends AbstractView {

    @Inject
    private Fontawsome5Service fontawsome5Service;

    private SimpleBooleanProperty running = new SimpleBooleanProperty(false);

    @PostConstruct
    public void init() {

        setIcon(findById("btnOpen"), "folder-open");
        setIcon(findById("btnRun"), "play");
        running.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                setIcon(findById("btnRun"), "stop");
            } else {
                setIcon(findById("btnRun"), "play");
            }
        });

    }

    private void setIcon(ButtonBase buttonBase, String icon) {
        buttonBase.setPadding(new Insets(0, 0, 0, 0));
        buttonBase.setFont(fontawsome5Service.getSolidFont(FontSize.SMALL));
        buttonBase.setText(fontawsome5Service.getFontIcon(icon));
    }

    public void show(PackageProject project) {
        TerminalController controller = getController();
        controller.setProject(project);
        super.show();
    }

    @Override
    public void show() {
        throw new RuntimeException("using show(PackageProject project) instead");
    }

    public void setRunning(boolean running) {
        this.running.set(running);
    }

    public boolean isRunning() {
        return running.get();
    }
}
