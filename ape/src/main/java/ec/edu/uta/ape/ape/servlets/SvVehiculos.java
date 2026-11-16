package ec.edu.uta.ape.ape.servlets;

import ec.edu.uta.ape.ape.daos.VehiculoDAO;
import ec.edu.uta.ape.ape.utils.ApiServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Year;
import java.util.Collections;
import java.util.Map;

@WebServlet(name = "SvVehiculos", urlPatterns = {"/api/vehiculos"})
public class SvVehiculos extends ApiServlet {
    private final VehiculoDAO dao = new VehiculoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String placa = request.getParameter("placa");
        Object resultado;
        if (placa == null || placa.isBlank()) {
            resultado = dao.getVehiculosConIds();
        } else {
            Map<String, Object> vehiculo = dao.getVehiculoConIdsByPlaca(placa.trim());
            resultado = vehiculo == null ? Collections.emptyList() : Collections.singletonList(vehiculo);
        }
        response.getWriter().write(gson.toJson(resultado));
    }

    private String texto(HttpServletRequest request, String nombre, int maximo) {
        String valor = request.getParameter(nombre);
        if (valor == null || valor.isBlank() || valor.trim().length() > maximo) {
            throw new IllegalArgumentException("El parámetro " + nombre + " es obligatorio y admite hasta " + maximo + " caracteres.");
        }
        return valor.trim();
    }

    private void guardar(HttpServletRequest request, HttpServletResponse response, boolean editar) throws IOException {
        int id = editar ? positivo(request, "id") : 0;
        String placa = texto(request, "placa", 8);
        String chasis = texto(request, "chasis", 17);
        int anio = positivo(request, "anio");
        if (anio < 1950 || anio > Year.now().getValue()) {
            throw new IllegalArgumentException("El año debe estar entre 1950 y el año actual.");
        }
        int modelo = positivo(request, "id_modelo");
        int color = positivo(request, "id_color");
        boolean resultado = editar ? dao.actualizar(id, placa, chasis, anio, modelo, color)
                : dao.insertar(placa, chasis, anio, modelo, color);
        json(response, resultado ? 200 : 404, resultado,
                resultado ? "Vehículo guardado correctamente" : "El vehículo no existe.");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        guardar(request, response, false);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        guardar(request, response, true);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        boolean resultado = dao.eliminar(positivo(request, "id"));
        json(response, resultado ? 200 : 404, resultado,
                resultado ? "Vehículo eliminado correctamente" : "El vehículo no existe.");
    }
}
