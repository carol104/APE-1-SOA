package ec.edu.uta.ape.apefront.servlets;

import ec.edu.uta.ape.apefront.utils.ApiCliente;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "SvVehiculo", urlPatterns = {"/SvVehiculo"})
public class SvVehiculo extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String url = ApiCliente.url("vehiculos");
        String placa = request.getParameter("placa");
        if (placa != null && !placa.isBlank()) {
            url += "?" + ApiCliente.parametros(Map.of("placa", placa.trim()));
        }
        String resultado;
        try {
            resultado = ApiCliente.get(url);
            new org.json.JSONArray(resultado); // Validar antes de renderizar el JSP.
        } catch (Exception e) {
            log("No se pudo consultar la API de vehículos", e);
            resultado = "[]";
            response.setStatus(502);
            request.setAttribute("errorApi", "No se pudo cargar la lista. Compruebe que la API y la base de datos estén disponibles.");
        }
        request.setAttribute("vehiculos", resultado);
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }

    private void modificar(HttpServletRequest request, HttpServletResponse response, String metodo) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        try {
            Map<String, String> datos = new LinkedHashMap<>();
            if (!"POST".equals(metodo)) datos.put("id", request.getParameter("id"));
            if (!"DELETE".equals(metodo)) {
                datos.put("placa", request.getParameter("placa"));
                datos.put("chasis", request.getParameter("chasis"));
                datos.put("anio", request.getParameter("anio"));
                datos.put("id_modelo", request.getParameter("modelo"));
                datos.put("id_color", request.getParameter("color"));
            }
            String url = ApiCliente.url("vehiculos");
            int status;
            switch (metodo) {
                case "POST": status = ApiCliente.post(url, datos); break;
                case "PUT": status = ApiCliente.put(url, datos); break;
                default: status = ApiCliente.delete(url + "?" + ApiCliente.parametros(datos));
            }
            // Una redirección no confirma una modificación correcta.
            if (status >= 300 && status < 400) status = 502;
            response.setStatus(status);
            response.getWriter().write(new org.json.JSONObject()
                    .put("success", status >= 200 && status < 300)
                    .put("message", status >= 200 && status < 300 ? "Operación completada" : "No se pudo completar la operación (HTTP " + status + ").")
                    .toString());
        } catch (IOException e) {
            log("No se pudo modificar el vehículo", e);
            response.setStatus(502);
            response.getWriter().write("{\"success\":false,\"message\":\"No se pudo conectar con la API.\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        modificar(request, response, "POST");
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        modificar(request, response, "PUT");
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        modificar(request, response, "DELETE");
    }
}
