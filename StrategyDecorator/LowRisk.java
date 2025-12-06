package StrategyDecorator;

import java.util.List;

import interfaces.TradingStrategy;
import models.*;

public class LowRisk extends StrategyDecorator {

    public LowRisk(TradingStrategy strategy) {
        super(strategy);
    }

    @Override
    public Signal generateSignal(List<Price> prices) {

        // Risk yönetimi burada yapılır:
        // Örnek: Low risk 

        return super.generateSignal(prices);
    }
}
