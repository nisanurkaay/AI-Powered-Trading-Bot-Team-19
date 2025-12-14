package TradingStrategies;

import java.util.List;
import interfaces.TradingStrategy;
import models.Signal;
import models.Candle;

/**
 * ADX (Average Directional Index) Strategy.
 * Measures trend strength and direction to identify strong trends.
 */
public class AdxStrategy implements TradingStrategy {
    private int period = 14;

    /**
     * Generates a signal based on ADX strength and DI crossovers.
     * ADX > 25 indicates a strong trend.
     * DI+ > DI- indicates an Uptrend.
     * DI- > DI+ indicates a Downtrend.
     */
    public Signal generateSignal(List<Candle> candles) {
        if (candles.size() < period * 2) {
            return Signal.HOLD;
        }

        AdxResult current = calculateADX(candles, candles.size() - 1);
        
        // Strategy Logic:
        // 1. Trend Strength: ADX > 25
        // 2. Trend Direction: Determined by PlusDI (Bullish) vs MinusDI (Bearish)
        // 3. Momentum: ADX should be rising (current > previous)
        
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

    /**
     * Calculates the ADX, PlusDI, and MinusDI values.
     * 
     * @param data The historical price data.
     * @param endIndex The index to calculate for.
     * @return An AdxResult object containing the calculated indicators.
     */
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
