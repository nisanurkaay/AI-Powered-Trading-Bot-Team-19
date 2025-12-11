package StrategyDecorator;

import java.util.List;
import interfaces.TradingStrategy;
import models.Candle;
import models.Signal;

public class HighRisk extends StrategyDecorator {

    public HighRisk(TradingStrategy strategy) {
        super(strategy);
    }

    @Override
    public Signal generateSignal(List<Candle> candles) {
        // High risk logic could go here
        return super.generateSignal(candles);
    }
}
