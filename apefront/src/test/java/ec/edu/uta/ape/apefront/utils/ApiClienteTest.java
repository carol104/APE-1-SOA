package ec.edu.uta.ape.apefront.utils;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ApiClienteTest {
    private HttpServer server;
    private String url;

    @Before
    public void iniciar() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        url = "http://127.0.0.1:" + server.getAddress().getPort();
        server.start();
    }

    @After
    public void detener() {
        server.stop(0);
    }

    @Test
    public void postCodificaDatosEspecialesEnUtf8() throws Exception {
        String[] recibido = new String[2];
        server.createContext("/vehiculos", exchange -> {
            recibido[0] = exchange.getRequestMethod();
            recibido[1] = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        assertEquals(200, ApiCliente.post(url + "/vehiculos", Map.of("placa", "A&B + ñ")));
        assertEquals("POST", recibido[0]);
        assertEquals("placa=A&B + ñ", URLDecoder.decode(recibido[1], StandardCharsets.UTF_8));
    }

    @Test
    public void putEnviaParametrosEnLaUrl() throws Exception {
        String[] recibido = new String[3];
        server.createContext("/vehiculos", exchange -> {
            recibido[0] = exchange.getRequestMethod();
            recibido[1] = exchange.getRequestURI().getRawQuery();
            recibido[2] = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(400, -1);
            exchange.close();
        });
        assertEquals(400, ApiCliente.put(url + "/vehiculos?id=1", Map.of("chasis", "A&B")));
        assertEquals("PUT", recibido[0]);
        assertEquals("id=1&chasis=A%26B", recibido[1]);
        assertEquals("", recibido[2]);
    }

    @Test
    public void noSigueRedireccionesQueOcultanErrores() throws Exception {
        AtomicInteger visitas = new AtomicInteger();
        server.createContext("/destino", exchange -> {
            visitas.incrementAndGet();
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.createContext("/redireccion", exchange -> {
            exchange.getResponseHeaders().add("Location", url + "/destino");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });
        assertEquals(302, ApiCliente.post(url + "/redireccion", Map.of()));
        assertThrows(IOException.class, () -> ApiCliente.get(url + "/redireccion"));
        assertEquals(0, visitas.get());
    }

    @Test
    public void getPropagaErrorYDeleteConservaEstado() throws Exception {
        server.createContext("/error", exchange -> {
            exchange.sendResponseHeaders(500, -1);
            exchange.close();
        });
        assertThrows(IOException.class, () -> ApiCliente.get(url + "/error"));
        assertEquals(500, ApiCliente.delete(url + "/error"));
    }
}
