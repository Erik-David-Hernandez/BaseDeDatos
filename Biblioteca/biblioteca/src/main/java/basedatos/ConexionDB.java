package basedatos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    // Ajusta estos parámetros si tu configuración local de PostgreSQL es distinta
    private static final String URL = "jdbc:postgresql://localhost:5432/Biblioteca_virtual";
    private static final String USER = "postgres";
    private static final String PASSWORD = "admin"; // <-- Cambia por tu contraseña de postgres

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
