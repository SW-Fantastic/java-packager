package org.swdc.packager.core;

import org.swdc.packager.core.entity.JavaEnvironment;
import org.swdc.packager.core.entity.JavaFXEnvironment;
import org.swdc.packager.views.ModEntity;
import org.swdc.packager.views.VMOptionPair;

import java.util.List;
import java.util.Map;

/**
 * Java打包项目的项目对象。
 */
public class PackageProject {

    private String name;

    private JavaEnvironment javaEnvironment;

    private JavaFXEnvironment javaFxEnvironment;

    private String mainClass;

    private String mainModule;

    private boolean noConsole;

    private List<VMOptionPair> vmOptions;

    private List<ModEntity> dependencies;

    private List<ModEntity> nativeLibs;

    private List<String> systemModules;

    public List<String> getSystemModules() {
        return systemModules;
    }

    public void setSystemModules(List<String> systemModules) {
        this.systemModules = systemModules;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JavaEnvironment getJavaEnvironment() {
        return javaEnvironment;
    }

    public void setJavaEnvironment(JavaEnvironment javaEnvironment) {
        this.javaEnvironment = javaEnvironment;
    }

    public JavaFXEnvironment getJavaFxEnvironment() {
        return javaFxEnvironment;
    }

    public void setJavaFxEnvironment(JavaFXEnvironment javaFxEnvironment) {
        this.javaFxEnvironment = javaFxEnvironment;
    }

    public List<VMOptionPair> getVmOptions() {
        return vmOptions;
    }

    public void setVmOptions(List<VMOptionPair> vmOptions) {
        this.vmOptions = vmOptions;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public String getMainModule() {
        return mainModule;
    }

    public void setMainModule(String mainModule) {
        this.mainModule = mainModule;
    }

    public boolean isNoConsole() {
        return noConsole;
    }

    public void setNoConsole(boolean noConsole) {
        this.noConsole = noConsole;
    }

    public List<ModEntity> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<ModEntity> dependencies) {
        this.dependencies = dependencies;
    }

    public List<ModEntity> getNativeLibs() {
        return nativeLibs;
    }

    public void setNativeLibs(List<ModEntity> nativeLibs) {
        this.nativeLibs = nativeLibs;
    }
}
