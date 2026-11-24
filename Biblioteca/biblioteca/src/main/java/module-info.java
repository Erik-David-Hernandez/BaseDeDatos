module basedatos {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;

    opens basedatos to javafx.fxml;
    exports basedatos;
}
