package interfaces;

import java.util.*;
import models.*;

public abstract class TradingTemplate {

    protected TradingStrategy strategy;
    protected ArrayList<Candle> data = new ArrayList<>();

    // Template Method (sabit akış)
    public final void trade(Candle candle) {
        List<Candle> candles = fetchData(candle);
        Signal signal = evaluateData(candles);
        Order order = createOrder(signal);
        executeOrder(order);
        logResult(order);
    }

    // Abstract method → gövde YOK
    protected abstract List<Candle> fetchData(Candle candle);

    protected abstract Signal evaluateData(List<Candle> candles);

    // Abstract method → gövde YOK
    protected abstract Order createOrder(Signal signal);

    protected abstract void executeOrder(Order order);

    // Abstract method → gövde YOK, return type EKLENDİ
    protected abstract void logResult(Order order);
}
