package PriceObservers;

import interfaces.*;
import models.Price;
import Bot.Bot;

public class PriceListener implements Observer{
    Bot bot = new Bot();
    @Override
    public void priceUpdated(Price price) {
        bot.trade(price);
    }
    
}
