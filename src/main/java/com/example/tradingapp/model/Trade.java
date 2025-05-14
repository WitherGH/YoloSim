package com.example.tradingapp.model;

import java.time.LocalDateTime;

public class Trade {

    private String        traderName;     
    private LocalDateTime timestamp;
    private Instrument    instrument;
    private TradeType     type;
    private double        quantity;
    private double        priceAtTrade;

    Trade() { }

    public Trade(String traderName,
                 Instrument instrument,
                 TradeType type,
                 double quantity,
                 double priceAtTrade) {
        this.traderName   = traderName;
        this.timestamp    = LocalDateTime.now();
        this.instrument   = instrument;
        this.type         = type;
        this.quantity     = quantity;
        this.priceAtTrade = priceAtTrade;
    }

    public String getTraderName()                 { return traderName; }
    public void   setTraderName(String name)      { this.traderName = name; }

    public LocalDateTime getTimestamp()           { return timestamp; }
    public void          setTimestamp(LocalDateTime ts){ this.timestamp = ts; }

    public Instrument getInstrument()             { return instrument; }
    public void       setInstrument(Instrument i) { this.instrument = i; }

    public TradeType getType()                    { return type; }
    public void      setType(TradeType t)         { this.type = t; }

    public double getQuantity()                   { return quantity; }
    public void   setQuantity(double q)           { this.quantity = q; }

    public double getPriceAtTrade()               { return priceAtTrade; }
    public void   setPriceAtTrade(double p)       { this.priceAtTrade = p; }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + traderName + " " + type + " " 
             + instrument.getSymbol() + " x" + quantity + " @ " + priceAtTrade;
    }
}
