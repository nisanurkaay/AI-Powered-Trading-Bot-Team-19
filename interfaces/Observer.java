package interfaces;

import models.*;

public interface Observer {
    void priceUpdated(Candle candle);
}
