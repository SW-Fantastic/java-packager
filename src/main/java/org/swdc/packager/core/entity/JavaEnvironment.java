package org.swdc.packager.core.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Map;

@Entity
public class JavaEnvironment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String version;

    private String path;


    public Long getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public String getVersion() {
        return version;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return version + "  [" + path + "]";
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JavaEnvironment) {
            JavaEnvironment other = (JavaEnvironment) obj;
            if (id != null && other.getId() != null && other.getId().equals(id)) {
                return true;
            } else {
                return toString().equals(other.toString());
            }
        }
        return false;
    }
}
