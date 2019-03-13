package com.example.mystockwatch;

public class StockObj implements Comparable {
    private String name;
    private String symbol;

    private long x;
    private float high;
    private float low;
    private float open;
    private float close;
    private String label;
    private double percentChange;

    public StockObj(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    public StockObj(String symbol, String name, double percentChange) {
        this.symbol = symbol;
        this.name = name;
        this.percentChange = percentChange;
    }

    public StockObj(long x, float high, float low, float open, float close, String label) {
        this.x = x;
        this.high = high;
        this.low = low;
        this.open = open;
        this.close = close;
        this.label = label;
    }

    public String getName() {
        return this.name;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public long getX() {
        return this.x;
    }

    public float getHigh() {
        return this.high;
    }

    public float getLow() {
        return this.low;
    }

    public float getOpen() {
        return this.open;
    }

    public float getClose() {
        return this.close;
    }

    public String getLabel() {
        return this.label;
    }

    public double getPercentChange() {
        return this.percentChange;
    }

    @Override
    public int compareTo(Object stock) {
        StockObj temp = (StockObj) stock;
        double pc = temp.getPercentChange();
        if(this.percentChange > pc){
            return -1;
        }else if(this.percentChange < pc){
            return 1;
        }else{
            return 0;
        }
    }


}
