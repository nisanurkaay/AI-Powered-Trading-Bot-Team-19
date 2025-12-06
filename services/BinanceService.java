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

    public double getPrice(String symbol) {
        try {
            String endpoint = BinanceConfig.BASE_URL + "/api/v3/ticker/price?symbol=" + symbol;
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

                return parsePriceFromJson(content.toString());
            } else {
                System.out.println("GET request failed. Response Code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void placeOrder(String symbol, String side, double quantity) {
        if (!BinanceConfig.isConfigured()) {
            System.out.println("SKIPPING ORDER: API Keys not configured in BinanceConfig.java");
            return;
        }

        try {
            String endpoint = "/api/v3/order";
            long timestamp = System.currentTimeMillis();
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

    private double parsePriceFromJson(String json) {
        try {
            String searchKey = "\"price\":\"";
            int startIndex = json.indexOf(searchKey);
            if (startIndex != -1) {
                startIndex += searchKey.length();
                int endIndex = json.indexOf("\"", startIndex);
                if (endIndex != -1) {
                    String priceStr = json.substring(startIndex, endIndex);
                    return Double.parseDouble(priceStr);
                }
            }
        } catch (Exception e) {
            System.out.println("Error parsing JSON: " + e.getMessage());
        }
        return -1;
    }
}
