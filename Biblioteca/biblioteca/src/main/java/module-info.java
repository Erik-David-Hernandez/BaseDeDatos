module basedatos {
    requires javafx.controls;
    requires javafx.fxml;

    opens basedatos to javafx.fxml;
    exports basedatos;
}
