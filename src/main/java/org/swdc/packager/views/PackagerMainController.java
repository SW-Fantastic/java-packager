package org.swdc.packager.views;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.controlsfx.control.PopOver;
import org.slf4j.Logger;
import org.swdc.dependency.annotations.EventListener;
import org.swdc.fx.FXResources;
import org.swdc.fx.view.ViewController;
import org.swdc.packager.core.*;
import org.swdc.packager.core.builder.JavaModuleExtractor;
import org.swdc.packager.core.builder.JavaRuntimeLinker;
import org.swdc.packager.core.entity.JavaEnvironment;
import org.swdc.packager.core.entity.JavaFXEnvironment;
import org.swdc.packager.core.service.JavaBuildService;
import org.swdc.packager.views.cell.DepModelCheckCell;
import org.swdc.packager.views.cell.SysModelCheckCell;
import org.swdc.packager.views.cell.TextEditableCell;
import org.swdc.packager.views.event.JavaEnvRefreshEvent;
import org.swdc.packager.views.event.JavaFXEnvRefreshEvent;

import java.io.File;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class PackagerMainController extends ViewController<PackagerMainView> {

    @Inject
    private FXResources resources;

    @Inject
    private JavaEnvManageView javaEnvManager;

    @Inject
    private JavaFXEnvManagerView javaFxEnvManager;

    @Inject
    private JavaBuildService javaBuildService;

    @Inject
    private TerminalView terminalView;

    @Inject
    private PackagerConfView confView;

    @Inject
    private Logger logger;

    @FXML
    private ComboBox<JavaEnvironment> javaEnvComboBox;

    @FXML
    private ComboBox<JavaFXEnvironment> javaFxEnvComboBox;

    @FXML
    private TableView<JavaSysModule> tvSystemModule;

    @FXML
    private TableColumn<JavaSysModule,String> colSysModName;

    @FXML
    private TableColumn<JavaSysModule,Void> colSysModEnabled;

    @FXML
    private TableView<VMOptionPair> tableVmOptions;

    @FXML
    private TableColumn<VMOptionPair,String> colVmOptName;

    @FXML
    private TableColumn<VMOptionPair,String> colVmOptValue;

    @FXML
    private TableView<ModEntity> tableJars;

    @FXML
    private TableColumn<ModEntity,String> colJarName;

    @FXML
    private TableColumn<ModEntity,Boolean> colJarEnabled;

    @FXML
    private TableView<ModEntity> tableNativeLibs;

    @FXML
    private TableColumn<ModEntity,String> colNativeLibName;

    @FXML
    private TableColumn<ModEntity,Boolean> colNativeLibEnabled;

    @FXML
    private TableColumn<ModEntity,String> colNativeLibPath;

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtMainModule;

    @FXML
    private TextField txtMainClass;

    @FXML
    private CheckBox cbxNoConsole;

    @FXML
    private ImageView iconView;

    @FXML
    private Button btnConfig;

    private PopOver configPopOver;

    private File projectFile;

    private SimpleBooleanProperty supportModular = new SimpleBooleanProperty(false);

    private PackageProject packageProject = new PackageProject();

    @Override
    protected void viewReady(URL url, ResourceBundle resourceBundle) {

        onJavaEnvRefresh(null);
        onJavaFXEnvRefresh(null);

        colSysModName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colSysModEnabled.setCellFactory(SysModelCheckCell::new);

        colVmOptName.setCellFactory(col -> new TextEditableCell<>(
                VMOptionPair::setOptName, VMOptionPair::getOptName
        ));
        colVmOptValue.setCellFactory(col -> new TextEditableCell<>(
                VMOptionPair::setOptValue, VMOptionPair::getOptValue
        ));

        colJarEnabled.setCellFactory(DepModelCheckCell::new);
        colJarName.setCellValueFactory(new PropertyValueFactory<>("fileName"));

        colNativeLibName.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        colNativeLibPath.setCellValueFactory(new PropertyValueFactory<>("originalFilePath"));
        colNativeLibEnabled.setCellFactory(DepModelCheckCell::new);

        javaEnvComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener(this::onJavaEnvChanged);

        File defaultIcon = new File(resources.getAssetsFolder(), "icon.png");
        if (defaultIcon.exists()) {
            iconView.setImage(new Image(defaultIcon.toURI().toString()));
        }

        configPopOver = new PopOver(btnConfig);
        configPopOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
        configPopOver.setAutoHide(true);
        configPopOver.setDetachable(false);
        configPopOver.setContentNode(confView.getView());
        btnConfig.setOnAction(e -> configPopOver.show(btnConfig));

        txtMainModule.disableProperty().bind(supportModular.not());
        javaFxEnvComboBox.disableProperty().bind(supportModular.not());

    }

    private void onJavaEnvChanged(ObservableValue<? extends JavaEnvironment> observable, JavaEnvironment oldValue, JavaEnvironment newValue) {

        JavaEnvironment environment = javaEnvComboBox.getSelectionModel().getSelectedItem();
        ObservableList<JavaSysModule> items = tvSystemModule.getItems();

        items.clear();
        if (environment == null) {
            return;
        }
        File file = new File(environment.getPath());
        if (!file.exists() || !file.isDirectory()) {
            return;
        }

        JavaModuleExtractor extractor = new JavaModuleExtractor(environment);
        List<String> moduleList = extractor.getModuleNames();

        List<JavaSysModule> modules = new ArrayList<>();
        if (moduleList == null || moduleList.isEmpty()) {
            // 这个JDK不支持JPMS
            supportModular.setValue(false);
            javaFxEnvComboBox.getSelectionModel().clearSelection();
            txtMainModule.setText("");
            return;
        }

        for (String module : moduleList) {
            List<String> selectedModules = packageProject.getSystemModules();
            JavaSysModule sysModule = new JavaSysModule(module, selectedModules != null && selectedModules.contains(module));
            modules.add(sysModule);
        }

        items.addAll(modules);
        tvSystemModule.layout();
        supportModular.setValue(true);
    }

    @EventListener(type = JavaFXEnvRefreshEvent.class)
    public void onJavaFXEnvRefresh(JavaFXEnvRefreshEvent event) {
        Platform.runLater(() -> {
            ObservableList<JavaFXEnvironment> items = javaFxEnvComboBox.getItems();
            List<JavaFXEnvironment> environments = javaBuildService.getAllJavaFXEnvironments();
            items.clear();
            items.addAll(environments);
        });
    }

    @EventListener(type = JavaEnvRefreshEvent.class)
    public void onJavaEnvRefresh(JavaEnvRefreshEvent event) {

        Platform.runLater(() -> {
            List<JavaEnvironment> environments = javaBuildService.getAllJavaEnvironments();
            ObservableList<JavaEnvironment> items = javaEnvComboBox.getItems();
            items.clear();
            items.addAll(environments);
        });

    }

    @FXML
    private void onNewProject() {

        packageProject = new PackageProject();
        projectFile = null;

        txtMainClass.setText("");
        txtMainModule.setText("");
        cbxNoConsole.setSelected(false);
        javaFxEnvComboBox.getSelectionModel().clearSelection();
        javaEnvComboBox.getSelectionModel().clearSelection();
        txtName.setText("");

        ObservableList<VMOptionPair> optionPairs = tableVmOptions.getItems();
        optionPairs.clear();

        ObservableList<ModEntity> jarItems = tableJars.getItems();
        jarItems.clear();

        ObservableList<ModEntity> nativeLibItems = tableNativeLibs.getItems();
        nativeLibItems.clear();

        File defaultIcon = new File(resources.getAssetsFolder(), "icon.png");
        if (defaultIcon.exists()) {
            iconView.setImage(new Image(defaultIcon.toURI().toString()));
        }

    }

    @FXML
    private void addVMOpts() {
        tableVmOptions.getItems().add(new VMOptionPair());
    }

    @FXML
    private void removeVMOpts() {
        VMOptionPair selectedItem = tableVmOptions.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return;
        }
        tableVmOptions.getItems().remove(selectedItem);
    }

    @FXML
    private void onOpen() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Project", "*.package.json"));
        File file = fileChooser.showOpenDialog(getView().getStage());
        if (file == null) {
            return;
        }

        ResourceBundle bundle = resources.getResourceBundle();

        projectFile = file;
        packageProject = FileUtils.load(file, PackageProject.class);
        javaEnvComboBox.getSelectionModel().select(packageProject.getJavaEnvironment());
        javaFxEnvComboBox.getSelectionModel().select(packageProject.getJavaFxEnvironment());

        File icon = new File(resources.getAssetsFolder(),"icon.png");
        if (!icon.exists()) {
            try {
                File defaultIcon = new File(resources.getAssetsFolder(),"icon.png");
                Files.copy(defaultIcon.toPath(), icon.toPath());
            } catch (Exception e) {
                Alert alert = getView().alert(
                        bundle.getString(LangKeys.FAILED),
                        bundle.getString(LangKeys.FAILED_LOAD_DEFAULT_ICON),
                        Alert.AlertType.ERROR
                );
                alert.showAndWait();
            }
        }
        iconView.setImage(new Image(icon.toURI().toString()));
        txtMainClass.setText(packageProject.getMainClass());
        txtMainModule.setText(packageProject.getMainModule());
        txtName.setText(packageProject.getName());

        ObservableList<VMOptionPair> optionPairs = tableVmOptions.getItems();
        optionPairs.clear();
        if (packageProject.getVmOptions() != null) {
            optionPairs.addAll(packageProject.getVmOptions());
        }

        ObservableList<ModEntity> jarItems = tableJars.getItems();
        jarItems.clear();
        if (packageProject.getDependencies() != null) {
            jarItems.addAll(packageProject.getDependencies());
        }
        tableJars.layout();

        ObservableList<ModEntity> nativeLibItems = tableNativeLibs.getItems();
        nativeLibItems.clear();
        if (packageProject.getNativeLibs() != null) {
            nativeLibItems.addAll(packageProject.getNativeLibs());
        }
        tableNativeLibs.layout();

    }

    @FXML
    private void onTestRun() {
        if (packageProject == null) {
            packageProject = new PackageProject();
        }
        if (projectFile == null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Project", "*.package.json"));
            projectFile = fileChooser.showSaveDialog(getView().getStage());
            if (projectFile == null) {
                return;
            }
            if (!projectFile.getName().endsWith(".package.json")) {
                projectFile = new File(projectFile.getAbsolutePath() + ".package.json");
            }
        }

        terminalView.show(this.packageProject);
    }

    @FXML
    private void onBuild() {

        if (packageProject == null) {
            packageProject = new PackageProject();
        }

        ResourceBundle bundle = resources.getResourceBundle();

        if (projectFile == null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Project", "*.package.json"));
            projectFile = fileChooser.showSaveDialog(getView().getStage());
            if (projectFile == null) {
                return;
            }
            if (!projectFile.getName().endsWith(".package.json")) {
                projectFile = new File(projectFile.getAbsolutePath() + ".package.json");
            }
        }

        String osName = System.getProperty("os.name").trim().toLowerCase();
        String executableSubfix = osName.startsWith("win") ? ".exe" : "";

        if (txtName.getText().isEmpty()) {
            Alert alert = getView().alert(
                    bundle.getString(LangKeys.FAILED),
                    bundle.getString(LangKeys.FAILED_BLANK_PROJECT_NAME),
                    Alert.AlertType.ERROR
            );
            alert.showAndWait();
            return;
        }

        if (txtMainClass.getText().isEmpty()) {
            Alert alert = getView().alert(
                    bundle.getString(LangKeys.FAILED),
                    bundle.getString(LangKeys.FAILED_NO_MAIN_CLASS),
                    Alert.AlertType.ERROR
            );
            alert.showAndWait();
            return;
        }

        if (!txtMainModule.getText().isBlank()) {
            JavaEnvironment environment = javaEnvComboBox.getSelectionModel().getSelectedItem();
            File linker = new File(environment.getPath(), "bin/jlink" + executableSubfix);
            if (!linker.exists()) {
                Alert alert = getView().alert(
                        bundle.getString(LangKeys.FAILED),
                        bundle.getString(LangKeys.FAILED_NOT_SUPPORT_MODULAR),
                        Alert.AlertType.ERROR
                );
                alert.showAndWait();
                return;
            }
        }

        if (tableJars.getItems().isEmpty()) {
            Alert alert = getView().alert(
                    bundle.getString(LangKeys.FAILED),
                    bundle.getString(LangKeys.FAILED_BLANK_LIBRARY_PATH),
                    Alert.AlertType.ERROR
            );
            alert.showAndWait();
            return;
        }

        if (!supportModular.getValue() && !txtMainModule.getText().isBlank()) {
            Alert alert = getView().alert(
                    bundle.getString(LangKeys.FAILED),
                    bundle.getString(LangKeys.FAILED_NOT_SUPPORT_MODULAR),
                    Alert.AlertType.ERROR
            );
            alert.showAndWait();
            return;
        }

        List<String> modules = tvSystemModule
                .getItems()
                .stream()
                .filter(JavaSysModule::isEnabled)
                .map(JavaSysModule::getName)
                .collect(Collectors.toList());

        JavaEnvironment javaEnv = javaEnvComboBox.getSelectionModel().getSelectedItem();
        JavaFXEnvironment javaFxEnv = javaFxEnvComboBox.getSelectionModel().getSelectedItem();

        packageProject.setSystemModules(modules);
        packageProject.setJavaEnvironment(javaEnv);
        packageProject.setJavaFxEnvironment(javaFxEnv);
        packageProject.setMainClass(txtMainClass.getText());
        packageProject.setMainModule(txtMainModule.getText());
        packageProject.setName(txtName.getText());
        packageProject.setNoConsole(cbxNoConsole.isSelected());
        packageProject.setDependencies(tableJars.getItems());
        packageProject.setVmOptions(tableVmOptions.getItems());
        packageProject.setNativeLibs(tableNativeLibs.getItems());
        FileUtils.save(packageProject, projectFile);

        ProgressModal modal = getView().getView(ProgressModal.class);
        modal.update(bundle.getString(LangKeys.BUILD_START), 0);
        modal.show();

        resources.getExecutor().submit(() -> {
            try {

                Platform.runLater(() -> {
                    getView().getView().setDisable(true);
                    modal.update(bundle.getString(LangKeys.BUILD_LOAD_DEP) , 0);
                });

                File dist = new File(projectFile.getParent(),"dist");
                File libs = new File(dist,"libs");
                if (!dist.exists()) {
                    dist.mkdir();
                }

                if (!libs.exists()) {
                    libs.mkdir();
                } else {
                    FileUtils.deleteFolder(libs);
                    libs.mkdir();
                }

                List<ModEntity> deps = new ArrayList<>();
                deps.addAll(tableJars.getItems());
                deps.addAll(tableNativeLibs.getItems());

                StringJoiner modulesName = new StringJoiner(",");
                int proceedDeps = 0;
                for (ModEntity mod : deps) {

                    try {
                        proceedDeps++;
                        double percent = (double)proceedDeps / deps.size();
                        Platform.runLater(() -> {
                            modal.update(bundle.getString(LangKeys.BUILD_LOAD_DEP) + mod.getFileName() , percent);
                        });
                        if (!mod.isEnabled()) {
                            continue;
                        }
                        File file = new File(mod.getOriginalFilePath());
                        if (!file.exists()) {
                            Platform.runLater(() -> {
                                Alert alert = getView().alert(
                                        bundle.getString(LangKeys.FAILED),
                                        bundle.getString(LangKeys.BUILD_FAILED_LOAD_DEP) + file.getAbsolutePath(),
                                        Alert.AlertType.ERROR
                                );
                                alert.showAndWait();
                                modal.hide();
                            });
                            return;
                        }
                        if (mod.getFileName().endsWith("jar")) {
                            if (supportModular.getValue()) {
                                ModuleFinder finder = ModuleFinder.of(file.toPath());
                                ModuleReference reference = finder.findAll().stream().findFirst().orElse(null);
                                if (reference != null) {
                                    String name = reference.descriptor().name();
                                    if (name != null) {
                                        modulesName.add(name);
                                    }
                                }
                            }
                            Files.copy(file.toPath(), new File(libs, file.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                        } else {
                            Files.copy(file.toPath(), new File(dist, file.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (Exception e) {
                        logger.error("Failed to copy dependency file: {}", mod.getOriginalFilePath(), e);
                        Platform.runLater(() -> {
                            Alert alert = getView().alert(
                                    bundle.getString(LangKeys.FAILED),
                                    bundle.getString(LangKeys.BUILD_FAILED_LOAD_DEP) + mod.getOriginalFilePath(),
                                    Alert.AlertType.ERROR
                            );
                            alert.showAndWait();
                            modal.hide();
                        });
                        return;
                    }
                }

                Platform.runLater(() -> {
                    modal.update(bundle.getString(LangKeys.BUILD_RUNTIME), 0.5);
                });
                JavaRuntimeLinker linker = new JavaRuntimeLinker(packageProject);
                boolean isLinked = linker.link(new File(dist,"runtime"));
                if (!isLinked) {
                    Platform.runLater(() -> {
                        Alert alert = getView().alert(
                                bundle.getString(LangKeys.FAILED),
                                bundle.getString(LangKeys.BUILD_CAN_NOT_BUILD_RUNTIME),
                                Alert.AlertType.ERROR
                        );
                        alert.showAndWait();
                        modal.hide();
                    });
                    return;
                }

                Platform.runLater(() -> {
                    modal.update(bundle.getString(LangKeys.BUILD_EXECUTABLE), 0.8);
                });


                File launcherFile = new File(dist,packageProject.getName() + executableSubfix);
                File originalLauncher = new File(resources.getAssetsFolder(),"launcher/launcher" + executableSubfix);
                File shortcutScript = new File(resources.getAssetsFolder(), "launcher/shortcut.lua");

                if (packageProject.isNoConsole() && osName.startsWith("win")) {
                    originalLauncher = new File(resources.getAssetsFolder(),"launcher/launcherW" + executableSubfix);
                }

                try {

                    Files.copy(originalLauncher.toPath(), launcherFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    File iconDir = new File(projectFile.getParent(),"icon");
                    if (!iconDir.exists()) {
                        iconDir.mkdir();
                    }

                    File icon = new File(projectFile.getParent(),"icon.png");
                    if (!icon.exists()) {
                        icon = new File(resources.getAssetsFolder(), "icon.png");
                    }
                    List<File> icons = new ArrayList<>();
                    List<Integer> sizes = Arrays.asList(16, 32, 64, 128, 256, 512);
                    for (Integer size : sizes) {
                        File iconFile = new File(iconDir, "icon_" + size + ".png");
                        FileUtils.resizeIconImage(icon, iconFile, size, size);
                        icons.add(iconFile);
                    }

                    boolean iconWrote = FileUtils.updateExecutableIcon(launcherFile, icons, icon);
                    if (!iconWrote) {
                        Platform.runLater(() -> {
                            Alert alert = getView().alert(
                                    bundle.getString(LangKeys.FAILED),
                                    bundle.getString(LangKeys.BUILD_FAILED_WRITE_ICON),
                                    Alert.AlertType.ERROR
                            );
                            alert.showAndWait();
                            modal.hide();
                        });
                        return;
                    }

                    Platform.runLater(() -> {
                        modal.update(bundle.getString(LangKeys.BUILD_WRITING_CONFIG) , 0.9);
                    });
                    PackageConfig packageConfig = new PackageConfig();
                    packageConfig.setModules(modulesName.toString());
                    packageConfig.setMainClass(packageProject.getMainClass());
                    packageConfig.setName(packageProject.getName());
                    packageConfig.setConsole(!packageProject.isNoConsole());

                    String mainModule = txtMainModule.getText();
                    if(mainModule.isBlank()) {
                        StringBuilder classPath = new StringBuilder();
                        String pathPrefix = "/libs/";
                        for (ModEntity mod : packageProject.getDependencies()) {
                            if (!mod.isEnabled()) {
                                continue;
                            }
                            String classPathItem = pathPrefix + mod.getFileName();
                            classPath.append(classPathItem);
                            if (osName.contains("win")) {
                                classPath.append(";");
                            } else {
                                classPath.append(":");
                            }
                        }
                        packageConfig.setClasspath(classPath.toString());
                    } else {
                        packageConfig.setModulePath("/libs");
                        packageConfig.setMainModule(packageProject.getMainModule());
                    }

                    List<String> vmOptions = new ArrayList<>();
                    if (packageProject.getVmOptions() != null) {
                        for (VMOptionPair pair : packageProject.getVmOptions()) {
                            if (pair.getOptValue() != null && !pair.getOptValue().isBlank()) {
                                vmOptions.add(pair.getOptName() + "=" + pair.getOptValue());
                            } else {
                                vmOptions.add(pair.getOptName());
                            }
                        }
                    }
                    packageConfig.setOptions(vmOptions);
                    if (osName.contains("linux")) {
                        packageConfig.setScript("shortcut");
                        Files.copy(shortcutScript.toPath(),new File(dist, "shortcut").toPath(),StandardCopyOption.REPLACE_EXISTING);
                    }
                    File packageFile = new File(dist,"package.json");
                    FileUtils.save(packageConfig, packageFile);
                    Platform.runLater(() -> {
                        modal.hide();
                        Alert alert = getView().alert(
                                bundle.getString(LangKeys.SUCCESS),
                                bundle.getString(LangKeys.BUILD_COMPLETE),
                                Alert.AlertType.INFORMATION
                        );
                        alert.showAndWait();
                    });

                } catch (Exception e) {
                    logger.error("Can not build launcher", e);
                    Platform.runLater(() -> {
                        Alert alert = getView().alert(
                                bundle.getString(LangKeys.FAILED),
                                bundle.getString(LangKeys.BUILD_FAILED_BUILD_LAUNCHER),
                                Alert.AlertType.ERROR
                        );
                        alert.showAndWait();
                        modal.hide();
                    });
                }

            } catch (Exception e) {
                logger.error("Can not build runtime", e);
                Platform.runLater(() -> {
                    getView().alert(
                            bundle.getString(LangKeys.FAILED),
                            bundle.getString(LangKeys.BUILD_CAN_NOT_BUILD_RUNTIME),
                            Alert.AlertType.ERROR
                    ).showAndWait();
                });
            } finally {
                Platform.runLater(() -> {
                    getView().getView().setDisable(false);
                });
            }
        });

    }

    @FXML
    private void clearDeps() {
        tableJars.getItems().clear();
        if (packageProject != null){
            packageProject.getDependencies().clear();
        }
    }

    @FXML
    private void saveProject() {

        if (packageProject == null) {
            packageProject = new PackageProject();
        }

        if (projectFile == null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Project", "*.package.json"));
            projectFile = fileChooser.showSaveDialog(getView().getStage());
            if (projectFile == null) {
                return;
            }
        }

        List<String> modules = tvSystemModule
                .getItems()
                .stream()
                .filter(JavaSysModule::isEnabled)
                .map(JavaSysModule::getName)
                .collect(Collectors.toList());

        JavaEnvironment javaEnv = javaEnvComboBox.getSelectionModel().getSelectedItem();
        JavaFXEnvironment javaFxEnv = javaFxEnvComboBox.getSelectionModel().getSelectedItem();

        packageProject.setSystemModules(modules);
        packageProject.setJavaEnvironment(javaEnv);
        packageProject.setJavaFxEnvironment(javaFxEnv);
        packageProject.setMainClass(txtMainClass.getText());
        packageProject.setMainModule(txtMainModule.getText());
        packageProject.setName(txtName.getText());
        packageProject.setVmOptions(tableVmOptions.getItems());
        packageProject.setNoConsole(cbxNoConsole.isSelected());
        packageProject.setDependencies(tableJars.getItems());
        packageProject.setNativeLibs(tableNativeLibs.getItems());

        FileUtils.save(packageProject, projectFile);
    }

    @FXML
    private void selectIcon() {

        ResourceBundle bundle = resources.getResourceBundle();
        if (projectFile == null) {
            Alert alert = getView().alert(
                    bundle.getString(LangKeys.FAILED),
                    bundle.getString(LangKeys.FAILED_NO_PROJECT),
                    Alert.AlertType.ERROR
            );
            alert.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Icon", "*.png"));
        File file = fileChooser.showOpenDialog(getView().getStage());
        if (file == null) {
            return;
        }

        File projectDir = projectFile.getParentFile();
        File icon = new File(projectDir,"icon.png");
        try {
            Image image = new Image(file.toURI().toURL().toExternalForm());
            if (image.getWidth() != image.getHeight() || image.getWidth() < 512) {
                Alert alert = getView().alert(
                        bundle.getString(LangKeys.FAILED),
                        bundle.getString(LangKeys.FAILED_INVALID_ICON),
                        Alert.AlertType.ERROR
                );
                alert.showAndWait();
                return;
            }
            Files.copy(file.toPath(), icon.toPath(), StandardCopyOption.REPLACE_EXISTING);
            iconView.setImage(new Image(file.toURI().toString()));
        } catch (Exception e) {
            Alert alert = getView().alert(
                    bundle.getString(LangKeys.FAILED),
                    bundle.getString(LangKeys.FAILED_LOAD_ICON),
                    Alert.AlertType.ERROR
            );
            alert.showAndWait();
        }
    }

    @FXML
    private void addNativeLib() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Lib", "*.dll","*.so","*.dylib"));
        File file = fileChooser.showOpenDialog(getView().getStage());
        if (file == null) {
            return;
        }
        ModEntity entity = new ModEntity();
        entity.setFileName(file.getName());
        entity.setEnabled(true);
        entity.setOriginalFilePath(file.getAbsolutePath());
        tableNativeLibs.getItems().add(entity);
    }

    @FXML
    private void removeNativeLib() {
        ModEntity entity = tableNativeLibs.getSelectionModel().getSelectedItem();
        if (entity == null) {
            return;
        }
        tableNativeLibs.getItems().remove(entity);
    }

    @FXML
    private void addJarFromFile() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Jar", "*.jar"));
        File file = fileChooser.showOpenDialog(getView().getStage());
        if (file == null) {
            return;
        }
        ModEntity entity = new ModEntity();
        entity.setFileName(file.getName());
        entity.setEnabled(true);
        entity.setOriginalFilePath(file.getAbsolutePath());
        tableJars.getItems().add(entity);

    }

    @FXML
    private void removeJar() {
        ModEntity entity = tableJars.getSelectionModel().getSelectedItem();
        if (entity == null) {
            return;
        }
        tableJars.getItems().remove(entity);
    }

    @FXML
    private void importJarsFromFolder() {

        DirectoryChooser chooser = new DirectoryChooser();
        File dir = chooser.showDialog(getView().getStage());
        if (dir == null) {
            return;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        List<ModEntity> entities = new ArrayList<>();
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".jar")) {
                ModEntity entity = new ModEntity();
                entity.setFileName(file.getName());
                entity.setEnabled(true);
                entity.setOriginalFilePath(file.getAbsolutePath());
                entities.add(entity);
            }
        }
        ObservableList<ModEntity> items = tableJars.getItems();
        items.addAll(entities);
    }

    @FXML
    private void showJavaMgr() {
        javaEnvManager.show();
    }

    @FXML
    private void showJavaFXMgr() {
        javaFxEnvManager.show();
    }

}
