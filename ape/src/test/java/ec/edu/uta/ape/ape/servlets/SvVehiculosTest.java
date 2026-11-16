package ec.edu.uta.ape.ape.servlets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.time.Year;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class SvVehiculosTest {
    private static class Servlet extends SvVehiculos {
        void ejecutar(HttpServletRequest request, HttpServletResponse response) throws Exception {
            service(request, response);
        }
    }

    private JsonObject peticionInvalida(String metodo, Map<String, String> parametros) throws Exception {
        StringWriter cuerpo = new StringWriter();
        int[] status = {200};
        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{HttpServletRequest.class}, (p, m, args) -> {
                    switch (m.getName()) {
                        case "getMethod": return metodo;
                        case "getProtocol": return "HTTP/1.1";
                        case "getParameter": return parametros.get(args[0]);
                        default: return null;
                    }
                });
        HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{HttpServletResponse.class}, (p, m, args) -> {
                    switch (m.getName()) {
                        case "getWriter": return new PrintWriter(cuerpo);
                        case "setStatus": status[0] = (Integer) args[0]; break;
                        default: break;
                    }
                    return null;
                });
        new Servlet().ejecutar(request, response);
        assertEquals(400, status[0]);
        JsonObject json = JsonParser.parseString(cuerpo.toString()).getAsJsonObject();
        assertFalse(json.get("success").getAsBoolean());
        assertTrue(json.get("message").getAsString().length() > 0);
        return json;
    }

    @Test
    public void rechazaIdAusenteNoNumericoYCero() throws Exception {
        peticionInvalida("DELETE", Map.of());
        peticionInvalida("DELETE", Map.of("id", "abc"));
        peticionInvalida("DELETE", Map.of("id", "0"));
        peticionInvalida("PUT", Map.of("id", "-1"));
    }

    @Test
    public void rechazaCamposVaciosYLargosAntesDeConsultarLaBase() throws Exception {
        peticionInvalida("POST", Map.of());
        peticionInvalida("POST", Map.of("placa", "         "));
        peticionInvalida("POST", Map.of("placa", "123456789"));
        peticionInvalida("POST", Map.of("placa", "ABC123", "chasis", "123456789012345678"));
    }

    @Test
    public void validaAnioYReferencias() throws Exception {
        Map<String, String> parametros = new HashMap<>(Map.of("placa", "ABC123", "chasis", "1234567"));
        parametros.put("anio", "1949");
        peticionInvalida("POST", parametros);
        parametros.put("anio", String.valueOf(Year.now().getValue() + 1));
        peticionInvalida("POST", parametros);
        parametros.put("anio", "2020");
        peticionInvalida("POST", parametros);
        parametros.put("id_modelo", "1");
        parametros.put("id_color", "incorrecto");
        peticionInvalida("POST", parametros);
    }
}
