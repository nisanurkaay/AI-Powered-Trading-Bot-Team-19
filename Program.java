import Bot.BotConfig;
import PriceObservers.PriceListener;
import PriceObservers.PriceSubject;
import models.Price;
import TradingStrategies.TrendFollowing;

public class Program {
    public static void main(String[] args) {
        //initializing the tradebot
        BotConfig tradeBot = BotConfig.getInstance();

        //assign it SmaCrossover strategy (Short: 5, Long: 10) wrapped with Crash Protection (1% drop)
        tradeBot.strategy = new StrategyDecorator.CrashProtection(
            new TradingStrategies.SmaCrossover(5, 10),
            0.01 
        );

        //init the subject
        PriceSubject subject = new PriceSubject();

        //init the observer
        PriceListener observer = new PriceListener();

        //register observer to subject
        subject.register(observer);

        // Initialize API Service
        services.ApiService apiService = new services.ApiService();
        apiService.start(8081);

        // Initialize Binance Service
        services.BinanceService binanceService = new services.BinanceService();
        String symbol = "BTCUSDT";

        System.out.println("Starting Trading Bot with Binance Data (" + symbol + ")...");

        // Real-time data loop
        while(true){
            // Fetch new price from Binance
            double currentPrice = binanceService.getPrice(symbol);

            if (currentPrice != -1) {
                System.out.println("Updated price (" + symbol + "): " + currentPrice);
                subject.setPrice(new Price(currentPrice));

                // subject notify the observers 
                subject.notifyObservers();
            } else {
                System.out.println("Failed to fetch price.");
            }

            // one second wait API limit safe
            waitOneSecond();
        }
    }    
    public static void waitOneSecond(){
        try {
            Thread.sleep(1000); // 1000 ms = 1 second
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
