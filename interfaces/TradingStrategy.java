package interfaces;

import java.util.List;
import models.*;

public interface TradingStrategy{

    Signal generateSignal(List<Price> prices);

}