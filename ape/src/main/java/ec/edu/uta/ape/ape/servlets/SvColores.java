package ec.edu.uta.ape.ape.servlets;

import ec.edu.uta.ape.ape.daos.ColorDAO;
import ec.edu.uta.ape.ape.utils.ApiServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "SvColores", urlPatterns = {"/api/colores"})
public class SvColores extends ApiServlet {
    private final ColorDAO dao = new ColorDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Object resultado = dao.getColores();
        response.getWriter().write(gson.toJson(resultado));
    }
}
