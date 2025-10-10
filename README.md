# Residential Maintenance Management System

## Running the JavaFX application from IntelliJ IDEA

If you receive the error `JavaFX runtime components are missing` when running `com.maintenance.MainApplication`, the IDE is launching the app without the required JavaFX modules on the module path.

To fix this, use the bundled run configuration that adds the JavaFX modules automatically:

1. In IntelliJ IDEA open the *Run* configuration chooser and pick **MainApplication (JavaFX)**.
2. This configuration passes the correct `--module-path` and `--add-modules` flags so that the JavaFX runtime is available at launch.
3. Press *Run* (or *Debug*) to start the application.

If you prefer to configure the settings manually, edit the run configuration for `MainApplication` and add the following VM options (Mac/Linux path separator shown, replace `:` with `;` on Windows):

```
--module-path $MAVEN_REPOSITORY$/org/openjfx/javafx-base/21.0.1/javafx-base-21.0.1.jar:$MAVEN_REPOSITORY$/org/openjfx/javafx-base/21.0.1/javafx-base-21.0.1-mac.jar:$MAVEN_REPOSITORY$/org/openjfx/javafx-controls/21.0.1/javafx-controls-21.0.1.jar:$MAVEN_REPOSITORY$/org/openjfx/javafx-controls/21.0.1/javafx-controls-21.0.1-mac.jar:$MAVEN_REPOSITORY$/org/openjfx/javafx-fxml/21.0.1/javafx-fxml-21.0.1.jar:$MAVEN_REPOSITORY$/org/openjfx/javafx-fxml/21.0.1/javafx-fxml-21.0.1-mac.jar:$MAVEN_REPOSITORY$/org/openjfx/javafx-graphics/21.0.1/javafx-graphics-21.0.1.jar:$MAVEN_REPOSITORY$/org/openjfx/javafx-graphics/21.0.1/javafx-graphics-21.0.1-mac.jar --add-modules javafx.controls,javafx.fxml
```

`$MAVEN_REPOSITORY$` is an IntelliJ macro that expands to your local Maven repository (for example `~/.m2/repository`). The provided run configuration stored in `.idea/runConfigurations/MainApplication_with_JavaFX.xml` already includes these settings so you can run or debug the UI without further changes.
