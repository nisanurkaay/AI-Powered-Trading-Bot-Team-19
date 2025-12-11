package PriceObservers;

import interfaces.*;
import models.Candle;
import Bot.Bot;

public class PriceListener implements Observer{
    Bot bot = new Bot();
    @Override
    public void priceUpdated(Candle candle) {
        bot.trade(candle);
    }
    
}
