package StrategyDecorator;

import java.util.List;
import interfaces.TradingStrategy;
import models.Candle;
import models.Signal;

public class CrashProtection extends StrategyDecorator {
    private final double dropThreshold; 

    public CrashProtection(TradingStrategy strategy, double dropThreshold) {
        super(strategy);
        this.dropThreshold = dropThreshold;
    }

    @Override
    public Signal generateSignal(List<Candle> candles) {
        if (candles.size() < 15) {
            return wrappedStrategy.generateSignal(candles);
        }

        Candle current = candles.get(candles.size() - 1);
        int lookback = Math.min(candles.size(), 5);
        Candle old = candles.get(candles.size() - lookback);

        double change = (current.close - old.close) / old.close;
        double rsi = calculateRSI(candles, 14);

        if (change < -dropThreshold) {
            if (rsi < 25) {
                System.out.println("CRASH PROTECTION: Drop detected (" + String.format("%.2f%%", change * 100) + ") BUT RSI is Oversold (" + String.format("%.2f", rsi) + "). HOLDING (Avoiding Panic Sell).");
                return Signal.HOLD; 
            }
            
            System.out.println("CRASH PROTECTION ENABLED: Price dropped " + String.format("%.2f%%", change * 100) + " (RSI: " + String.format("%.2f", rsi) + ")");
            return Signal.SELL;
        }

        return wrappedStrategy.generateSignal(candles);
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
}
