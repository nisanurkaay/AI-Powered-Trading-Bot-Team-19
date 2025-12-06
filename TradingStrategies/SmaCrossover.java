package TradingStrategies;

import java.util.List;
import interfaces.TradingStrategy;
import models.Price;
import models.Signal;

public class SmaCrossover implements TradingStrategy {
    private final int shortWindow;
    private final int longWindow;

    public SmaCrossover(int shortWindow, int longWindow) {
        this.shortWindow = shortWindow;
        this.longWindow = longWindow;
    }

    @Override
    public Signal generateSignal(List<Price> prices) {
        if (prices.size() < longWindow) {
            return Signal.HOLD;
        }

        double shortSma = calculateSma(prices, shortWindow);
        double longSma = calculateSma(prices, longWindow);
        double prevShortSma = calculateSma(prices.subList(0, prices.size() - 1), shortWindow);
        double prevLongSma = calculateSma(prices.subList(0, prices.size() - 1), longWindow);

        System.out.println(String.format("SMA(%d): %.2f | SMA(%d): %.2f", shortWindow, shortSma, longWindow, longSma));

        // Crossover logic
        if (prevShortSma <= prevLongSma && shortSma > longSma) {
            return Signal.BUY;
        } else if (prevShortSma >= prevLongSma && shortSma < longSma) {
            return Signal.SELL;
        }

        return Signal.HOLD;
    }

    private double calculateSma(List<Price> prices, int window) {
        if (prices.size() < window) return 0;
        
        double sum = 0;
        for (int i = 0; i < window; i++) {
            sum += prices.get(prices.size() - 1 - i).value;
        }
        return sum / window;
    }
}
