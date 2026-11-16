package ec.edu.uta.ape.apefront.servlets;

import ec.edu.uta.ape.apefront.utils.ApiCliente;
import java.io.IOException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "SvColor", urlPatterns = {"/SvColor"})
public class SvColor extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        try {
            String url = ApiCliente.url("colores");
            String resultado = ApiCliente.get(url);
            response.getWriter().write(resultado);
        } catch (IOException e) {
            log("No se pudo consultar la API", e);
            response.setStatus(502);
            response.getWriter().write("{\"success\":false,\"message\":\"No se pudo conectar con la API.\"}");
        }
    }
}
