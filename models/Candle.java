package models;

public class Candle {
    public long openTime;
    public double open;
    public double high;
    public double low;
    public double close;
    public double volume;
    public long closeTime;

    public Candle(long openTime, double open, double high, double low, double close, double volume, long closeTime) {
        this.openTime = openTime;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.closeTime = closeTime;
    }

    @Override
    public String toString() {
        return "Candle{" +
                "time=" + openTime +
                ", close=" + close +
                '}';
    }
}
