package StrategyDecorator;

import java.util.List;
import interfaces.TradingStrategy;
import models.Candle;
import models.Signal;

public class LowRisk extends StrategyDecorator {

    private int period = 20; 

    public LowRisk(TradingStrategy strategy) {
        super(strategy);
    }

    @Override
    public Signal generateSignal(List<Candle> candles) {
        Signal baseSignal = super.generateSignal(candles);
        if (baseSignal != Signal.HOLD) {
            return baseSignal;
        }

        if (candles.size() < 50) {
            return Signal.HOLD;
        }

        double highestHigh = Double.NEGATIVE_INFINITY;
        double lowestLow = Double.MAX_VALUE;
        double totalVolume = 0;

        int endIndex = candles.size() - 2; 
        int startIndex = endIndex - period + 1;

        for (int i = startIndex; i <= endIndex; i++) {
            Candle c = candles.get(i);
            if (c.high > highestHigh) highestHigh = c.high;
            if (c.low < lowestLow) lowestLow = c.low;
            totalVolume += c.volume;
        }

        double averageVolume = totalVolume / period;
        Candle currentCandle = candles.get(candles.size() - 1);
        double currentClose = currentCandle.close;
        double currentVolume = currentCandle.volume;

        double sma50 = calculateSMA(candles, 50);

        if (currentClose > highestHigh && currentVolume > averageVolume) {
            if (currentClose > sma50) {
                System.out.println("LowRisk: Breakout + Vol + Trend UP -> BUY");
                return Signal.BUY;
            } else {
                System.out.println("LowRisk: Breakout detected but below SMA50 (Counter-Trend) -> IGNORE");
            }
        } 
        else if (currentClose < lowestLow) {
            System.out.println("LowRisk: Destek Kırıldı -> SELL");
            return Signal.SELL;
        }

        return Signal.HOLD;
    }

    private double calculateSMA(List<Candle> candles, int period) {
        double sum = 0;
        for (int i = 0; i < period; i++) {
            sum += candles.get(candles.size() - 1 - i).close;
        }
        return sum / period;
    }

    @Override
    public String getName() {
        return "LowRisk (Vol + SMA50 Filter) + " + wrappedStrategy.getName();
    }
}
