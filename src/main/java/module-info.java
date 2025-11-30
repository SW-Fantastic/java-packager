module ours.packager {

    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires java.desktop;

    requires swdc.application.configs;
    requires swdc.application.fx;
    requires swdc.application.dependency;
    requires swdc.application.data;
    requires jakarta.annotation;
    requires jakarta.inject;
    requires org.slf4j;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;

    requires jakarta.persistence;
    requires org.hibernate.orm.core;

    requires com.ibm.icu;
    requires org.controlsfx.controls;

    opens org.swdc.packager to
            javafx.graphics,
            javafx.fxml,
            swdc.application.configs,
            swdc.application.dependency,
            swdc.application.fx;

    opens org.swdc.packager.views to
            swdc.application.fx,
            swdc.application.dependency,
            com.fasterxml.jackson.core,
            com.fasterxml.jackson.databind,
            javafx.base,
            javafx.graphics,
            javafx.fxml;

    opens org.swdc.packager.core to
            javafx.base,
            javafx.controls,
            com.fasterxml.jackson.databind,
            com.fasterxml.jackson.core,
            swdc.application.dependency,
            swdc.application.fx;


    opens org.swdc.packager.core.builder to
            javafx.base,
            javafx.controls,
            swdc.application.dependency,
            swdc.application.fx;


    opens org.swdc.packager.core.repo to
            org.hibernate.orm.core,
            swdc.application.dependency,
            swdc.application.data;

    opens org.swdc.packager.core.entity;
    opens org.swdc.packager.core.service;

    opens icons;
    opens views.main;
    opens lang;

}