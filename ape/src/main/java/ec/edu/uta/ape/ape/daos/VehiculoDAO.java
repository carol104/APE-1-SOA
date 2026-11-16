/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.edu.uta.ape.ape.daos;

import ec.edu.uta.ape.ape.connection.Conexion;
import ec.edu.uta.ape.ape.models.Vehiculo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author USER
 */
public class VehiculoDAO {

    public List<Vehiculo> getVehiculos() {
        List<Vehiculo> vehiculos = new ArrayList<>();
        String sql = "SELECT  v.id , v.placa, v.chasis, v.anio, m.marca AS marca, o.modelo AS modelo, c.color AS color FROM vehiculos v JOIN modelos o ON v.id_modelo = o.id JOIN marcas m ON o.id_marca = m.id JOIN colores c ON v.id_color = c.id ";

        try (Connection conexion = Conexion.obtenerConexion(); Statement st = conexion.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                vehiculos.add(new Vehiculo(
                        rs.getInt("id"),
                        rs.getString("placa"),
                        rs.getString("chasis"),
                        rs.getInt("anio"),
                        rs.getString("marca"),
                        rs.getString("modelo"),
                        rs.getString("color")
                ));

            }
        } catch (Exception e) {
            throw new IllegalStateException("Error al consultar la base de datos", e);
        }

        return vehiculos;

    }

    // Nueva: listar con IDs y nombres sin crear una clase nueva
    public List<Map<String, Object>> getVehiculosConIds() {
        List<Map<String, Object>> vehiculos = new ArrayList<>();
        String sql = "SELECT v.id, v.placa, v.chasis, v.anio, "
                + "m.id AS id_marca, o.id AS id_modelo, c.id AS id_color, "
                + "m.marca AS marca, o.modelo AS modelo, c.color AS color "
                + "FROM vehiculos v "
                + "JOIN modelos o ON v.id_modelo = o.id "
                + "JOIN marcas m ON o.id_marca = m.id "
                + "JOIN colores c ON v.id_color = c.id";

        try (Connection conexion = Conexion.obtenerConexion(); Statement st = conexion.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> v = new HashMap<>();
                v.put("id", rs.getInt("id"));
                v.put("placa", rs.getString("placa"));
                v.put("chasis", rs.getString("chasis"));
                v.put("anio", rs.getInt("anio"));
                v.put("id_marca", rs.getInt("id_marca"));
                v.put("id_modelo", rs.getInt("id_modelo"));
                v.put("id_color", rs.getInt("id_color"));
                v.put("marca", rs.getString("marca"));
                v.put("modelo", rs.getString("modelo"));
                v.put("color", rs.getString("color"));
                vehiculos.add(v);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Error al consultar la base de datos", e);
        }

        return vehiculos;
    }

    /**
     * Obtiene un vehículo por su número de placa.
     *
     * @param placa El número de placa del vehículo a buscar.
     * @return El objeto Vehiculo encontrado o null si no existe.
     */
    public Vehiculo getVehiculoByPlaca(String placa) {
        Vehiculo vehiculo = null;

        // La consulta ahora incluye WHERE y los IDs de las FKs para compatibilidad
        String sql = "SELECT v.id, v.placa, v.chasis, v.anio, m.marca AS marca, o.modelo AS modelo, c.color AS color, "
                + "v.id_modelo, v.id_color, m.id AS id_marca "
                + "FROM vehiculos v "
                + "JOIN modelos o ON v.id_modelo = o.id "
                + "JOIN marcas m ON o.id_marca = m.id "
                + "JOIN colores c ON v.id_color = c.id "
                + "WHERE v.placa = ?"; // Cláusula WHERE para filtrar por placa

        try (Connection conexion = Conexion.obtenerConexion(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, placa);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                   
                    vehiculo = new Vehiculo(
                            rs.getInt("id"),
                            rs.getString("placa"),
                            rs.getString("chasis"),
                            rs.getInt("anio"),
                            rs.getString("marca"),
                            rs.getString("modelo"),
                            rs.getString("color")
                    );
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Error al consultar la base de datos", e);
        }

        return vehiculo;
    }

    // Nueva: obtener por placa con IDs y nombres en un Map
    public Map<String, Object> getVehiculoConIdsByPlaca(String placa) {
        Map<String, Object> vehiculo = null;

        String sql = "SELECT v.id, v.placa, v.chasis, v.anio, "
                + "m.id AS id_marca, o.id AS id_modelo, c.id AS id_color, "
                + "m.marca AS marca, o.modelo AS modelo, c.color AS color "
                + "FROM vehiculos v "
                + "JOIN modelos o ON v.id_modelo = o.id "
                + "JOIN marcas m ON o.id_marca = m.id "
                + "JOIN colores c ON v.id_color = c.id "
                + "WHERE v.placa = ?";

        try (Connection conexion = Conexion.obtenerConexion(); PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, placa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    vehiculo = new HashMap<>();
                    vehiculo.put("id", rs.getInt("id"));
                    vehiculo.put("placa", rs.getString("placa"));
                    vehiculo.put("chasis", rs.getString("chasis"));
                    vehiculo.put("anio", rs.getInt("anio"));
                    vehiculo.put("id_marca", rs.getInt("id_marca"));
                    vehiculo.put("id_modelo", rs.getInt("id_modelo"));
                    vehiculo.put("id_color", rs.getInt("id_color"));
                    vehiculo.put("marca", rs.getString("marca"));
                    vehiculo.put("modelo", rs.getString("modelo"));
                    vehiculo.put("color", rs.getString("color"));
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Error al consultar la base de datos", e);
        }

        return vehiculo;
    }

    public boolean insertar(String placa, String chasis, int anio, int idModelo, int idColor) {
        String sql = "INSERT INTO vehiculos (placa, chasis, anio,  id_modelo, id_color) VALUES (?, ?, ?, ?,  ?)";

        try (Connection conexion = Conexion.obtenerConexion(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, placa);
            ps.setString(2, chasis);
            ps.setInt(3, anio);
            ps.setInt(4, idModelo);
            ps.setInt(5, idColor);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (Exception e) {
            throw new IllegalStateException("Error al modificar la base de datos", e);
        }
    }

    public boolean actualizar(int id, String placa, String chasis, int anio, int idModelo, int idColor) {
        String sql = "UPDATE vehiculos SET placa=?, chasis=?, anio=?, id_modelo=?, id_color=? WHERE id=?";

        try (Connection conexion = Conexion.obtenerConexion(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, placa);
            ps.setString(2, chasis);
            ps.setInt(3, anio);
            ps.setInt(4, idModelo);
            ps.setInt(5, idColor);
            ps.setInt(6, id);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (Exception e) {
            throw new IllegalStateException("Error al modificar la base de datos", e);
        }
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM vehiculos WHERE id = ?";

        try (Connection conexion = Conexion.obtenerConexion(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (Exception e) {
            throw new IllegalStateException("Error al modificar la base de datos", e);
        }
    }

}
