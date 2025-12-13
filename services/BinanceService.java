package services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class BinanceService {

    private static long serverTimeOffset = 0;
    private static boolean isTimeSynced = false;

    public models.Candle getCandle(String symbol, String interval) {
        try {
            String endpoint = BinanceConfig.BASE_URL + "/api/v3/klines?symbol=" + symbol + "&interval=" + interval + "&limit=1";
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                conn.disconnect();

                return parseCandleFromJson(content.toString());
            } else {
                System.out.println("GET request failed. Response Code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void syncTime() {
        try {
            if (isTimeSynced) return;
            
            String endpoint = BinanceConfig.BASE_URL + "/api/v3/time";
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            
            if (conn.getResponseCode() == 200) {
                 BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                 String inputLine;
                 StringBuilder content = new StringBuilder();
                 while ((inputLine = in.readLine()) != null) {
                     content.append(inputLine);
                 }
                 in.close();
                 
                 // Payload: {"serverTime":1765619555123}
                 String json = content.toString();
                 String search = "\"serverTime\":";
                 int idx = json.indexOf(search);
                 if (idx != -1) {
                     int start = idx + search.length();
                     int end = json.indexOf("}", start);
                     if (end != -1) {
                         long serverTime = Long.parseLong(json.substring(start, end));
                         // local + offset = server => offset = server - local
                         // wait, usually we want: requestTimestamp = serverTime (roughly)
                         // requestTimestamp = localTime + offset
                         // offset = serverTime - localTime
                         serverTimeOffset = serverTime - System.currentTimeMillis();
                         isTimeSynced = true;
                         System.out.println("Time synced. Offset: " + serverTimeOffset + " ms");
                     }
                 }
            } else {
                System.err.println("Failed to sync time. Code: " + conn.getResponseCode());
            }
        } catch (Exception e) {
            System.err.println("Time sync error: " + e.getMessage());
        }
    }

    public void placeOrder(String symbol, String side, double quantity) {
        if (!BinanceConfig.isConfigured()) {
            System.out.println("SKIPPING ORDER: API Keys not configured in BinanceConfig.java");
            return;
        }

        try {
            if (!isTimeSynced) syncTime();
            
            String endpoint = "/api/v3/order";
            long timestamp = System.currentTimeMillis() + serverTimeOffset;
            String queryParams = "symbol=" + symbol + "&side=" + side + "&type=MARKET&quantity=" + quantity + "&timestamp=" + timestamp;
            
            String signature = hmacSha256(queryParams, BinanceConfig.SECRET_KEY);
            String fullQuery = queryParams + "&signature=" + signature;
            
            URL url = new URL(BinanceConfig.BASE_URL + endpoint + "?" + fullQuery);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("X-MBX-APIKEY", BinanceConfig.API_KEY);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setDoOutput(true);

            
            int responseCode = conn.getResponseCode();
            BufferedReader in;
            if (responseCode >= 200 && responseCode < 300) {
                in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                System.out.println("SUCCESS: " + side + " Order Placed!");
            } else {
                in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                System.out.println("FAILED: Could not place order.");
            }
            
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            System.out.println("Binance Response: " + content.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String hmacSha256(String data, String secret) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] raw = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(2 * raw.length);
            for (byte b : raw) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("Unable to sign message", e);
        }
    }

    private models.Candle parseCandleFromJson(String json) {
        try {
            json = json.trim();
            if (json.startsWith("[[") && json.endsWith("]]")) {
                json = json.substring(2, json.length() - 2);
            }
            
            String[] parts = json.split("\",\"");
            
              
            String[] rawValues = json.split(",");
            if (rawValues.length < 6) return null;

            long openTime = Long.parseLong(rawValues[0].replaceAll("\"", ""));
            double open = Double.parseDouble(rawValues[1].replaceAll("\"", ""));
            double high = Double.parseDouble(rawValues[2].replaceAll("\"", ""));
            double low = Double.parseDouble(rawValues[3].replaceAll("\"", ""));
            double close = Double.parseDouble(rawValues[4].replaceAll("\"", ""));
            double volume = Double.parseDouble(rawValues[5].replaceAll("\"", ""));
            long closeTime = Long.parseLong(rawValues[6].replaceAll("\"", ""));

            return new models.Candle(openTime, open, high, low, close, volume, closeTime);
        } catch (Exception e) {
            System.out.println("Error parsing Candle JSON: " + e.getMessage());
        }
        return null;
    }
    public models.WalletBalance getWalletBalance() {
        if (!BinanceConfig.isConfigured()) {
            return new models.WalletBalance(0, 0);
        }

        try {
            if (!isTimeSynced) syncTime();
            
            String endpoint = "/api/v3/account";
            long timestamp = System.currentTimeMillis() + serverTimeOffset;
            String queryParams = "timestamp=" + timestamp;
            String signature = hmacSha256(queryParams, BinanceConfig.SECRET_KEY);
            String fullQuery = queryParams + "&signature=" + signature;

            URL url = new URL(BinanceConfig.BASE_URL + endpoint + "?" + fullQuery);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("X-MBX-APIKEY", BinanceConfig.API_KEY);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder content = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                String json = content.toString();
                
                double usdt = parseAssetBalance(json, "USDT");
                double btc = parseAssetBalance(json, "BTC");
                return new models.WalletBalance(usdt, btc);
            } else {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                StringBuilder error = new StringBuilder();
                String line;
                while((line = in.readLine()) != null) error.append(line);
                in.close();
                System.out.println("Failed to fetch balance. Code: " + responseCode + " Msg: " + error.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new models.WalletBalance(0, 0);
    }

    private double parseAssetBalance(String json, String asset) {
        try {
            // Very naive JSON parsing to avoid pulling in external libraries effectively
            // Looking for "asset":"BTC",..."free":"0.001"
            String assetSearch = "\"asset\":\"" + asset + "\"";
            int assetIndex = json.indexOf(assetSearch);
            if (assetIndex != -1) {
                // Find "free" after asset
                String freeSearch = "\"free\":\"";
                int freeIndex = json.indexOf(freeSearch, assetIndex);
                if (freeIndex != -1) {
                    freeIndex += freeSearch.length();
                    int endIndex = json.indexOf("\"", freeIndex);
                    if (endIndex != -1) {
                        return Double.parseDouble(json.substring(freeIndex, endIndex));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error parsing balance for " + asset);
        }
        return 0.0;
    }
}
