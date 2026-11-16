package ec.edu.uta.ape.ape.servlets;

import ec.edu.uta.ape.ape.daos.ModeloDAO;
import ec.edu.uta.ape.ape.utils.ApiServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "SvModelos", urlPatterns = {"/api/modelos"})
public class SvModelos extends ApiServlet {
    private final ModeloDAO dao = new ModeloDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Object resultado = dao.getModelos(positivo(request, "id"));
        response.getWriter().write(gson.toJson(resultado));
    }
}
