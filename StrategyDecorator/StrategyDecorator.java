package StrategyDecorator;

import java.util.List;
import interfaces.TradingStrategy;
import models.*;

public abstract class StrategyDecorator implements TradingStrategy {

    protected TradingStrategy wrappedStrategy;

    public StrategyDecorator(TradingStrategy strategy) {
        this.wrappedStrategy = strategy;
    }

    @Override
    public Signal generateSignal(List<Price> prices) {
        return wrappedStrategy.generateSignal(prices);
    }
}
