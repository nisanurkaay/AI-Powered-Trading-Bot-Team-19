package TradingStrategies;

import java.util.List;
import interfaces.TradingStrategy;
import models.Signal;
import models.Candle;

/**
 * Simple Moving Average (SMA) Crossover Strategy.
 * A classic trend-following strategy.
 */
public class SmaCrossover implements TradingStrategy {
    private int shortPeriod; // Fast moving average (e.g., 50 days)
    private int longPeriod;  // Slow moving average (e.g., 200 days)

    /**
     * @param shortPeriod Period for the fast SMA.
     * @param longPeriod Period for the slow SMA.
     */
    public SmaCrossover(int shortPeriod, int longPeriod) {
        this.shortPeriod = shortPeriod;
        this.longPeriod = longPeriod;
    }

    /**
     * Generates a signal based on the crossover of two SMAs.
     * BUY (Golden Cross): Short SMA crosses above Long SMA.
     * SELL (Death Cross): Short SMA crosses below Long SMA.
     */
    @Override
    public Signal generateSignal(List<Candle> candles) {
        if (candles.size() < longPeriod) {
            return Signal.HOLD;
        }

         
        double shortSmaCurrent = calculateSma(candles, shortPeriod, candles.size() - 1);
        double longSmaCurrent = calculateSma(candles, longPeriod, candles.size() - 1);
        
        double shortSmaPrev = calculateSma(candles, shortPeriod, candles.size() - 2);
        double longSmaPrev = calculateSma(candles, longPeriod, candles.size() - 2);
        
        // Golden Cross: Short SMA crosses ABOVE Long SMA
        if (shortSmaPrev <= longSmaPrev && shortSmaCurrent > longSmaCurrent) {
            return Signal.BUY;
        }
        
      
        // Death Cross: Short SMA crosses BELOW Long SMA
        if (shortSmaPrev >= longSmaPrev && shortSmaCurrent < longSmaCurrent) {
            return Signal.SELL;
        }

        return Signal.HOLD;
    }

    /**
     * Calculates the Simple Moving Average.
     */
    private double calculateSma(List<Candle> candles, int window, int endIndex) {
        if (endIndex < window - 1) return 0;
        
        double sum = 0;
        for (int i = 0; i < window; i++) {
            sum += candles.get(endIndex - i).close;
        }
        return sum / window;
    }

    @Override
    public String getName() {
        return "SMA Crossover (" + shortPeriod + "/" + longPeriod + ")";
    }
}
