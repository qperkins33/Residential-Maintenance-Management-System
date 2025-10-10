module com.maintenance {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires java.sql;
    requires com.h2database;
    requires org.controlsfx.controls;

    exports com.maintenance;
    exports com.maintenance.dao;
    exports com.maintenance.database;
    exports com.maintenance.enums;
    exports com.maintenance.models;
    exports com.maintenance.notification;
    exports com.maintenance.service;
    exports com.maintenance.ui.controllers;
    exports com.maintenance.ui.views;
    exports com.maintenance.util;

    opens com.maintenance.ui.controllers to javafx.fxml;
}
