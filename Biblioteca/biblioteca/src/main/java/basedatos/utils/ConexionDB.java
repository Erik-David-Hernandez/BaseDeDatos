package basedatos.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    private static final String URL = "jdbc:postgresql://localhost:5432/Biblioteca_virtual?connectTimeout=5";
    static final String USER = System.getenv().getOrDefault("BIBLIOTECA_DB_USER", "biblioteca");
    static final String PASSWORD = System.getenv().getOrDefault("BIBLIOTECA_DB_PASSWORD", "admin");

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
