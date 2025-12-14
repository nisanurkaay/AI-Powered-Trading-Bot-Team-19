package StrategyDecorator;

import java.util.List;
import interfaces.TradingStrategy;
import models.Candle;
import models.Signal;

/**
 * Abstract base class for Strategy Decorators.
 * Follows the Decorator Pattern to add dynamic behaviors (Risk Management, Filters) 
 * to existing Trading Strategies without modifying them.
 */
public abstract class StrategyDecorator implements TradingStrategy {

    protected TradingStrategy wrappedStrategy;

    public StrategyDecorator(TradingStrategy strategy) {
        this.wrappedStrategy = strategy;
    }

    @Override
    public Signal generateSignal(List<Candle> candles) {
        return wrappedStrategy.generateSignal(candles);
    }
    @Override
    public String getName() {
        return this.getClass().getSimpleName() + " + " + wrappedStrategy.getName();
    }
}
