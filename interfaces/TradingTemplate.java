package interfaces;

import java.util.*;
import models.*;

public abstract class TradingTemplate {

    protected TradingStrategy strategy;
    protected ArrayList<Price> data = new ArrayList<>();

    // Template Method (sabit akış)
    public final void trade(Price price) {
        List<Price> prices = fetchData(price);
        Signal signal = evaluateData(prices);
        Order order = createOrder(signal);
        executeOrder(order);
        logResult(order);
    }

    // Abstract method → gövde YOK
    protected abstract List<Price> fetchData(Price price);

    protected abstract Signal evaluateData(List<Price> prices);

    // Abstract method → gövde YOK
    protected abstract Order createOrder(Signal signal);

    protected abstract void executeOrder(Order order);

    // Abstract method → gövde YOK, return type EKLENDİ
    protected abstract void logResult(Order order);
}
