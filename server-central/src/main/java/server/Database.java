// server-central/src/main/java/server/Database.java
package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String URL = "jdbc:postgresql://localhost:5432/voting_system";
    private static final String USER = "votante_user";
    private static final String PASSWORD = "votante_pass";

    static {
        try {
            // Carga del driver JDBC de PostgreSQL
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("No se pudo cargar el driver de PostgreSQL.");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}