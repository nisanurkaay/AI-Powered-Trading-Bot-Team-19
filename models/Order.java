package models;

public class Order {
    public String symbol;
    public String side; // BUY, SELL, HOLD
    public double quantity;
    
    public Order(String symbol, String side, double quantity) {
        this.symbol = symbol;
        this.side = side;
        this.quantity = quantity;
    }
    
    @Override
    public String toString() {
        return "Order: " + side + " " + quantity + " " + symbol;
    }
}