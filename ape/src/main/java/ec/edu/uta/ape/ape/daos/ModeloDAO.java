/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.edu.uta.ape.ape.daos;

import ec.edu.uta.ape.ape.connection.Conexion;
import ec.edu.uta.ape.ape.models.Marca;
import ec.edu.uta.ape.ape.models.Modelo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author USER
 */
public class ModeloDAO {

    public List<Modelo> getModelos(int idMarca) {
        List<Modelo> modelos = new ArrayList<>();

        String sql = "SELECT * FROM modelos WHERE id_marca = ?";

        try (Connection conexion = Conexion.obtenerConexion(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idMarca); 

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    modelos.add(new Modelo(
                            rs.getInt("id"),
                            rs.getString("modelo"),
                            rs.getInt("id_marca")
                    ));
                }
            }

        } catch (Exception e) {
            throw new IllegalStateException("Error al consultar la base de datos", e);
        }

        return modelos;
    }

}
