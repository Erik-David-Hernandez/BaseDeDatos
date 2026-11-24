module basedatos {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;

    opens basedatos.controllers to javafx.fxml;
    opens basedatos.models to javafx.base;
    exports basedatos;
    exports basedatos.models;
}
