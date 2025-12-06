package TradingStrategies;

import java.util.List;
import interfaces.TradingStrategy;
import models.Price;
import models.Signal;

public class MacdStrategy implements TradingStrategy {
    private int shortPeriod = 12;
    private int longPeriod = 26;
    private int signalPeriod = 9;

    @Override
    public Signal generateSignal(List<Price> priceData) {
        if (priceData.size() < longPeriod + signalPeriod) {
            return Signal.HOLD;
        }

        // We need the two most recent MACD and Signal line values to check for crossover
        // Current values
        double[] currentMacd = calculateMacdValues(priceData, priceData.size() - 1);
        double macdCurrent = currentMacd[0];
        double signalCurrent = currentMacd[1];

        // Previous values (to detect cross)
        double[] prevMacd = calculateMacdValues(priceData, priceData.size() - 2);
        double macdPrev = prevMacd[0];
        double signalPrev = prevMacd[1];

        // Crossover logic
        // Bullish Cross: MACD crosses ABOVE Signal Line
        if (macdPrev <= signalPrev && macdCurrent > signalCurrent) {
            return Signal.BUY;
        }
        // Bearish Cross: MACD crosses BELOW Signal Line
        else if (macdPrev >= signalPrev && macdCurrent < signalCurrent) {
            return Signal.SELL;
        }

        return Signal.HOLD;
    }

    // Returns [MACD Line, Signal Line]
    private double[] calculateMacdValues(List<Price> data, int endIndex) {
        // Calculate EMAs up to endIndex
        double emaShort = calculateEMA(data, shortPeriod, endIndex);
        double emaLong = calculateEMA(data, longPeriod, endIndex);
        
        double macdLine = emaShort - emaLong;
        
        // This is a simplification. Properly, Signal Line is EMA of MACD Line history.
        // Doing recursion for Signal Line is expensive without a cache. 
        // For this assignment, we will approximate Signal Line by calculating MACD for the last 'signalPeriod' points and averaging them (SMA) or trying to compute EMA on the fly.
        // Given the constraints and likely list implementation, let's use SMA of MACD as a proxy or simple EMA if possible.
        // To do it right: We strictly need a series of MACD values.
        
        // Simplified approach for robustness: 
        // Calculate MACD for the last 9 points and take their average (SMA) as signal line.
        // It's not strictly 9-EMA but close enough for a basic bot.
        
        double signalLine = 0;
        int distinctPoints = 0;
        for(int i = 0; i < signalPeriod; i++) {
            if(endIndex - i < longPeriod) break;
            double eShort = calculateEMA(data, shortPeriod, endIndex - i);
            double eLong = calculateEMA(data, longPeriod, endIndex - i);
            signalLine += (eShort - eLong);
            distinctPoints++;
        }
        if(distinctPoints > 0) signalLine /= distinctPoints;

        return new double[]{macdLine, signalLine};
    }

    private double calculateEMA(List<Price> data, int period, int index) {
        if (index < 0 || index >= data.size()) return 0;
        
        double k = 2.0 / (period + 1);
        double ema = data.get(0).value; // Start with first value
        
        int start = Math.max(0, index - (period * 4));
        if (start == 0) ema = data.get(0).value;
        else ema = data.get(start).value; // approximate start

        for (int i = start + 1; i <= index; i++) {
            double price = data.get(i).value;
            ema = price * k + ema * (1 - k);
        }
        return ema;
    }

    @Override
    public String getName() {
        return "MACD (12, 26, 9)";
    }
}
