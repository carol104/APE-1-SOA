package ec.edu.uta.ape.ape.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Conexion {
    private Conexion() { }

    public static Connection obtenerConexion() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontró MySQL Connector/J.", e);
        }
        return DriverManager.getConnection(config("DB_URL", "jdbc:mysql://localhost:3306/apesoa"),
                config("DB_USER", "root"), config("DB_PASSWORD", ""));
    }

    private static String config(String nombre, String defecto) {
        String valor = System.getenv(nombre);
        return valor == null ? defecto : valor;
    }
}
