package TradingStrategies;

import java.util.List;
import interfaces.TradingStrategy;
import models.Signal;
import models.Candle;

public class DefaultStrategy implements TradingStrategy {
    
    // Risk Management Mode:
    // If no clear signal from other strategies, we manage existing positions.
    // Logic: If price drops below Dynamic Support (ATR based), SELL (Stop Loss / Trailing Stop)
    
    // The price level at which a trailing stop sell order triggers
    private double trailingStopPrice = -1;
    // Period for Average True Range calculation (volatility measure)
    private int atrPeriod = 14;
    // Multiplier for ATR to set the stop loss distance
    private double atrMultiplier = 2.0;

    /**
     * Generates a trading signal based on risk management rules.
     * Primarily focuses on crash protection and trailing stops.
     * 
     * @param candles The historical price data.
     * @return Signal.SELL if a crash or stop loss is detected, otherwise Signal.HOLD.
     */
    @Override
    public Signal generateSignal(List<Candle> candles) {
        if (candles.size() < atrPeriod + 1) {
            return Signal.HOLD;
        }

        double currentPrice = candles.get(candles.size() - 1).close;
        double atr = calculateATR(candles, candles.size() - 1);
        
        // TODO: Implement actual trailing stop logic using atrMultiplier
        
        // Implementing High Volatility Crash Protection here as a fallback
        double prevClose = candles.get(candles.size() - 2).close;
        double pctChange = (currentPrice - prevClose) / prevClose;
        
        if (pctChange < -0.08) { 
             // 8% sudden drop in 1 candle -> Panic Sell
             return Signal.SELL;
        }
        
        return Signal.HOLD;
    }
    
    /**
     * Calculates the Average True Range (ATR) to measure market volatility.
     * 
     * @param data The list of candles.
     * @param endIndex The index to calculate ATR for.
     * @return The ATR value.
     */
    private double calculateATR(List<Candle> data, int endIndex) {
        double sumTr = 0;
        for(int i=0; i<atrPeriod; i++) {
            int idx = endIndex - i;
            Candle curr = data.get(idx);
            Candle prev = data.get(idx - 1);
            
            double tr1 = curr.high - curr.low;
            double tr2 = Math.abs(curr.high - prev.close);
            double tr3 = Math.abs(curr.low - prev.close);
            double tr = Math.max(tr1, Math.max(tr2, tr3));
            sumTr += tr;
        }
        return sumTr / atrPeriod;
    }

    @Override
    public String getName() {
        return "Risk Manager (Default)";
    }
}
