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
    
    // Bakiye Önbelleği (Grafiklerin akması ve API limitlerini korumak için)
    private double lastUsdtBalance = 0;
    private double lastBtcBalance = 0;
    private boolean isBalanceInitialized = false;

    @Override
    protected List<Candle> fetchData(Candle candle) {
        data.add(candle);
        // Keep memory usage in check (keep last 500 candles roughly)
        if (data.size() > 500) {
            data.remove(0);
        }
        return data; 
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

    private void updateBalance() {
        if (services.BinanceConfig.isConfigured()) {
            services.BinanceService service = new services.BinanceService();
            models.WalletBalance balance = service.getWalletBalance();
            lastUsdtBalance = balance.usdt;
            lastBtcBalance = balance.btc;
        } else {
            models.Wallet wallet = models.Wallet.getInstance();
            lastUsdtBalance = wallet.getUsdtBalance();
            lastBtcBalance = wallet.getBtcBalance();
        }
        isBalanceInitialized = true;
    }

    @Override
    protected Order createOrder(Signal signal) {
        String symbol = "BTCUSDT";
        
        if (signal == Signal.HOLD) {
            if (!isBalanceInitialized) updateBalance();
            return new Order(symbol, "HOLD", 0);
        }

        updateBalance();

        double currentPrice = data.isEmpty() ? 0 : data.get(data.size() - 1).close;
        if (currentPrice == 0) return new Order(symbol, "HOLD", 0);

        String side = "HOLD";
        double quantity = 0;

        switch (signal) {
            case STRONG_BUY:
                side = "BUY";
                quantity = (lastUsdtBalance * 0.98) / currentPrice;
                System.out.println(">>> STRONG_BUY: All In! (" + String.format("%.2f", lastUsdtBalance * 0.98) + " USDT)");
                break;
                
            case BUY:
                side = "BUY";
                quantity = (lastUsdtBalance * 0.40) / currentPrice;
                System.out.println(">>> BUY: Standard Entry (" + String.format("%.2f", lastUsdtBalance * 0.40) + " USDT)");
                break;

            case STRONG_SELL:
                side = "SELL";
                quantity = lastBtcBalance; 
                System.out.println(">>> STRONG_SELL: Panic Sell (All BTC)");
                break;

            case SELL:
                side = "SELL";
                quantity = lastBtcBalance * 0.50;
                System.out.println(">>> SELL: Take Profit (50% BTC)");
                break;
                
            default:
                side = "HOLD";
                quantity = 0;
                break;
        }
        
        if (!side.equals("HOLD") && (quantity * currentPrice < 5.0)) { 
             System.out.println("Order quantity too small (" + String.format("%.2f", quantity * currentPrice) + " USDT). Skipping.");
             return new Order(symbol, "HOLD", 0);
        }

        return new Order(symbol, side, quantity);
    }

    @Override
    protected void executeOrder(Order order) {
        if (order.side.equals("HOLD")) return;

        OrderReceiver receiver = new OrderReceiver();
        double currentPrice = data.isEmpty() ? 0 : data.get(data.size() - 1).close;

        OrderCommand command = null;

        if (order.side.equals("BUY")) {
            command = new BuyCommand(receiver, order, currentPrice);
        } else if (order.side.equals("SELL")) {
            command = new SellCommand(receiver, order, currentPrice);
        }

        if (command != null) {
            command.execute();
            updateBalance();
        }
    }

    @Override
    protected void logResult(Order order) {
        if (!order.side.equals("HOLD")) {
            System.out.println(order.toString());
            System.out.println(String.format(java.util.Locale.US, "Balance: %.2f USDT | %.5f BTC", lastUsdtBalance, lastBtcBalance));
        }
        
        double currentPrice = data.isEmpty() ? 0 : data.get(data.size() - 1).close;

        synchronized (services.LockService.fileLock) {
            try (PrintWriter out = new PrintWriter(new FileWriter("trades.csv", true))) {
                String timestamp = java.time.LocalDateTime.now().toString();
                out.println(String.format(java.util.Locale.US, "%s,%s,%s,%s,%.2f,%.2f,%.6f", 
                    timestamp, 
                    order.symbol, 
                    order.side, 
                    order.quantity,
                    currentPrice,
                    lastUsdtBalance, 
                    lastBtcBalance
                ));
            } catch (IOException e) {
                System.err.println("Error writing to CSV: " + e.getMessage());
            }
        }
    }
}
