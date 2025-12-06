package TradingStrategies;

import java.util.List;
import interfaces.TradingStrategy;
import models.Price;
import models.Signal;

public class DefaultStrategy implements TradingStrategy {
    
    @Override
    public Signal generateSignal(List<Price> priceData) {
        // Default behavior: HOLD
        // This strategy is a safe fallback that avoids making trades.
        return Signal.HOLD;
    }

    @Override
    public String getName() {
        return "Default (Hold)";
    }
}
