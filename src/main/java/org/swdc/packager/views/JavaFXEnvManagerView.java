package org.swdc.packager.views;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBase;
import javafx.stage.Stage;
import org.swdc.fx.font.FontSize;
import org.swdc.fx.font.Fontawsome5Service;
import org.swdc.fx.view.AbstractView;
import org.swdc.fx.view.View;
import org.swdc.packager.core.LangKeys;

@View(viewLocation = "views/main/JavaFXManageView.fxml", title = LangKeys.JAVAFX_SDK)
public class JavaFXEnvManagerView extends AbstractView {

    @Inject
    private Fontawsome5Service fontawsome5Service;

    @PostConstruct
    public void initView() {

        setupIcon(findById("iconAdd"), "plus");
        setupIcon(findById("iconRemove"), "minus");
        Stage stage = getStage();
        stage.setMinHeight(480);
        stage.setMinWidth(640);

    }

    private void setupIcon(ButtonBase button, String icon) {
        button.setFont(fontawsome5Service.getSolidFont(FontSize.SMALL));
        button.setPadding(new Insets(4));
        button.setText(fontawsome5Service.getFontIcon(icon));
    }


}
