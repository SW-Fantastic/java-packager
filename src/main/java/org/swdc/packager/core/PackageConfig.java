package org.swdc.packager.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PackageConfig {

    @JsonProperty("main-class")
    private String mainClass;

    @JsonProperty("main-module")
    private String mainModule;

    @JsonProperty("module-path")
    private String modulePath;

    @JsonProperty("class-path")
    private String classpath;

    @JsonProperty("options")
    private List<String> options;

    private String modules;

    private String script;

    private boolean console;

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isConsole() {
        return console;
    }

    public void setConsole(boolean console) {
        this.console = console;
    }

    public void setMainModule(String mainModule) {
        this.mainModule = mainModule;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public void setClasspath(String classpath) {
        this.classpath = classpath;
    }

    public void setModulePath(String modulePath) {
        this.modulePath = modulePath;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getModules() {
        return modules;
    }

    public void setModules(String modules) {
        this.modules = modules;
    }

    public String getMainModule() {
        return mainModule;
    }

    public String getMainClass() {
        return mainClass;
    }

    public List<String> getOptions() {
        return options;
    }

    public String getClasspath() {
        return classpath;
    }

    public String getModulePath() {
        return modulePath;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }
}
