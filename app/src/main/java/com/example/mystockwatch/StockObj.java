package com.example.mystockwatch;

public class StockObj {
    private String name;
    private String symbol;

    private long x;
    private float high;
    private float low;
    private float open;
    private float close;
    private String label;

    public StockObj(String symbol, String name){
        this.symbol = symbol;
        this.name = name;
    }


    public StockObj(long x, float high, float low, float open,float close, String label){
        this.x = x;
        this.high = high;
        this.low  = low;
        this.open = open;
        this.close = close;
        this.label = label;
    }
    public String getName(){
        return this.name;
    }
    public String getSymbol(){
        return this.symbol;
    }
    public long getX(){
        return this.x;
    }
    public float getHigh(){
        return this.high;
    }
    public float getLow(){
        return this.low;
    }
    public float getOpen(){
        return this.open;
    }
    public float getClose(){
        return this.close;
    }
    public String getLabel(){
        return this.label;
    }

}
