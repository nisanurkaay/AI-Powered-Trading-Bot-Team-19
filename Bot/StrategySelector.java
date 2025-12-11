package Bot;

import java.util.List;
import models.Candle;
import interfaces.TradingStrategy;
import TradingStrategies.*;

public class StrategySelector {
    
    // Strategies
    private TradingStrategy trendStrategy;
    private TradingStrategy meanReversionStrategy;
    private TradingStrategy riskManagementStrategy;
    
    // Market Condition Indicators
    private AdxStrategy adxAnalyzer;
    
    private int cooldown = 0;
    private final int COOLDOWN_PERIOD = 5; // 5 candles before switching again
    
    private TradingStrategy currentStrategy;
    
    public StrategySelector() {
        // Initialize Strategies
        this.trendStrategy = new MacdStrategy(); 
        this.meanReversionStrategy = new RsiStrategy();
        this.riskManagementStrategy = new DefaultStrategy(); 
        
        // Initial Strategy
        this.currentStrategy = this.riskManagementStrategy;
        
        this.adxAnalyzer = new AdxStrategy(); 
    }
    
    public TradingStrategy determineStrategy(List<Candle> candles) {
        if (candles.size() < 30) return riskManagementStrategy; // Not enough data
        
        if (cooldown > 0) {
            cooldown--;
            return currentStrategy;
        }
        
        // Analyze Market Conditions
        double adxValue = calculateAdxValue(candles);
        
        TradingStrategy newStrategy = currentStrategy;
        
        // Logic:
        // ADX > 25: Trend Following (MACD)
        // ADX < 20: Mean Reversion (RSI)
        // 20 <= ADX <= 25: Hold/Risk Management (Default)
        
        if (adxValue > 25) {
            newStrategy = trendStrategy;
        } else if (adxValue < 20) {
            newStrategy = meanReversionStrategy;
        } else {
            newStrategy = riskManagementStrategy;
        }
        
        // Hysteresis / Cooldown
        if (newStrategy != currentStrategy) {
            System.out.println("SWITCHING STRATEGY: " + currentStrategy.getName() + " -> " + newStrategy.getName() + " (ADX: " + String.format("%.2f", adxValue) + ")");
            currentStrategy = newStrategy;
            cooldown = COOLDOWN_PERIOD;
        }
        
        return currentStrategy;
    }
    
    // Copying simplified ADX Logic for Selector decision
    private double calculateAdxValue(List<Candle> data) {
        int period = 14;
        if(data.size() < period * 2) return 0;
        return calculateADX(data, data.size()-1, period);
    }
    
    // Stateless ADX Calculation duplicate (Helper)
    private double calculateADX(List<Candle> data, int endIndex, int period) {

        double sumTR = 0, sumPlusDM = 0, sumMinusDM = 0;

        for (int i = 0; i < period; i++) {
            int idx = endIndex - i;
            if (idx <= 0) break;
            Candle curr = data.get(idx);
            Candle prev = data.get(idx - 1);
            
            double tr1 = curr.high - curr.low;
            double tr2 = Math.abs(curr.high - prev.close);
            double tr3 = Math.abs(curr.low - prev.close);
            double trueRange = Math.max(tr1, Math.max(tr2, tr3));
            
            double upMove = curr.high - prev.high;
            double downMove = prev.low - curr.low;
            
            double pdm = 0, mdm = 0;
            if (upMove > downMove && upMove > 0) pdm = upMove;
            if (downMove > upMove && downMove > 0) mdm = downMove;
            
            sumTR += trueRange;
            sumPlusDM += pdm;
            sumMinusDM += mdm;
        }
        if (sumTR == 0) sumTR = 1;
        double smoothedPlusDI = 100 * (sumPlusDM/sumTR);
        double smoothedMinusDI = 100 * (sumMinusDM/sumTR);
        double dx = 100 * Math.abs(smoothedPlusDI - smoothedMinusDI) / (smoothedPlusDI + smoothedMinusDI + 0.0001);
        
        return dx; 
    }
}
