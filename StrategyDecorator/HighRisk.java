package StrategyDecorator;

import java.util.List;

import interfaces.TradingStrategy;
import models.*;

public class HighRisk extends StrategyDecorator {

    public HighRisk(TradingStrategy strategy) {
        super(strategy);
    }

    @Override
    public Signal generateSignal(List<Price> prices) {

        // Risk yönetimi burada yapılır:
        // Örnek: High risk → hiçbir filtre uygulama
        // ya da aggressive sinyal davranışı 

        return super.generateSignal(prices);
    }
}
