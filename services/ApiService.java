package services;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.InetSocketAddress;

import Bot.BotConfig;
import TradingStrategies.*;
import StrategyDecorator.*;
import interfaces.TradingStrategy;
import java.io.InputStreamReader;

public class ApiService {
    
    public void start(int port) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/api/trades", new TradesHandler());
            server.createContext("/api/strategy", new StrategyHandler());
            server.setExecutor(null); // creates a default executor
            server.start();
            System.out.println("API Server started on port " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class TradesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            // CORS headers
            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS, POST");
            t.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
            
            // Cache Control
            t.getResponseHeaders().add("Cache-Control", "no-cache, no-store, must-revalidate");
            t.getResponseHeaders().add("Pragma", "no-cache");
            t.getResponseHeaders().add("Expires", "0");

            if (t.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                t.sendResponseHeaders(204, -1);
                return;
            }

            String response = getTradesAsJson();
            t.getResponseHeaders().add("Content-Type", "application/json");
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private String getTradesAsJson() {
            StringBuilder json = new StringBuilder("[");
            synchronized (services.LockService.fileLock) {
                try (BufferedReader br = new BufferedReader(new FileReader("trades.csv"))) {
                    String line;
                    boolean first = true;
                    while ((line = br.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;
                        String[] parts = line.split(",");
                        if (parts.length < 4) continue;
                        
                        if (!first) json.append(",");
                        
                        String price = parts.length > 4 ? parts[4].trim() : "0";
                        String usdt = parts.length > 5 ? parts[5].trim() : "0";
                        String btc = parts.length > 6 ? parts[6].trim() : "0";
                        
                        json.append(String.format("{\"timestamp\":\"%s\",\"symbol\":\"%s\",\"side\":\"%s\",\"quantity\":\"%s\",\"price\":%s,\"usdt\":%s,\"btc\":%s}", 
                            parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim(), price, usdt, btc));
                        
                        first = false;
                    }
                } catch (IOException e) {
                    System.err.println("API Error reading CSV: " + e.getMessage());
                    return "[]";
                }
            }
            json.append("]");
            return json.toString();
        }
    }

    static class StrategyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            // CORS headers
            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            t.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");

            if (t.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                t.sendResponseHeaders(204, -1);
                return;
            }

            if (t.getRequestMethod().equalsIgnoreCase("GET")) {
                String currentStrategyName = BotConfig.getInstance().strategy.getName();
                String response = "{\"name\": \"" + currentStrategyName + "\"}";
                t.getResponseHeaders().add("Content-Type", "application/json");
                t.sendResponseHeaders(200, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else if (t.getRequestMethod().equalsIgnoreCase("POST")) {
                // Read request body
                StringBuilder textBuilder = new StringBuilder();
                try (java.util.Scanner scanner = new java.util.Scanner(t.getRequestBody())) {
                    while (scanner.hasNext()) {
                        textBuilder.append(scanner.next());
                    }
                }
                String body = textBuilder.toString();
                
                // Very basic parsing
                boolean sma = body.contains("SmaCrossover");
                boolean trend = body.contains("TrendFollowing");
                
                boolean crash = body.contains("CrashProtection");
                boolean high = body.contains("HighRisk");
                boolean low = body.contains("LowRisk");

                TradingStrategy baseStrategy;
                if (trend) {
                    baseStrategy = new TrendFollowing();
                } else {
                    baseStrategy = new SmaCrossover(5, 10);
                }

                TradingStrategy finalStrategy = baseStrategy;
                if (crash) {
                    finalStrategy = new CrashProtection(baseStrategy, 0.02);
                } else if (high) {
                    finalStrategy = new HighRisk(baseStrategy);
                } else if (low) {
                    finalStrategy = new LowRisk(baseStrategy);
                }

                BotConfig.getInstance().strategy = finalStrategy;

                String response = "{\"status\": \"updated\", \"name\": \"" + finalStrategy.getName() + "\"}";
                t.getResponseHeaders().add("Content-Type", "application/json");
                t.sendResponseHeaders(200, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }
}
