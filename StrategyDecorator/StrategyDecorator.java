package StrategyDecorator;

import java.util.List;
import interfaces.TradingStrategy;
import models.Candle;
import models.Signal;

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
