package ec.edu.uta.ape.ape.servlets;

import ec.edu.uta.ape.ape.daos.MarcaDAO;
import ec.edu.uta.ape.ape.utils.ApiServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "SvMarcas", urlPatterns = {"/api/marcas"})
public class SvMarcas extends ApiServlet {
    private final MarcaDAO dao = new MarcaDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Object resultado = dao.getMarcas();
        response.getWriter().write(gson.toJson(resultado));
    }
}
