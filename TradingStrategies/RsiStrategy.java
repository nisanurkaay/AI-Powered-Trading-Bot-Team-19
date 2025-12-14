package TradingStrategies;

import java.util.List;
import interfaces.TradingStrategy;
import models.Signal;
import models.Candle;

/**
 * RSI (Relative Strength Index) Strategy.
 * A momentum oscillator that measures the speed and change of price movements.
 * Used primarily to identify Overbought and Oversold conditions.
 */
public class RsiStrategy implements TradingStrategy {
    private int period = 14;

    @Override
    public Signal generateSignal(List<Candle> candles) {
        if (candles.size() < period + 1) {
            return Signal.HOLD;
        }

        double rsi = calculateRSI(candles, candles.size() - 1);
        
        // Identify Trend via simple SMA check (using 50 SMA)
        boolean isUptrend = isUptrend(candles);

        // Strategy Logic:
        // 1. Trend Filter: Use SMA 50 to determine the dominant market trend.
        // 2. Dynamic Thresholds: Adjust Buy/Sell levels based on the trend.
        
        // Uptrend (Bullish):
        // - Buy on Dips (RSI < 45) -> Aggressive Entry
        // - Sell at Extreme highs (RSI > 80)
        
        // Downtrend/Sideways (Bearish):
        // - Buy only at Extreme lows (RSI < 30) -> Conservative Entry
        // - Sell early (RSI > 70)
        
        double buyThreshold = isUptrend ? 45.0 : 30.0;
        double sellThreshold = isUptrend ? 80.0 : 70.0;
        
        if (rsi < buyThreshold) {
            return Signal.BUY;
        } else if (rsi > sellThreshold) {
            return Signal.SELL;
        }

        return Signal.HOLD;
    }
    
    /**
     * Checks if the market is in an uptrend using a simple 50-period SMA.
     */
    private boolean isUptrend(List<Candle> candles) {
        if (candles.size() < 50) return false;
        double sma50 = 0;
        for (int i = 0; i < 50; i++) {
            sma50 += candles.get(candles.size() - 1 - i).close;
        }
        sma50 /= 50;
        return candles.get(candles.size() - 1).close > sma50;
    }

    /**
     * Calculates the Relative Strength Index (RSI).
     * 
     * @param data The historical candles.
     * @param endIndex The index to calculate for.
     * @return The RSI value (0-100).
     */
    private double calculateRSI(List<Candle> data, int endIndex) {
        double avgGain = 0, avgLoss = 0;
        
        // Needs proper sequence calculation usually, simplified for length
        for (int i = 1; i <= period; i++) {
            int idx = endIndex - period + i;
            double change = data.get(idx).close - data.get(idx - 1).close;
            if (change > 0) avgGain += change;
            else avgLoss += Math.abs(change);
        }
        
        avgGain /= period;
        avgLoss /= period;

        if (avgLoss == 0) return 100;
        double rs = avgGain / avgLoss;
        return 100 - (100 / (1 + rs));
    }

    @Override
    public String getName() {
        return "RSI Strategy";
    }
}
