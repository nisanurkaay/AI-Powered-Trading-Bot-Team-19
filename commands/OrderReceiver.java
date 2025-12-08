package commands;

import models.Order;
import models.Wallet;
import services.BinanceConfig;
import services.BinanceService;

public class OrderReceiver {

    public void placeBuyOrder(Order order, double currentPrice) {
        if (!BinanceConfig.isConfigured()) {
            // Simulation
            Wallet wallet = Wallet.getInstance();
            double cost = order.quantity * currentPrice;

            if (wallet.getUsdtBalance() >= cost) {
                wallet.withdrawUsdt(cost);
                wallet.depositBtc(order.quantity);
                System.out.println("SIMULATION BUY | Cost: " + String.format("%.2f", cost) + " USDT");
            } else {
                System.out.println("SIMULATION BUY FAILED | Insufficient USDT");
            }
        } else {
            // Real Execution
            BinanceService service = new BinanceService();
            service.placeOrder(order.symbol, "BUY", order.quantity);
        }
    }

    public void placeSellOrder(Order order, double currentPrice) {
        if (!BinanceConfig.isConfigured()) {
            // Simulation
            Wallet wallet = Wallet.getInstance();
            double cost = order.quantity * currentPrice;

            if (wallet.getBtcBalance() >= order.quantity) {
                wallet.withdrawBtc(order.quantity);
                wallet.depositUsdt(cost);
                System.out.println("SIMULATION SELL | Received: " + String.format("%.2f", cost) + " USDT");
            } else {
                System.out.println("SIMULATION SELL FAILED | Insufficient BTC");
            }
        } else {
            // Real Execution
            BinanceService service = new BinanceService();
            service.placeOrder(order.symbol, "SELL", order.quantity);
        }
    }
}
