package StrategyDecorator;

import java.util.List;
import interfaces.TradingStrategy;
import models.Candle;
import models.Signal;

public class LowRisk extends StrategyDecorator {

    public LowRisk(TradingStrategy strategy) {
        super(strategy);
    }

    @Override
    public Signal generateSignal(List<Candle> candles) {
        // Low risk logic could go here
        return super.generateSignal(candles);
    }
}
