package StrategyDecorator;

import java.util.List;
import interfaces.TradingStrategy;
import models.Candle;
import models.Signal;

public class HighRisk extends StrategyDecorator {

    private int period = 5; 

    public HighRisk(TradingStrategy strategy) {
        super(strategy);
    }

    @Override
    public Signal generateSignal(List<Candle> candles) {
        Signal baseSignal = super.generateSignal(candles);
        if (baseSignal != Signal.HOLD) {
            return baseSignal;
        }

        if (candles.size() < 15) {
            return Signal.HOLD;
        }

        double highestHigh = Double.NEGATIVE_INFINITY;
        double lowestLow = Double.MAX_VALUE;

        int endIndex = candles.size() - 2; 
        int startIndex = endIndex - period + 1;

        if (startIndex < 0) return Signal.HOLD;

        for (int i = startIndex; i <= endIndex; i++) {
            Candle c = candles.get(i);
            if (c.high > highestHigh) highestHigh = c.high;
            if (c.low < lowestLow) lowestLow = c.low;
        }

        double currentClose = candles.get(candles.size() - 1).close;
        double rsi = calculateRSI(candles, 14);

        if (currentClose > highestHigh) {
            if (rsi < 70) {
                System.out.println("HighRisk: Breakout UP + RSI Safe (" + String.format("%.2f", rsi) + ") -> BUY");
                return Signal.BUY;
            } else {
                System.out.println("HighRisk: Breakout UP but RSI Overbought (" + String.format("%.2f", rsi) + ") -> IGNORE");
            }
        } else if (currentClose < lowestLow) {
            System.out.println("HighRisk: Breakout DOWN -> SELL");
            return Signal.SELL;
        }

        return Signal.HOLD;
    }
    
    private double calculateRSI(List<Candle> data, int period) {
        if (data.size() <= period) return 50;
        
        double avgGain = 0, avgLoss = 0;
        for (int i = 1; i <= period; i++) {
            int idx = data.size() - 1 - period + i;
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
        return "HighRisk (Period: " + period + ", RSI Filter) + " + wrappedStrategy.getName();
    }
}
