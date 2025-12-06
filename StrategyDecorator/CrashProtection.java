package StrategyDecorator;

import java.util.List;
import interfaces.TradingStrategy;
import models.Price;
import models.Signal;

public class CrashProtection extends StrategyDecorator {
    private final double dropThreshold; // e.g., 0.05 for 5% drop

    public CrashProtection(TradingStrategy strategy, double dropThreshold) {
        super(strategy);
        this.dropThreshold = dropThreshold;
    }

    @Override
    public Signal generateSignal(List<Price> prices) {
        if (prices.size() < 2) {
            return wrappedStrategy.generateSignal(prices);
        }

        Price current_price = prices.get(prices.size() - 1);
        // Look back 5 ticks or less
        int lookback = Math.min(prices.size(), 5);
        Price old_price = prices.get(prices.size() - lookback);

        double change = (current_price.value - old_price.value) / old_price.value;

        if (change < -dropThreshold) {
            System.out.println("CRASH PROTECTION ENABLED: Price dropped " + String.format("%.2f%%", change * 100));
            return Signal.SELL;
        }

        return wrappedStrategy.generateSignal(prices);
    }
}
