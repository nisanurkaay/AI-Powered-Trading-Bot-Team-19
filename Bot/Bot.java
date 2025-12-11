package Bot;

import java.util.List;

import interfaces.TradingStrategy;
import interfaces.TradingTemplate;
import models.Candle;
import models.Order;
import models.Signal;
import commands.*;

public class Bot extends TradingTemplate {
    private BotConfig config = BotConfig.getInstance();
    private StrategySelector selector = new StrategySelector();

    @Override
    protected List<Candle> fetchData(Candle candle) {
        data.add(candle);
        // Keep memory usage in check (keep last 500 candles roughly)
        if (data.size() > 500) {
            data.remove(0);
        }
        return data; // Return full history for analysis
    }

    @Override
    protected Signal evaluateData(List<Candle> candles) {
         if (candles.isEmpty()) return Signal.HOLD;
         
         // Dynamic Strategy Selection
         TradingStrategy bestStrategy = selector.determineStrategy(candles);
         
         // Update Config (or just use local variable)
         if (config.strategy != bestStrategy) {
              config.strategy = bestStrategy;
         }
         
         return config.strategy.generateSignal(candles);
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
        if (order.side.equals("HOLD")) return;

        OrderReceiver receiver = new OrderReceiver();
        // Get current price for simulation purposes (logic inside Receiver)
        double currentPrice = data.isEmpty() ? 0 : data.get(data.size() - 1).close;

        OrderCommand command = null;

        if (order.side.equals("BUY")) {
            command = new BuyCommand(receiver, order, currentPrice);
        } else if (order.side.equals("SELL")) {
            command = new SellCommand(receiver, order, currentPrice);
        }

        if (command != null) {
            command.execute();
        }
    }

    @Override
    protected void logResult(Order order) {
        System.out.println(order.toString());
        
        double currentPrice = data.isEmpty() ? 0 : data.get(data.size() - 1).close;
        double usdtBalance = 0;
        double btcBalance = 0;

        if (services.BinanceConfig.isConfigured()) {
            services.BinanceService service = new services.BinanceService();
            models.WalletBalance balance = service.getWalletBalance();
            usdtBalance = balance.usdt;
            btcBalance = balance.btc;
        } else {
            models.Wallet wallet = models.Wallet.getInstance();
            usdtBalance = wallet.getUsdtBalance();
            btcBalance = wallet.getBtcBalance();
        }

        System.out.println(String.format(java.util.Locale.US, "Balance: %.2f USDT | %.5f BTC", usdtBalance, btcBalance));

        // CSV Logging
        synchronized (services.LockService.fileLock) {
            try (java.io.PrintWriter out = new java.io.PrintWriter(new java.io.FileWriter("trades.csv", true))) {
                String timestamp = java.time.LocalDateTime.now().toString();
                // Format: timestamp, symbol, side, quantity, price, usdtBalance, btcBalance
                out.println(String.format(java.util.Locale.US, "%s,%s,%s,%s,%.2f,%.2f,%.6f", 
                    timestamp, 
                    order.symbol, 
                    order.side, 
                    order.quantity,
                    currentPrice,
                    usdtBalance,
                    btcBalance
                ));
            } catch (java.io.IOException e) {
                System.err.println("Error writing to CSV: " + e.getMessage());
            }
        }
    }
}
