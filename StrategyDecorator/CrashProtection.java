package StrategyDecorator;

import java.util.List;
import interfaces.TradingStrategy;
import models.Candle;
import models.Signal;

public class CrashProtection extends StrategyDecorator {
    private final double dropThreshold; // e.g., 0.05 for 5% drop

    public CrashProtection(TradingStrategy strategy, double dropThreshold) {
        super(strategy);
        this.dropThreshold = dropThreshold;
    }

    @Override
    public Signal generateSignal(List<Candle> candles) {
        if (candles.size() < 2) {
            return wrappedStrategy.generateSignal(candles);
        }

        Candle current = candles.get(candles.size() - 1);
        // Look back 5 ticks or less
        int lookback = Math.min(candles.size(), 5);
        Candle old = candles.get(candles.size() - lookback);

        double change = (current.close - old.close) / old.close;

        // Check for sudden drop
        if (change < -dropThreshold) {
            System.out.println("CRASH PROTECTION ENABLED: Price dropped " + String.format("%.2f%%", change * 100));
            return Signal.SELL;
        }

        return wrappedStrategy.generateSignal(candles);
    }
}
