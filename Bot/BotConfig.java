package Bot;

import interfaces.TradingStrategy;

public class BotConfig {
    // Lazy loading
    private static BotConfig uniqueInstance;
    private BotConfig(){}
    public TradingStrategy strategy;

    public static BotConfig getInstance(){
        if(uniqueInstance == null){
            uniqueInstance = new BotConfig();
        }
        return uniqueInstance;
    }
}
