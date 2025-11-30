package org.swdc.packager.views;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBase;
import org.swdc.fx.font.FontSize;
import org.swdc.fx.font.Fontawsome5Service;
import org.swdc.fx.view.AbstractView;
import org.swdc.fx.view.View;
import org.swdc.packager.core.LangKeys;

@View(viewLocation = "views/main/PackagerMainView.fxml",title = LangKeys.MAIN_VIEW_TITLE)
public class PackagerMainView extends AbstractView {

    @Inject
    private Fontawsome5Service fontawsome5Service;

    @PostConstruct
    public void initView() {

        setupIcon(findById("btnOpen"), "folder-open");
        setupIcon(findById("btnSave"), "save");
        setupIcon(findById("btnNew"), "file-alt");
        setupIcon(findById("btnTestRun"), "play");
        setupIcon(findById("btnBuild"), "gavel");
        setupIcon(findById("btnParamAdd"), "plus");
        setupIcon(findById("btnParamRemove"), "minus");
        setupIcon(findById("btnAddFromFolder"), "folder-plus");
        setupIcon(findById("btnAddFromJar"), "plus");
        setupIcon(findById("btnTrashJar"), "minus");
        setupIcon(findById("btnClear"), "trash-alt");
        setupIcon(findById("btnAddNative"), "plus");
        setupIcon(findById("btnRemoveNative"), "minus");
        setupIcon(findById("btnConf"), "cog");

        getStage().setResizable(false);

    }

    private void setupIcon(ButtonBase button, String icon) {
        button.setFont(fontawsome5Service.getSolidFont(FontSize.SMALL));
        button.setPadding(new Insets(4));
        button.setText(fontawsome5Service.getFontIcon(icon));
    }

}
