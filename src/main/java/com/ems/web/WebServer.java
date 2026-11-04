package com.ems.web;

import com.ems.config.DBConnection;
import com.ems.dao.BookingDAO;
import com.ems.dao.EventDAO;
import com.ems.dao.TicketDAO;
import com.ems.dao.UserDAO;
import com.ems.model.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Time;
import java.util.*;
import java.util.concurrent.Executors;

public class WebServer {

    private static final int DEFAULT_PORT = 8080;
    private final UserDAO userDAO = new UserDAO();
    private final EventDAO eventDAO = new EventDAO();
    private final TicketDAO ticketDAO = new TicketDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private int port = DEFAULT_PORT;
    private HttpServer server;

    public static void main(String[] args) {
        WebServer webServer = new WebServer();
        webServer.start();
    }

    public void start() {
        try {
            String envPort = System.getenv("PORT");
            if (envPort != null && !envPort.isBlank()) {
                // Cloud provider assigned port (e.g. 10000 on Render)
                this.port = Integer.parseInt(envPort.trim());
                server = HttpServer.create(new InetSocketAddress(this.port), 0);
            } else {
                // Local dev fallback searching 8080-8090
                int currentPort = DEFAULT_PORT;
                boolean started = false;
                while (!started && currentPort <= 8090) {
                    try {
                        server = HttpServer.create(new InetSocketAddress(currentPort), 0);
                        started = true;
                        this.port = currentPort;
                    } catch (IOException e) {
                        currentPort++;
                    }
                }
                if (!started) {
                    throw new IOException("Could not bind to any port between 8080 and 8090.");
                }
            }

            // Route Contexts
            server.createContext("/", new StaticFileHandler());
            server.createContext("/api/auth/login", new AuthHandler());
            server.createContext("/api/events", new EventsHandler());
            server.createContext("/api/events/all", new AllEventsHandler());
            server.createContext("/api/events/organizer", new OrganizerEventsHandler());
            server.createContext("/api/events/create", new CreateEventHandler());
            server.createContext("/api/events/status", new EventStatusHandler());
            server.createContext("/api/events/delete", new DeleteEventHandler());
            server.createContext("/api/tickets", new TicketsHandler());
            server.createContext("/api/tickets/create", new CreateTicketHandler());
            server.createContext("/api/tickets/delete", new DeleteTicketHandler());
            server.createContext("/api/bookings/create", new CreateBookingHandler());
            server.createContext("/api/bookings/find", new FindBookingHandler());
            server.createContext("/api/bookings/all", new AllBookingsHandler());
            server.createContext("/api/bookings/organizer", new OrganizerBookingsHandler());
            server.createContext("/api/stats/admin", new AdminStatsHandler());
            server.createContext("/api/stats/organizer", new OrganizerStatsHandler());
            server.createContext("/api/organizers/all", new AllOrganizersHandler());
            server.createContext("/api/organizers/add", new AddOrganizerHandler());

            server.setExecutor(Executors.newFixedThreadPool(10));
            server.start();

            String url = "http://localhost:" + port;
            System.out.println("=================================================");
            System.out.println("🌐 EMS Web Application is LIVE!");
            System.out.println("👉 Access in your browser at: " + url);
            System.out.println("=================================================");

            // Automatically open browser on host
            openBrowser(url);

        } catch (Exception e) {
            System.err.println("❌ Failed to start web server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openBrowser(String url) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("mac")) {
                Runtime.getRuntime().exec(new String[]{"open", url});
            } else if (os.contains("win")) {
                Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url});
            } else if (os.contains("nix") || os.contains("nux")) {
                Runtime.getRuntime().exec(new String[]{"xdg-open", url});
            }
        } catch (Exception ignored) {}
    }

    // ========================================================
    // STATIC FILE & HTML HANDLER
    // ========================================================
    private static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            InputStream in = getClass().getResourceAsStream("/web" + path);
            if (in == null) {
                // Try file system fallback
                File f = new File("src/main/resources/web" + path);
                if (f.exists()) {
                    in = new FileInputStream(f);
                }
            }

            if (in == null) {
                String notFound = "<h1>404 Not Found</h1>";
                sendResponse(exchange, 404, "text/html", notFound.getBytes(StandardCharsets.UTF_8));
                return;
            }

            byte[] bytes = in.readAllBytes();
            in.close();

            String contentType = "text/html; charset=UTF-8";
            if (path.endsWith(".css")) contentType = "text/css";
            else if (path.endsWith(".js")) contentType = "application/javascript";
            else if (path.endsWith(".png")) contentType = "image/png";
            else if (path.endsWith(".svg")) contentType = "image/svg+xml";

            sendResponse(exchange, 200, contentType, bytes);
        }
    }

    // ========================================================
    // API HANDLERS
    // ========================================================

    private class AuthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            Map<String, String> body = JsonUtil.parseSimpleJson(readBody(exchange));
            String email = body.get("email");
            String password = body.get("password");

            try {
                User user = userDAO.authenticate(email, password);
                if (user != null) {
                    String json = String.format("{\"success\":true,\"user\":{\"userId\":%d,\"name\":\"%s\",\"email\":\"%s\",\"role\":\"%s\"}}",
                            user.getUserId(), JsonUtil.escape(user.getName()), JsonUtil.escape(user.getEmail()), user.getRole());
                    sendJson(exchange, 200, json);
                } else {
                    sendJson(exchange, 401, "{\"success\":false,\"message\":\"Invalid email or password\"}");
                }
            } catch (Exception ex) {
                sendJson(exchange, 500, "{\"error\":\"" + JsonUtil.escape(ex.getMessage()) + "\"}");
            }
        }
    }

    private class EventsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());
            String search = query.get("search");
            String cat = query.get("category");

            try {
                List<Event> list = eventDAO.getApprovedEvents(search, cat);
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    Event ev = list.get(i);
                    sb.append(String.format("{\"eventId\":%d,\"title\":\"%s\",\"description\":\"%s\",\"category\":\"%s\",\"date\":\"%s\",\"time\":\"%s\",\"location\":\"%s\",\"organizer\":\"%s\"}",
                            ev.getEventId(), JsonUtil.escape(ev.getTitle()), JsonUtil.escape(ev.getDescription()),
                            JsonUtil.escape(ev.getCategory()), ev.getEventDate(), ev.getEventTime(),
                            JsonUtil.escape(ev.getLocation()), JsonUtil.escape(ev.getOrganizerName())));
                    if (i < list.size() - 1) sb.append(",");
                }
                sb.append("]");
                sendJson(exchange, 200, sb.toString());
            } catch (Exception ex) {
                sendJson(exchange, 500, "{\"error\":\"" + JsonUtil.escape(ex.getMessage()) + "\"}");
            }
        }
    }

    private class AllEventsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                List<Event> list = eventDAO.getAllEventsForAdmin();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    Event ev = list.get(i);
                    sb.append(String.format("{\"eventId\":%d,\"title\":\"%s\",\"category\":\"%s\",\"date\":\"%s\",\"time\":\"%s\",\"location\":\"%s\",\"status\":\"%s\",\"organizer\":\"%s\"}",
                            ev.getEventId(), JsonUtil.escape(ev.getTitle()), JsonUtil.escape(ev.getCategory()),
                            ev.getEventDate(), ev.getEventTime(), JsonUtil.escape(ev.getLocation()),
                            ev.getApprovalStatus(), JsonUtil.escape(ev.getOrganizerName())));
                    if (i < list.size() - 1) sb.append(",");
                }
                sb.append("]");
                sendJson(exchange, 200, sb.toString());
            } catch (Exception ex) {
                sendJson(exchange, 500, "{\"error\":\"" + JsonUtil.escape(ex.getMessage()) + "\"}");
            }
        }
    }

    private class OrganizerEventsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());
            int orgId = Integer.parseInt(query.getOrDefault("id", "0"));
            try {
                List<Event> list = eventDAO.getEventsByOrganizer(orgId);
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    Event ev = list.get(i);
                    sb.append(String.format("{\"eventId\":%d,\"title\":\"%s\",\"category\":\"%s\",\"date\":\"%s\",\"time\":\"%s\",\"location\":\"%s\",\"status\":\"%s\"}",
                            ev.getEventId(), JsonUtil.escape(ev.getTitle()), JsonUtil.escape(ev.getCategory()),
                            ev.getEventDate(), ev.getEventTime(), JsonUtil.escape(ev.getLocation()),
                            ev.getApprovalStatus()));
                    if (i < list.size() - 1) sb.append(",");
                }
                sb.append("]");
                sendJson(exchange, 200, sb.toString());
            } catch (Exception ex) {
                sendJson(exchange, 500, "{\"error\":\"" + JsonUtil.escape(ex.getMessage()) + "\"}");
            }
        }
    }

    private class CreateEventHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            Map<String, String> body = JsonUtil.parseSimpleJson(readBody(exchange));
            try {
                int orgId = Integer.parseInt(body.get("organizerId"));
                String title = body.get("title");
                String cat = body.getOrDefault("category", "General");
                Date date = Date.valueOf(body.get("date"));
                Time time = Time.valueOf(body.get("time"));
                String loc = body.get("location");
                String desc = body.get("description");

                Event ev = new Event(0, orgId, title, desc, cat, date, time, loc, "Pending");
                boolean ok = eventDAO.createEvent(ev);
                sendJson(exchange, 200, "{\"success\":" + ok + ",\"eventId\":" + ev.getEventId() + "}");
            } catch (Exception ex) {
                sendJson(exchange, 400, "{\"error\":\"" + JsonUtil.escape(ex.getMessage()) + "\"}");
            }
        }
    }

    private class EventStatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            Map<String, String> body = JsonUtil.parseSimpleJson(readBody(exchange));
            try {
                int eventId = Integer.parseInt(body.get("eventId"));
                String status = body.get("status");
                boolean ok = eventDAO.updateApprovalStatus(eventId, status);
                sendJson(exchange, 200, "{\"success\":" + ok + "}");
            } catch (Exception ex) {
                sendJson(exchange, 400, "{\"error\":\"" + JsonUtil.escape(ex.getMessage()) + "\"}");
            }
        }
    }

    private class DeleteEventHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            Map<String, String> body = JsonUtil.parseSimpleJson(readBody(exchange));
            try {
                int eventId = Integer.parseInt(body.get("eventId"));
                int orgId = Integer.parseInt(body.getOrDefault("organizerId", "0"));
                boolean ok = eventDAO.deleteEvent(eventId, orgId);
                sendJson(exchange, 200, "{\"success\":" + ok + "}");
            } catch (Exception ex) {
                sendJson(exchange, 400, "{\"error\":\"" + JsonUtil.escape(ex.getMessage()) + "\"}");
            }
        }
    }

    private class TicketsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());
            int eventId = Integer.parseInt(query.getOrDefault("eventId", "0"));
            try {
                List<Ticket> list = ticketDAO.getTicketsByEvent(eventId);
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    Ticket t = list.get(i);
                    sb.append(String.format("{\"ticketId\":%d,\"eventId\":%d,\"type\":\"%s\",\"price\":%.2f,\"quantity\":%d}",
                            t.getTicketId(), t.getEventId(), JsonUtil.escape(t.getType()), t.getPrice(), t.getQuantity()));
                    if (i < list.size() - 1) sb.append(",");
                }
                sb.append("]");
                sendJson(exchange, 200, sb.toString());
            } catch (Exception ex) {
                sendJson(exchange, 500, "{\"error\":\"" + JsonUtil.escape(ex.getMessage()) + "\"}");
            }
        }
    }

    private class CreateTicketHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            Map<String, String> body = JsonUtil.parseSimpleJson(readBody(exchange));
            try {
                int eventId = Integer.parseInt(body.get("eventId"));
                String type = body.get("type");
                BigDecimal price = new BigDecimal(body.get("price"));
                int qty = Integer.parseInt(body.get("quantity"));

                Ticket t = new Ticket(0, eventId, type, price, qty);
                boolean ok = ticketDAO.createTicket(t);
                sendJson(exchange, 200, "{\"success\":" + ok + "}");
            } catch (Exception ex) {
                sendJson(exchange, 400, "{\"error\":\"" + JsonUtil.escape(ex.getMessage()) + "\"}");
            }
        }
    }

    private class DeleteTicketHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> body = JsonUtil.parseSimpleJson(readBody(exchange));
            try {
                int ticketId = Integer.parseInt(body.get("ticketId"));
                boolean ok = ticketDAO.deleteTicket(ticketId);
                sendJson(exchange, 200, "{\"success\":" + ok + "}");
            } catch (Exception ex) {
                sendJson(exchange, 400, "{\"error\":\"" + JsonUtil.escape(ex.getMessage()) + "\"}");
            }
        }
    }

    private class CreateBookingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            Map<String, String> body = JsonUtil.parseSimpleJson(readBody(exchange));
            try {
                String name = body.get("name");
                String email = body.get("email");
                int ticketId = Integer.parseInt(body.get("ticketId"));
                int qty = Integer.parseInt(body.getOrDefault("quantity", "1"));

                // ACID transaction processing
                Booking b = bookingDAO.processBooking(name, email, ticketId, qty);
                BookingItem item = b.getItems().get(0);

                String json = String.format("{\"success\":true,\"booking\":{\"bookingId\":%d,\"name\":\"%s\",\"email\":\"%s\",\"total\":%.2f,\"event\":\"%s\",\"ticketType\":\"%s\",\"quantity\":%d,\"date\":\"%s\"}}",
                        b.getBookingId(), JsonUtil.escape(b.getUserName()), JsonUtil.escape(b.getUserEmail()),
                        b.getTotalAmount(), JsonUtil.escape(item.getEventTitle()), JsonUtil.escape(item.getTicketType()),
                        item.getQuantity(), b.getBookingDate());
                sendJson(exchange, 200, json);
            } catch (Exception ex) {
                sendJson(exchange, 400, "{\"error\":\"" + JsonUtil.escape(ex.getMessage()) + "\"}");
            }
        }
    }

    private class FindBookingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());
            int id = Integer.parseInt(query.getOrDefault("id", "0"));
            try {
                Booking b = bookingDAO.getBookingById(id);
                if (b != null && !b.getItems().isEmpty()) {
                    BookingItem item = b.getItems().get(0);
                    String json = String.format("{\"found\":true,\"booking\":{\"bookingId\":%d,\"name\":\"%s\",\"email\":\"%s\",\"total\":%.2f,\"status\":\"%s\",\"event\":\"%s\",\"ticketType\":\"%s\",\"quantity\":%d,\"date\":\"%s\"}}",
                            b.getBookingId(), JsonUtil.escape(b.getUserName()), JsonUtil.escape(b.getUserEmail()),
                            b.getTotalAmount(), b.getStatus(), JsonUtil.escape(item.getEventTitle()),
                            JsonUtil.escape(item.getTicketType()), item.getQuantity(), b.getBookingDate());
                    sendJson(exchange, 200, json);
                } else {
                    sendJson(exchange, 404, "{\"found\":false,\"message\":\"Booking not found\"}");
                }
            } catch (Exception ex) {
                sendJson(exchange, 500, "{\"error\":\"" + JsonUtil.escape(ex.getMessage()) + "\"}");
            }
        }
    }

    private class AllBookingsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                List<BookingDAO.BookingRecord> list = bookingDAO.getAllBookingsForAdmin();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    BookingDAO.BookingRecord b = list.get(i);
                    sb.append(String.format("{\"bookingId\":%d,\"name\":\"%s\",\"email\":\"%s\",\"event\":\"%s\",\"ticketType\":\"%s\",\"quantity\":%d,\"total\":%.2f,\"date\":\"%s\",\"status\":\"%s\"}",
                            b.bookingId, JsonUtil.escape(b.userName), JsonUtil.escape(b.userEmail),
                            JsonUtil.escape(b.eventTitle), JsonUtil.escape(b.ticketType), b.quantity,
                            b.amount, b.bookingDate, b.status));
                    if (i < list.size() - 1) sb.append(",");
                }
                sb.append("]");
                sendJson(exchange, 200, sb.toString());
            } catch (Exception ex) {
                sendJson(exchange, 500, "{\"error\":\"" + JsonUtil.escape(ex.getMessage()) + "\"}");
            }
        }
    }

    private class OrganizerBookingsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());
            int orgId = Integer.parseInt(query.getOrDefault("id", "0"));
            try {
                List<BookingDAO.BookingRecord> list = bookingDAO.getBookingsByOrganizer(orgId);
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    BookingDAO.BookingRecord b = list.get(i);
                    sb.append(String.format("{\"bookingId\":%d,\"name\":\"%s\",\"email\":\"%s\",\"event\":\"%s\",\"ticketType\":\"%s\",\"quantity\":%d,\"total\":%.2f,\"date\":\"%s\"}",
                            b.bookingId, JsonUtil.escape(b.userName), JsonUtil.escape(b.userEmail),
                            JsonUtil.escape(b.eventTitle), JsonUtil.escape(b.ticketType), b.quantity,
                            b.amount, b.bookingDate));
                    if (i < list.size() - 1) sb.append(",");
                }
                sb.append("]");
                sendJson(exchange, 200, sb.toString());
            } catch (Exception ex) {
                sendJson(exchange, 500, "{\"error\":\"" + JsonUtil.escape(ex.getMessage()) + "\"}");
            }
        }
    }

    private class AdminStatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                DashboardStats stats = bookingDAO.getSystemStats();
                String json = String.format("{\"revenue\":%.2f,\"bookings\":%d,\"ticketsSold\":%d,\"totalEvents\":%d,\"approvedEvents\":%d,\"pendingEvents\":%d,\"organizers\":%d,\"attendees\":%d}",
                        stats.getTotalRevenue(), stats.getTotalBookings(), stats.getTicketsSold(),
                        stats.getTotalEvents(), stats.getApprovedEvents(), stats.getPendingEvents(),
                        stats.getTotalOrganizers(), stats.getTotalAttendees());
                sendJson(exchange, 200, json);
            } catch (Exception ex) {
                sendJson(exchange, 500, "{\"error\":\"" + JsonUtil.escape(ex.getMessage()) + "\"}");
            }
        }
    }

    private class OrganizerStatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());
            int orgId = Integer.parseInt(query.getOrDefault("id", "0"));
            try {
                DashboardStats stats = bookingDAO.getOrganizerStats(orgId);
                String json = String.format("{\"revenue\":%.2f,\"ticketsSold\":%d,\"totalEvents\":%d,\"approvedEvents\":%d}",
                        stats.getTotalRevenue(), stats.getTicketsSold(), stats.getTotalEvents(), stats.getApprovedEvents());
                sendJson(exchange, 200, json);
            } catch (Exception ex) {
                sendJson(exchange, 500, "{\"error\":\"" + JsonUtil.escape(ex.getMessage()) + "\"}");
            }
        }
    }

    private class AllOrganizersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                List<User> list = userDAO.findAllOrganizers();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    User u = list.get(i);
                    sb.append(String.format("{\"userId\":%d,\"name\":\"%s\",\"email\":\"%s\"}",
                            u.getUserId(), JsonUtil.escape(u.getName()), JsonUtil.escape(u.getEmail())));
                    if (i < list.size() - 1) sb.append(",");
                }
                sb.append("]");
                sendJson(exchange, 200, sb.toString());
            } catch (Exception ex) {
                sendJson(exchange, 500, "{\"error\":\"" + JsonUtil.escape(ex.getMessage()) + "\"}");
            }
        }
    }

    private class AddOrganizerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> body = JsonUtil.parseSimpleJson(readBody(exchange));
            try {
                String name = body.get("name");
                String email = body.get("email");
                String pass = body.getOrDefault("password", "organizer123");
                boolean ok = userDAO.addOrganizer(name, email, pass);
                sendJson(exchange, 200, "{\"success\":" + ok + "}");
            } catch (Exception ex) {
                sendJson(exchange, 400, "{\"error\":\"" + JsonUtil.escape(ex.getMessage()) + "\"}");
            }
        }
    }

    // ========================================================
    // UTILITIES
    // ========================================================

    private static String readBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int r;
        while ((r = is.read(buf)) != -1) baos.write(buf, 0, r);
        return baos.toString(StandardCharsets.UTF_8);
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String contentType, byte[] data) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, data.length);
        OutputStream os = exchange.getResponseBody();
        os.write(data);
        os.close();
    }

    private static void sendJson(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] data = json.getBytes(StandardCharsets.UTF_8);
        sendResponse(exchange, statusCode, "application/json; charset=UTF-8", data);
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
            String val = pair.length > 1 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "";
            map.put(key, val);
        }
        return map;
    }
}

