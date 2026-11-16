/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.edu.uta.ape.ape.daos;

import ec.edu.uta.ape.ape.connection.Conexion;
import ec.edu.uta.ape.ape.models.Color;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author USER
 */
public class ColorDAO {

    public List<Color> getColores() {
        List<Color> colores = new ArrayList<>();
        String sql = "SELECT * FROM colores";

        try (Connection conexion = Conexion.obtenerConexion(); Statement st = conexion.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                colores.add(new Color(rs.getInt("id"), rs.getString("color")));

            }
        } catch (Exception e) {
            throw new IllegalStateException("Error al consultar la base de datos", e);
        }

        return colores;

    }

}
