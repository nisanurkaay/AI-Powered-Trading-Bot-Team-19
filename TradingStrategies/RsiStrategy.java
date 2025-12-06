package TradingStrategies;

import java.util.List;
import interfaces.TradingStrategy;
import models.Price;
import models.Signal;

public class RsiStrategy implements TradingStrategy {
    private int period = 14;
    private int overbought = 70;
    private int oversold = 30;

    @Override
    public Signal generateSignal(List<Price> priceData) {
        if (priceData.size() < period + 1) {
            return Signal.HOLD;
        }

        double rsi = calculateRSI(priceData, period);

        if (rsi <= oversold) {
            return Signal.BUY;
        } else if (rsi >= overbought) {
            return Signal.SELL;
        }

        return Signal.HOLD;
    }

    private double calculateRSI(List<Price> data, int n) {
        if (data.size() < n + 1) return 50.0;

        double totalGain = 0.0;
        double totalLoss = 0.0;

        // Calculate initial Average Gain/Loss
        for (int i = data.size() - n; i < data.size(); i++) {
            double change = data.get(i).value - data.get(i - 1).value;
            if (change > 0) {
                totalGain += change;
            } else {
                totalLoss += Math.abs(change);
            }
        }

        double avgGain = totalGain / n;
        double avgLoss = totalLoss / n;

        if (avgLoss == 0) return 100.0;
        
        double rs = avgGain / avgLoss;
        return 100.0 - (100.0 / (1.0 + rs));
    }

    @Override
    public String getName() {
        return "RSI Strategy (14)";
    }
}
