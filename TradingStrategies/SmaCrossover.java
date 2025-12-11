package TradingStrategies;

import java.util.List;
import interfaces.TradingStrategy;
import models.Signal;
import models.Candle;

public class SmaCrossover implements TradingStrategy {
    private int shortPeriod;
    private int longPeriod;

    public SmaCrossover(int shortPeriod, int longPeriod) {
        this.shortPeriod = shortPeriod;
        this.longPeriod = longPeriod;
    }

    @Override
    public Signal generateSignal(List<Candle> candles) {
        if (candles.size() < longPeriod) {
            return Signal.HOLD;
        }

         
        double shortSmaCurrent = calculateSma(candles, shortPeriod, candles.size() - 1);
        double longSmaCurrent = calculateSma(candles, longPeriod, candles.size() - 1);
        
        double shortSmaPrev = calculateSma(candles, shortPeriod, candles.size() - 2);
        double longSmaPrev = calculateSma(candles, longPeriod, candles.size() - 2);
        
        if (shortSmaPrev <= longSmaPrev && shortSmaCurrent > longSmaCurrent) {
            return Signal.BUY;
        }
        
      
        if (shortSmaPrev >= longSmaPrev && shortSmaCurrent < longSmaCurrent) {
            return Signal.SELL;
        }

        return Signal.HOLD;
    }

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
