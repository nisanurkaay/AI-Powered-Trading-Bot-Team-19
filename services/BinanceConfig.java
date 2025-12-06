package services;

public class BinanceConfig {
    // "https://testnet.binance.vision" for Safe Testing
    public static final String BASE_URL = "https://testnet.binance.vision"; 
    
    public static String API_KEY = "YOUR_API_KEY_HERE";
    public static String SECRET_KEY = "YOUR_SECRET_KEY_HERE";
    
    static {
        loadEnv();
    }

    private static void loadEnv() {
        try {
            java.io.File envFile = new java.io.File(".env");
            if (envFile.exists()) {
                java.util.Scanner scanner = new java.util.Scanner(envFile);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("BINANCE_API_KEY=")) {
                        API_KEY = line.substring("BINANCE_API_KEY=".length()).trim();
                    } else if (line.startsWith("BINANCE_SECRET_KEY=")) {
                        SECRET_KEY = line.substring("BINANCE_SECRET_KEY=".length()).trim();
                    }
                }
                scanner.close();
            } else {
                System.out.println("Warning: .env file not found.");
            }
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public static boolean isConfigured() {
        return !API_KEY.equals("YOUR_API_KEY_HERE") && !SECRET_KEY.equals("YOUR_SECRET_KEY_HERE");
    }
}
