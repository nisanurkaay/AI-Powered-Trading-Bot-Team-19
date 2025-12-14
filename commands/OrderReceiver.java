package commands;

import models.Order;
import models.Wallet;
import services.BinanceConfig;
import services.BinanceService;

/**
 * Receives and processes order execution requests.
 * Acts as the 'Receiver' in the Command Pattern.
 * Supports both Simulation (Paper Trading) and Real Execution modes.
 */
public class OrderReceiver {

    /**
     * Executes a BUY order.
     * If API keys are missing, simulates the trade using the local Wallet.
     * If configured, places a real market order on Binance.
     * 
     * @param order The order details.
     * @param currentPrice The current market price (used for simulation calculations).
     */
    public void placeBuyOrder(Order order, double currentPrice) {
        if (!BinanceConfig.isConfigured()) {
            // Simulation Mode: Deduct USDT, Add BTC
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
            // Real Execution Mode: Call Binance API
            BinanceService service = new BinanceService();
            service.placeOrder(order.symbol, "BUY", order.quantity);
        }
    }

    /**
     * Executes a SELL order.
     * If API keys are missing, simulates the trade using the local Wallet.
     * If configured, places a real market order on Binance.
     * 
     * @param order The order details.
     * @param currentPrice The current market price (used for simulation calculations).
     */
    public void placeSellOrder(Order order, double currentPrice) {
        if (!BinanceConfig.isConfigured()) {
            // Simulation Mode: Deduct BTC, Add USDT
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
            // Real Execution Mode: Call Binance API
            BinanceService service = new BinanceService();
            service.placeOrder(order.symbol, "SELL", order.quantity);
        }
    }
}
