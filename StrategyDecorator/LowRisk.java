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

        if (startIndex < 0) return Signal.HOLD;

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

        if (currentClose > highestHigh) {
            boolean trendUp = currentClose > sma50;
            boolean volumeUp = currentVolume > averageVolume;

            double macdValue = calculateMACD(candles);
            double rsiValue = calculateRSI(candles, 14);

            if (trendUp && volumeUp) {
                if (macdValue > 0 && rsiValue < 70) {
                    System.out.println("LowRisk: Breakout + Vol + Trend UP + MACD(+) -> STRONG_BUY");
                    return Signal.STRONG_BUY;
                } else {
                    System.out.println("LowRisk: Breakout but RSI/MACD not optimal. Waiting.");
                }
            } else if (trendUp) {
                if (macdValue > 0 && rsiValue < 60) {
                    System.out.println("LowRisk: Breakout + Trend UP + MACD(+) -> BUY");
                    return Signal.BUY;
                }
            } else {
                 System.out.println("LowRisk: Breakout but Counter-Trend -> IGNORE");
            }
        } 
        else if (currentClose < lowestLow) {
            System.out.println("LowRisk: Destek Kırıldı -> SELL");
            return Signal.SELL;
        }

        double ema20 = calculateEMA(candles, 20);
        if (currentClose > ema20 * 1.012) {
             System.out.println("LowRisk: PUMP DETECTED! (Price > EMA20 + 1.2%) -> TAKE PROFIT NOW");
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

    private double calculateMACD(List<Candle> data) {
        if (data.size() < 26) return 0;
        double ema12 = calculateEMA(data, 12);
        double ema26 = calculateEMA(data, 26);
        return ema12 - ema26;
    }

    private double calculateEMA(List<Candle> data, int period) {
        double k = 2.0 / (period + 1);
        double ema = data.get(data.size() - period).close; 
        for (int i = data.size() - period + 1; i < data.size(); i++) {
            ema = data.get(i).close * k + ema * (1 - k);
        }
        return ema;
    }

    private double calculateRSI(List<Candle> data, int period) {
        if (data.size() <= period) return 50;
        double avgGain = 0, avgLoss = 0;
        int start = Math.max(0, data.size() - period - 1);
        for (int i = start + 1; i < data.size(); i++) {
            double change = data.get(i).close - data.get(i - 1).close;
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
        return "LowRisk (Vol + SMA50 Filter) + " + wrappedStrategy.getName();
    }
}
