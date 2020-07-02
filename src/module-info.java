module JavaFX {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.fxc to javafx.fxml;
    exports com.fxc;
}