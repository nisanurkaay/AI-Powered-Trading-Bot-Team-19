package TradingStrategies;

import java.util.List;
import interfaces.TradingStrategy;
import models.Price;
import models.Signal;

public class AdxStrategy implements TradingStrategy {
    private int period = 14;

    @Override
    public Signal generateSignal(List<Price> priceData) {
        if (priceData.size() < period * 2) {
            return Signal.HOLD;
        }

        AdxResult current = calculateADX(priceData, priceData.size() - 1);
        
        // Basic Logic:
        // If ADX > 25, the market is trending.
        // If +DI > -DI, it's an uptrend (BUY).
        // If -DI > +DI, it's a downtrend (SELL).
        
        if (current.adx > 25) {
            if (current.plusDI > current.minusDI) {
                return Signal.BUY;
            } else if (current.minusDI > current.plusDI) {
                return Signal.SELL;
            }
        }

        return Signal.HOLD;
    }

    private static class AdxResult {
        double adx;
        double plusDI;
        double minusDI;
    }

    private AdxResult calculateADX(List<Price> data, int endIndex) {
           
        double[] tr = new double[period];
        double[] plusDM = new double[period];
        double[] minusDM = new double[period];

        // Need at least 'period' data points ending at 'endIndex'
             
        // Let's look at the window [endIndex - period + 1, endIndex]
        
        double sumTR = 0, sumPlusDM = 0, sumMinusDM = 0;

        for (int i = 0; i < period; i++) {
            int idx = endIndex - i;
            if (idx <= 0) break;
            
            double high = data.get(idx).value; // approximating High/Low with Close since we only have 'value'
            double low = data.get(idx).value;  
            

            // +DM = max(current - prev, 0)
            // -DM = max(prev - current, 0)
            // TR = abs(current - prev)
            
            double prev = data.get(idx - 1).value;
            double curr = data.get(idx).value;
            
            double trVal = Math.abs(curr - prev);
            double pdm = (curr > prev) ? (curr - prev) : 0;
            double mdm = (prev > curr) ? (prev - curr) : 0; // if prev > curr, moved down
            
            if (pdm > mdm) mdm = 0;
            else if (mdm > pdm) pdm = 0;
            else { pdm = 0; mdm = 0; } // Inside move or equal
            
            sumTR += trVal;
            sumPlusDM += pdm;
            sumMinusDM += mdm;
        }

        // Avoid divide by zero
        if (sumTR == 0) sumTR = 1;

        double smoothedPlusDI = 100 * (sumPlusDM / sumTR);
        double smoothedMinusDI = 100 * (sumMinusDM / sumTR);

        double dx = 100 * Math.abs(smoothedPlusDI - smoothedMinusDI) / (smoothedPlusDI + smoothedMinusDI + 0.0001);
        
        AdxResult res = new AdxResult();
        res.adx = dx;
        res.plusDI = smoothedPlusDI;
        res.minusDI = smoothedMinusDI;
        return res;
    }

    @Override
    public String getName() {
        return "ADX Strategy (Simplified)";
    }
}
