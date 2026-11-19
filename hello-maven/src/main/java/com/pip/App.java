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
import java.util.concurrent.atomic.AtomicBoolean;

/** Tiny web service: greeting, PostgreSQL visit counter, health endpoint and a failure switch. */
public final class App {

    static final String DEFAULT_GREETING = "Hello from the Jenkins pipeline!";
    private static final AtomicBoolean HEALTHY = new AtomicBoolean(true);

    private App() {
    }

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(env("PORT", "8080"));
        String greeting = env("APP_GREETING", DEFAULT_GREETING);
        String pod = env("HOSTNAME", "unknown");
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", ex -> reply(ex, 200, greeting + " (pod " + pod + ")\n"));
        server.createContext("/healthz", ex -> {
            if (HEALTHY.get()) {
                reply(ex, 200, "ok\n");
            } else {
                reply(ex, 500, "unhealthy\n");
            }
        });
        server.createContext("/fail", ex -> {
            HEALTHY.set(false);
            reply(ex, 200, "health check will now fail on pod " + pod + "\n");
        });
        server.createContext("/db", ex -> {
            try {
                reply(ex, 200, countVisit());
            } catch (SQLException e) {
                reply(ex, 503, "Database error: " + e.getMessage() + "\n");
            }
        });
        server.start();
        String source = System.getenv("APP_GREETING") == null ? "built-in default" : "APP_GREETING";
        System.out.println("hello-maven v2 listening on port " + port + ", greeting from " + source);
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
