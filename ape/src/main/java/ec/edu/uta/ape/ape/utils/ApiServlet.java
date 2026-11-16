package ec.edu.uta.ape.ape.utils;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/** Maneja los errores antes de escribir el cuerpo de la respuesta. */
public abstract class ApiServlet extends HttpServlet {
    protected final Gson gson = new Gson();

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        try {
            super.service(request, response);
        } catch (IllegalArgumentException e) {
            json(response, 400, false, e.getMessage());
        } catch (RuntimeException e) {
            log("Error al procesar la API", e);
            json(response, 500, false, "No se pudo completar la operación. Revise la conexión y los registros del servidor.");
        }
    }

    protected void json(HttpServletResponse response, int status, boolean success, String message)
            throws IOException {
        response.setStatus(status);
        response.getWriter().write(gson.toJson(Map.of("success", success, "message", message)));
    }

    protected int positivo(HttpServletRequest request, String nombre) {
        try {
            int valor = Integer.parseInt(request.getParameter(nombre));
            if (valor > 0) return valor;
        } catch (NumberFormatException e) {
            // Mismo mensaje para valores ausentes y no numéricos.
        }
        throw new IllegalArgumentException("El parámetro " + nombre + " debe ser un entero positivo.");
    }
}
