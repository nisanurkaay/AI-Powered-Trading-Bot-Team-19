package Bot;

import java.util.List;

import interfaces.TradingStrategy;
import interfaces.TradingTemplate;
import models.Order;
import models.Price;
import models.Signal;

public class Bot extends TradingTemplate {

    @Override
    protected List<Price> fetchData(Price price) {
        data.add(price);
        return data;
    }
    @Override
    protected Signal evaluateData(List<Price> prices) {
        BotConfig config = BotConfig.getInstance();
        TradingStrategy strategy = config.strategy;
        return strategy.generateSignal(data);
    }

    @Override
    protected Order createOrder(Signal signal) {
        String side = "HOLD";
        double quantity = 0.001; // Example quantity
        String symbol = "BTCUSDT";

        switch (signal) {
            case BUY:
                side = "BUY";
                break;
            case SELL:
                side = "SELL";
                break;
            default:
                side = "HOLD";
                break;
        }
        return new Order(symbol, side, quantity);
    }

    @Override
    protected void executeOrder(Order order) {
        if (!order.side.equals("HOLD")) {
            services.BinanceService service = new services.BinanceService();
            service.placeOrder(order.symbol, order.side, order.quantity);
        }
    }

    @Override
    protected void logResult(Order order) {
        System.out.println(order.toString());
        
        // CSV Logging
        try (java.io.PrintWriter out = new java.io.PrintWriter(new java.io.FileWriter("trades.csv", true))) {
            String timestamp = java.time.LocalDateTime.now().toString();
            out.println(timestamp + "," + order.symbol + "," + order.side + "," + order.quantity);
        } catch (java.io.IOException e) {
            System.err.println("Error writing to CSV: " + e.getMessage());
        }
    }
}
