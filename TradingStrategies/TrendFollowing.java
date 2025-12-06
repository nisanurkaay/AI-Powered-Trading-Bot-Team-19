package TradingStrategies;

import java.util.*;

import interfaces.TradingStrategy;
import models.*;

public class TrendFollowing implements TradingStrategy{
    int lastXprice = 5;
    @Override
    public Signal generateSignal(List<Price> prices) {
        if(prices.size() < lastXprice){
            return Signal.HOLD;
        }else{
            if(isTrendFollowing(prices)){
                return Signal.BUY;
            }else{
                return Signal.HOLD;
            }
        }
    }

    public boolean isTrendFollowing(List<Price> prices){
        int last = prices.size() - lastXprice;
        for(int i = 0; i < lastXprice -1; i++){
            if(prices.get(last).value > prices.get(last +1).value){
                return false;
            }

        }
        return true;
    }

 
    
    @Override
    public String getName() {
        return "TrendFollowing (Last: " + lastXprice + ")";
    }
}