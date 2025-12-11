package TradingStrategies;

import java.util.List;
import interfaces.TradingStrategy;
import models.Signal;
import models.Candle;

public class TrendFollowing implements TradingStrategy {
    
    @Override
    public Signal generateSignal(List<Candle> candles) {
        if (candles.size() < 2) {
            return Signal.HOLD;
        }

        double currentPrice = candles.get(candles.size() - 1).close;
        double previousPrice = candles.get(candles.size() - 2).close;

        if (currentPrice > previousPrice) {
            return Signal.BUY;
        } else if (currentPrice < previousPrice) {
            return Signal.SELL;
        }

        return Signal.HOLD;
    }

    @Override
    public String getName() {
        return "Trend Following (Simple)";
    }
}