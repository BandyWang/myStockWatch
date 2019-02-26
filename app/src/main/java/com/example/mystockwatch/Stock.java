package com.example.mystockwatch;

public class Stock {
    private String name;
    private String symbol;

    public Stock(String symbol, String name){
        this.symbol = symbol;
        this.name = name;
    }
    public String getName(Stock s){
        return s.name;
    }
    public String getSymbol(Stock s){
        return s.symbol;
    }
}
