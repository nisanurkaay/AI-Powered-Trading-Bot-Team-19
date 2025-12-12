package StrategyDecorator;

import java.util.List;
import interfaces.TradingStrategy;
import models.Candle;
import models.Signal;

public class HighRisk extends StrategyDecorator {

    private int period = 15; 

    public HighRisk(TradingStrategy strategy) {
        super(strategy);
    }

    @Override
    public Signal generateSignal(List<Candle> candles) {
        Signal baseSignal = super.generateSignal(candles);
        if (baseSignal != Signal.HOLD) {
            return baseSignal;
        }

        if (candles.size() < 20) { // Uyuşmazlık kontrolü için biraz daha veri lazım
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
        double currentRsi = calculateRSI(candles, 14);

        if (detectBullishDivergence(candles)) {
            System.out.println("HighRisk: BULLISH DIVERGENCE DETECTED! (Price Down, RSI Up) -> STRONG_BUY");
            return Signal.STRONG_BUY;
        }

        double totalVolume = 0;
        for(int i = 0; i < 15; i++) {
            totalVolume += candles.get(candles.size() - 1 - i).volume;
        }
        double avgVolume = totalVolume / 15;
        double currentVolume = candles.get(candles.size() - 1).volume;

        double macdValue = calculateMACD(candles);
        
        if (currentClose > highestHigh) {
            if (currentVolume > avgVolume * 1.2 && macdValue > 0) {
                if (currentRsi < 40) {
                    System.out.println("HighRisk: Breakout + High Vol + MACD(+) + Oversold RSI -> STRONG_BUY");
                    return Signal.STRONG_BUY;
                } else if (currentRsi < 70) {
                    System.out.println("HighRisk: Breakout + High Vol + MACD(+) -> BUY");
                    return Signal.BUY;
                }
            } else {
                System.out.println("HighRisk: Breakout detected but Weak Indicators (Vol/MACD). Ignored.");
            }
        } else if (currentClose < lowestLow * 0.995) {
            System.out.println("HighRisk: Breakout DOWN (Toleranslı) -> SELL");
            return Signal.SELL;
        }

        double ema20 = calculateEMA(candles, 20);
        if (currentClose > ema20 * 1.015) {
             System.out.println("HighRisk: PUMP DETECTED! (Price > EMA20 + 1.5%) -> TAKE PROFIT NOW");
             return Signal.SELL;
        }

        return Signal.HOLD;
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
    
    private boolean detectBullishDivergence(List<Candle> candles) {
        int lookback = 10;
        if (candles.size() < lookback + 5) return false;

        double priceLow = Double.MAX_VALUE;
        double rsiAtPriceLow = 100;
        int lowIndex = -1;

        for (int i = 1; i <= lookback; i++) {
            int idx = candles.size() - 1 - i;
            if (candles.get(idx).low < priceLow) {
                priceLow = candles.get(idx).low;
                lowIndex = idx;
                rsiAtPriceLow = calculateRSI(candles.subList(0, idx + 1), 14); 
            }
        }

        double currentPriceLow = candles.get(candles.size() - 1).low;
        double currentRsi = calculateRSI(candles, 14);

        if (currentPriceLow < priceLow && currentRsi > rsiAtPriceLow) {
            if (currentRsi < 55) {
                return true;
            }
        }
        return false;
    }

    private double calculateRSI(List<Candle> data, int period) {
        if (data.size() <= period) return 50;
        
        double avgGain = 0, avgLoss = 0;
        // Son 'period' kadar veriyi al
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
        return "HighRisk (Div + RSI + MACD) + " + wrappedStrategy.getName();
    }
}
