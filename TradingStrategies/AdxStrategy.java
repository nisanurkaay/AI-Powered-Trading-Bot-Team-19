package TradingStrategies;

import java.util.List;
import interfaces.TradingStrategy;
import models.Signal;
import models.Candle;

public class AdxStrategy implements TradingStrategy {
    private int period = 14;

    @Override
    public Signal generateSignal(List<Candle> candles) {
        if (candles.size() < period * 2) {
            return Signal.HOLD;
        }

        AdxResult current = calculateADX(candles, candles.size() - 1);
        
        // ADX > 25: Strong Trend
        // PDI > MDI: Uptrend
        // MDI > PDI: Downtrend
        // Slope Logic: Check if ADX is rising
        
        AdxResult previous = calculateADX(candles, candles.size() - 2);
        boolean adxRising = current.adx > previous.adx;
        
        if (current.adx > 25 && adxRising) {
            if (current.plusDI > current.minusDI) {
                return Signal.BUY; // Strong Uptrend
            } else if (current.minusDI > current.plusDI) {
                return Signal.SELL; // Strong Downtrend
            }
        } else if (current.adx < 20) {
            // Weak Trend / Ranging
            return Signal.HOLD;
        }

        return Signal.HOLD;
    }

    private static class AdxResult {
        double adx;
        double plusDI;
        double minusDI;
    }

    private AdxResult calculateADX(List<Candle> data, int endIndex) {
           


        double sumTR = 0, sumPlusDM = 0, sumMinusDM = 0;

        for (int i = 0; i < period; i++) {
            int idx = endIndex - i;
            if (idx <= 0) break;
            
            Candle curr = data.get(idx);
            Candle prev = data.get(idx - 1);
            
            // True Range
            double tr1 = curr.high - curr.low;
            double tr2 = Math.abs(curr.high - prev.close);
            double tr3 = Math.abs(curr.low - prev.close);
            double trueRange = Math.max(tr1, Math.max(tr2, tr3));
            
            // Directional Movement
            double upMove = curr.high - prev.high;
            double downMove = prev.low - curr.low;
            
            double pdm = 0;
            double mdm = 0;
            
            if (upMove > downMove && upMove > 0) {
                pdm = upMove;
            }
            if (downMove > upMove && downMove > 0) {
                mdm = downMove;
            }
            
            sumTR += trueRange;
            sumPlusDM += pdm;
            sumMinusDM += mdm;
        }

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
        return "ADX Strategy";
    }
}
