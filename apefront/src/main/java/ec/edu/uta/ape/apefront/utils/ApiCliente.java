package ec.edu.uta.ape.apefront.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public final class ApiCliente {
    private ApiCliente() { }

    public static String url(String recurso) {
        String base = System.getenv("APE_API_URL");
        if (base == null || base.isBlank()) base = "http://localhost:8081/ape/api";
        return base.replaceAll("/+$", "") + "/" + recurso;
    }

    public static String parametros(Map<String, String> parametros) {
        return parametros.entrySet().stream()
                .map(p -> URLEncoder.encode(p.getKey(), StandardCharsets.UTF_8) + "="
                        + URLEncoder.encode(p.getValue() == null ? "" : p.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    private static HttpURLConnection abrir(String url, String metodo) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod(metodo);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestProperty("Accept", "application/json");
        return conn;
    }

    public static String get(String url) throws IOException {
        HttpURLConnection conn = abrir(url, "GET");
        try {
            int status = conn.getResponseCode();
            if (status < 200 || status >= 300) throw new IOException("La API respondió HTTP " + status);
            try (InputStream in = conn.getInputStream()) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        } finally {
            conn.disconnect();
        }
    }

    private static int enviar(String url, String metodo, Map<String, String> parametros) throws IOException {
        // Los servlets leen los parámetros PUT de la URL, no del cuerpo.
        if ("PUT".equals(metodo) && parametros != null && !parametros.isEmpty()) {
            url += (url.contains("?") ? "&" : "?") + parametros(parametros);
        }
        HttpURLConnection conn = abrir(url, metodo);
        try {
            if ("POST".equals(metodo)) {
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                conn.setDoOutput(true);
                try (OutputStream out = conn.getOutputStream()) {
                    out.write(parametros(parametros).getBytes(StandardCharsets.UTF_8));
                }
            }
            return conn.getResponseCode();
        } finally {
            conn.disconnect();
        }
    }

    public static int post(String url, Map<String, String> parametros) throws IOException {
        return enviar(url, "POST", parametros);
    }

    public static int put(String url, Map<String, String> parametros) throws IOException {
        return enviar(url, "PUT", parametros);
    }

    public static int delete(String url) throws IOException {
        return enviar(url, "DELETE", null);
    }
}
