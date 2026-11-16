/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.edu.uta.ape.ape.daos;

import ec.edu.uta.ape.ape.connection.Conexion;
import ec.edu.uta.ape.ape.models.Marca;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author USER
 */
public class MarcaDAO {

    public List<Marca> getMarcas() {
        List<Marca> marcas = new ArrayList<>();
        String sql = "SELECT * FROM marcas";
        try (Connection conexion = Conexion.obtenerConexion(); Statement st = conexion.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                marcas.add(new Marca(rs.getInt("id"), rs.getString("marca")));

            }
        } catch (Exception e) {
            throw new IllegalStateException("Error al consultar la base de datos", e);
        }
        return marcas;
    }
}
