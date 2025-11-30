package org.swdc.packager.core;

public class Language {

    private String name;
    private String local;

    public String getName() {
        return name;
    }

    public String getLocal() {
        return local;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    @Override
    public String toString() {
        return getName() + " (" + getLocal() + ")";
    }

}
