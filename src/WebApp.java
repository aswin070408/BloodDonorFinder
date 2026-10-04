import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// Simple Java web server (no Tomcat needed). Run it, then open http://localhost:8080
public class WebApp {

    static final String[] GROUPS = {"A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"};
    static final DonorDAO donorDAO = new DonorDAO();
    static final AdminDAO adminDAO = new AdminDAO();
    static final Map<String, Session> sessions = new ConcurrentHashMap<>();

    static class Session {
        String role;   // "admin" or "donor"
        int donorId;
    }

    public static void main(String[] args) throws Exception {
        Schema.ensureTables();
        adminDAO.ensureDefaultAdmin();
        String portEnv = System.getenv("PORT"); // cloud hosts give the port here
        int port = (portEnv == null || portEnv.isEmpty()) ? 8080 : Integer.parseInt(portEnv);
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", ex -> {
            try {
                route(ex);
            } catch (Exception e) {
                e.printStackTrace();
                send(ex, 500, Views.page("Error",
                    "<div class='card'><div class='err'>Something went wrong: "
                    + Views.esc(String.valueOf(e.getMessage())) + "</div></div>", null));
            } finally {
                ex.close();
            }
        });
        server.start();
        System.out.println("Server running on port " + port + "  (local: http://localhost:" + port + ")");
    }

    // ---------- routing ----------
    static void route(HttpExchange ex) throws Exception {
        String path = ex.getRequestURI().getPath();
        boolean post = ex.getRequestMethod().equals("POST");
        Map<String, String> q = parse(ex.getRequestURI().getRawQuery());
        Map<String, String> f = post ? parse(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)) : new HashMap<>();
        Session s = getSession(ex);

        switch (path) {
            case "/":
                send(ex, 200, Views.home(s));
                break;

            case "/admin/login":
                if (post) {
                    if (adminDAO.login(f.getOrDefault("username", "").trim(), f.getOrDefault("password", ""))) {
                        startSession(ex, "admin", 0);
                        redirect(ex, "/admin/dashboard");
                    } else {
                        send(ex, 401, Views.adminLogin("Wrong admin username or password."));
                    }
                } else {
                    send(ex, 200, Views.adminLogin(null));
                }
                break;

            case "/admin/dashboard":
                if (!isAdmin(s)) { redirect(ex, "/admin/login"); break; }
                send(ex, 200, Views.dashboard(
                    donorDAO.list(q.getOrDefault("bg", ""), q.getOrDefault("city", "").trim()),
                    q.getOrDefault("bg", ""), q.getOrDefault("city", "").trim(), s));
                break;

            case "/admin/export":
                if (!isAdmin(s)) { redirect(ex, "/admin/login"); break; }
                byte[] csv = ExportService.toCsv(
                    donorDAO.list(q.getOrDefault("bg", ""), q.getOrDefault("city", "").trim()))
                    .getBytes(StandardCharsets.UTF_8);
                ex.getResponseHeaders().set("Content-Type", "text/csv; charset=UTF-8");
                ex.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"donors.csv\"");
                ex.sendResponseHeaders(200, csv.length);
                try (OutputStream os = ex.getResponseBody()) { os.write(csv); }
                break;

            case "/register":
                if (post) handleRegister(ex, f);
                else send(ex, 200, Views.register(null, new HashMap<>()));
                break;

            case "/login":
                if (post) {
                    Donor d = donorDAO.login(f.getOrDefault("username", "").trim().toLowerCase(), f.getOrDefault("password", ""));
                    if (d != null) {
                        startSession(ex, "donor", d.id);
                        redirect(ex, "/profile");
                    } else {
                        send(ex, 401, Views.donorLogin("Wrong username or password.", null));
                    }
                } else {
                    send(ex, 200, Views.donorLogin(null, q.containsKey("ok") ? "Account created. Please sign in." : null));
                }
                break;

            case "/profile":
                if (s == null || !"donor".equals(s.role)) { redirect(ex, "/login"); break; }
                Donor me = donorDAO.findById(s.donorId);
                if (me == null) { redirect(ex, "/logout"); break; }
                send(ex, 200, Views.profile(me, s));
                break;

            case "/donate":
                if (s == null || !"donor".equals(s.role) || !post) { redirect(ex, "/login"); break; }
                donorDAO.recordDonationToday(s.donorId);
                redirect(ex, "/profile");
                break;

            case "/logout":
                String t = cookieToken(ex);
                if (t != null) sessions.remove(t);
                ex.getResponseHeaders().add("Set-Cookie", "SID=; Path=/; Max-Age=0");
                redirect(ex, "/");
                break;

            default:
                send(ex, 404, Views.page("Not found", "<div class='card'>Page not found.</div>", s));
        }
    }

    // ---------- registration ----------
    static void handleRegister(HttpExchange ex, Map<String, String> f) throws Exception {
        String err = validate(f);
        if (err != null) {
            send(ex, 400, Views.register(err, f));
            return;
        }
        Donor d = new Donor();
        d.username = f.get("username").trim().toLowerCase();
        d.name = f.get("name").trim();
        d.age = Integer.parseInt(f.get("age").trim());
        d.gender = f.getOrDefault("gender", "Other");
        d.bloodGroup = f.get("bg");
        d.city = f.get("city").trim();
        d.phone = f.get("phone").trim();
        String last = f.getOrDefault("last", "").trim();
        d.lastDonation = last.isEmpty() ? null : Date.valueOf(last);
        donorDAO.register(d, f.get("password"));
        redirect(ex, "/login?ok=1");
    }

    static String validate(Map<String, String> f) throws Exception {
        String user = f.getOrDefault("username", "").trim().toLowerCase();
        if (!user.matches("[a-z0-9_]{3,20}")) return "Username must be 3-20 letters, digits or underscore.";
        if (f.getOrDefault("password", "").length() < 6) return "Password must be at least 6 characters.";
        if (f.getOrDefault("name", "").trim().isEmpty()) return "Name is required.";
        int age;
        try { age = Integer.parseInt(f.getOrDefault("age", "").trim()); }
        catch (NumberFormatException e) { return "Enter a valid age."; }
        if (age < 18 || age > 65) return "Donor age must be between 18 and 65.";
        if (!Arrays.asList(GROUPS).contains(f.getOrDefault("bg", ""))) return "Choose a blood group.";
        if (f.getOrDefault("city", "").trim().isEmpty()) return "City is required.";
        if (!f.getOrDefault("phone", "").trim().matches("\\d{10}")) return "Phone must be 10 digits.";
        String last = f.getOrDefault("last", "").trim();
        if (!last.isEmpty()) {
            try {
                if (Date.valueOf(last).after(new java.util.Date())) return "Last donation date cannot be in the future.";
            } catch (IllegalArgumentException e) { return "Invalid date."; }
        }
        if (donorDAO.usernameExists(user)) return "That username is already taken.";
        return null;
    }

    // ---------- sessions & helpers ----------
    static boolean isAdmin(Session s) { return s != null && "admin".equals(s.role); }

    static void startSession(HttpExchange ex, String role, int donorId) {
        Session s = new Session();
        s.role = role;
        s.donorId = donorId;
        String token = UUID.randomUUID().toString();
        sessions.put(token, s);
        ex.getResponseHeaders().add("Set-Cookie", "SID=" + token + "; Path=/; HttpOnly; SameSite=Lax");
    }

    static String cookieToken(HttpExchange ex) {
        List<String> cookies = ex.getRequestHeaders().get("Cookie");
        if (cookies == null) return null;
        for (String header : cookies)
            for (String part : header.split(";")) {
                String p = part.trim();
                if (p.startsWith("SID=")) return p.substring(4);
            }
        return null;
    }

    static Session getSession(HttpExchange ex) {
        String t = cookieToken(ex);
        return t == null ? null : sessions.get(t);
    }

    static Map<String, String> parse(String raw) throws Exception {
        Map<String, String> m = new HashMap<>();
        if (raw == null || raw.isEmpty()) return m;
        for (String pair : raw.split("&")) {
            int i = pair.indexOf('=');
            String k = i < 0 ? pair : pair.substring(0, i);
            String v = i < 0 ? "" : pair.substring(i + 1);
            m.put(URLDecoder.decode(k, "UTF-8"), URLDecoder.decode(v, "UTF-8"));
        }
        return m;
    }

    static void send(HttpExchange ex, int status, String html) throws IOException {
        byte[] b = html.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        ex.sendResponseHeaders(status, b.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(b); }
    }

    static void redirect(HttpExchange ex, String location) throws IOException {
        ex.getResponseHeaders().set("Location", location);
        ex.sendResponseHeaders(302, -1);
    }
}
