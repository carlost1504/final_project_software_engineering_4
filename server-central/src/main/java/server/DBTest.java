package server;

import java.sql.Connection;
import java.sql.SQLException;

public class DBTest {
    public static void main(String[] args) {
        try (Connection conn = Database.getConnection()) {
            System.out.println(" Conexión exitosa a PostgreSQL");
        } catch (SQLException e) {
            System.err.println(" Error en conexión: " + e.getMessage());
        }
    }
}
