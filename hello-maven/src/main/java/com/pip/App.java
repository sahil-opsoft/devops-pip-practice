package com.pip;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Tiny web service: "/" returns a greeting, "/db" stores and counts visits in PostgreSQL. */
public final class App {

    static final String GREETING = "Hello from the Jenkins pipeline!";

    private App() {
    }

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(env("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", ex -> reply(ex, 200, GREETING + "\n"));
        server.createContext("/db", ex -> {
            try {
                reply(ex, 200, countVisit());
            } catch (SQLException e) {
                reply(ex, 503, "Database error: " + e.getMessage() + "\n");
            }
        });
        server.start();
        System.out.println("hello-maven listening on port " + port);
    }

    private static String countVisit() throws SQLException {
        String url = env("DB_URL", "jdbc:postgresql://localhost:5432/pip");
        try (Connection c = DriverManager.getConnection(url, env("DB_USER", "pip"), env("DB_PASSWORD", ""));
             Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS visits (id SERIAL PRIMARY KEY, at TIMESTAMP DEFAULT now())");
            s.execute("INSERT INTO visits DEFAULT VALUES");
            try (ResultSet r = s.executeQuery("SELECT count(*) FROM visits")) {
                r.next();
                return "Database OK - visits stored: " + r.getLong(1) + "\n";
            }
        }
    }

    private static void reply(HttpExchange ex, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isEmpty() ? fallback : value;
    }
}
