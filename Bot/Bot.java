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
        if (order.side.equals("HOLD")) return;

        boolean isReal = services.BinanceConfig.isConfigured();

        if (isReal) {
            // Real Execution on Binance (Testnet)
            services.BinanceService service = new services.BinanceService();
            service.placeOrder(order.symbol, order.side, order.quantity);
            // We don't manually update local wallet since we rely on API balance
        } else {
            // Simulation
            models.Wallet wallet = models.Wallet.getInstance();
            double currentPrice = data.get(data.size() - 1).value;
            double cost = order.quantity * currentPrice;

            if (order.side.equals("BUY")) {
                if (wallet.getUsdtBalance() >= cost) {
                    wallet.withdrawUsdt(cost);
                    wallet.depositBtc(order.quantity);
                    System.out.println("SIMULATION BUY | Cost: " + String.format("%.2f", cost) + " USDT");
                }
            } else if (order.side.equals("SELL")) {
                if (wallet.getBtcBalance() >= order.quantity) {
                    wallet.withdrawBtc(order.quantity);
                    wallet.depositUsdt(cost);
                    System.out.println("SIMULATION SELL | Received: " + String.format("%.2f", cost) + " USDT");
                }
            }
        }
    }

    @Override
    protected void logResult(Order order) {
        System.out.println(order.toString());
        
        double currentPrice = data.isEmpty() ? 0 : data.get(data.size() - 1).value;
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
