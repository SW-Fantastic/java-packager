package org.swdc.packager.core.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class JavaFXEnvironment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String path;

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return name + "  [" + path + "]";
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JavaFXEnvironment) {
            JavaFXEnvironment other = (JavaFXEnvironment) obj;
            if (id != null && other.getId() != null && other.getId().equals(id)) {
                return true;
            } else {
                return toString().equals(other.toString());
            }
        }
        return false;
    }
}
