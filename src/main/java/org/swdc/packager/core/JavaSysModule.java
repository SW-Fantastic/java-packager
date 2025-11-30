package org.swdc.packager.core;

import java.io.File;

public class JavaSysModule {

    private String name;

    private boolean enabled;

    public JavaSysModule(String name, boolean enabled) {
        this.name = name;
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
