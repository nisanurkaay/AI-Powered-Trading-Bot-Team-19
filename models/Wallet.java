package models;

public class Wallet {
    private static Wallet instance;
    private double usdtBalance;
    private double btcBalance;

    private Wallet() {
        this.usdtBalance = 1000.0; // Initial simulated balance
        this.btcBalance = 0.0;
    }

    public static synchronized Wallet getInstance() {
        if (instance == null) {
            instance = new Wallet();
        }
        return instance;
    }

    public double getUsdtBalance() { return usdtBalance; }
    public double getBtcBalance() { return btcBalance; }

    public void depositUsdt(double amount) { this.usdtBalance += amount; }
    public void withdrawUsdt(double amount) { this.usdtBalance -= amount; }
    
    public void depositBtc(double amount) { this.btcBalance += amount; }
    public void withdrawBtc(double amount) { this.btcBalance -= amount; }
}
