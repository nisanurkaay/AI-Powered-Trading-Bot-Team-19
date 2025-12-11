package TradingStrategies;

import java.util.List;
import interfaces.TradingStrategy;
import models.Signal;
import models.Candle;

public class MacdStrategy implements TradingStrategy {
    private int fastPeriod = 12;
    private int slowPeriod = 26;
    private int signalPeriod = 9;

    @Override
    public Signal generateSignal(List<Candle> candles) {
        if (candles.size() < slowPeriod + signalPeriod + 5) {
            return Signal.HOLD;
        }

        MacdResult current = calculateMACD(candles, candles.size() - 1);
        MacdResult previous = calculateMACD(candles, candles.size() - 2);

        // 1. Histogram Reversal: Histogram negative but rising toward zero (Bullish momentum building)
        boolean histogramReversalUp = current.histogram < 0 && 
                                      current.histogram > previous.histogram && 
                                      previous.histogram < previous_prev_histogram(candles); 

        if (histogramReversalUp) {
             return Signal.BUY; 
        }

        // Standard Crossover
        if (previous.macdLine < previous.signalLine && current.macdLine > current.signalLine) {
            return Signal.BUY;
        } else if (previous.macdLine > previous.signalLine && current.macdLine < current.signalLine) {
            return Signal.SELL;
        }

        return Signal.HOLD;
    }
    
    // Helper to get hist 3 steps back
    private double previous_prev_histogram(List<Candle> candles) {
         if (candles.size() < 3) return 0;
         return calculateMACD(candles, candles.size() - 3).histogram;
    }

    private static class MacdResult {
        double macdLine;
        double signalLine;
        double histogram;
    }

    private MacdResult calculateMACD(List<Candle> data, int endIndex) {
        double fastEma = calculateEMA(data, fastPeriod, endIndex);
        double slowEma = calculateEMA(data, slowPeriod, endIndex);
        
        double macdLine = fastEma - slowEma;
        
        // Simplified Signal Line calculation: Average of last 'signalPeriod' MACD values
        double signalLine = calculateSimpleSignalLine(data, signalPeriod, endIndex);

        MacdResult res = new MacdResult();
        res.macdLine = macdLine;
        res.signalLine = signalLine;
        res.histogram = macdLine - signalLine;
        return res;
    }

    private double calculateEMA(List<Candle> data, int period, int endIndex) {
        double k = 2.0 / (period + 1);
        double ema = data.get(endIndex - period + 1).close; // Start with SMA approximation
        
        // Simple scan to catch up EMA
        int start = Math.max(0, endIndex - (period * 3)); 
        ema = data.get(start).close;
        
        for (int i = start + 1; i <= endIndex; i++) {
            ema = data.get(i).close * k + ema * (1 - k);
        }
        return ema;
    }
    
    private double calculateSimpleSignalLine(List<Candle> data, int period, int endIndex) {
         double sum = 0;
         int count = 0;
         for(int i = 0; i < period; i++) {
             count++;
             double fast = calculateEMA(data, fastPeriod, endIndex - i);
             double slow = calculateEMA(data, slowPeriod, endIndex - i);
             sum += (fast - slow);
         }
         return (count > 0) ? sum / count : 0;
    }

    @Override
    public String getName() {
        return "MACD Strategy";
    }
}
