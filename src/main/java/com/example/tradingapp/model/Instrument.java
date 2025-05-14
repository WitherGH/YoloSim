package com.example.tradingapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.beans.property.*;

public class Instrument {
    private final StringProperty symbol = new SimpleStringProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final DoubleProperty price = new SimpleDoubleProperty();
    private final StringProperty type = new SimpleStringProperty();   
    private final DoubleProperty priceChange = new SimpleDoubleProperty();
    private final DoubleProperty priceChangePercent = new SimpleDoubleProperty();
    private final DoubleProperty marketCap = new SimpleDoubleProperty();
    private final StringProperty marketCapFormatted = new SimpleStringProperty("N/A");

    Instrument() {}

    public Instrument(String symbol, String name, double price, String type) {
        this.symbol.set(symbol);
        this.name.set(name);
        this.price.set(price);
        this.type.set(type);
        this.priceChange.set(0.0);
        this.priceChangePercent.set(0.0);
        this.marketCap.set(0.0);
    }

    public String getSymbol() { return symbol.get(); }
    public void setSymbol(String s) { symbol.set(s); }
    public StringProperty symbolProperty() { return symbol; }

    public String getName() { return name.get(); }
    public void setName(String n) { name.set(n); }
    public StringProperty nameProperty() { return name; }

    public double getPrice() { return price.get(); }
    public void setPrice(double p) { price.set(p); }
    public DoubleProperty priceProperty() { return price; }

    public String getType() { return type.get(); }
    public void setType(String t) { type.set(t); }
    public StringProperty typeProperty() { return type; }
    
    public double getPriceChange() { return priceChange.get(); }
    public void setPriceChange(double change) { priceChange.set(change); }
    public DoubleProperty priceChangeProperty() { return priceChange; }
    
    public double getPriceChangePercent() { return priceChangePercent.get(); }
    public void setPriceChangePercent(double percentChange) { priceChangePercent.set(percentChange); }
    public DoubleProperty priceChangePercentProperty() { return priceChangePercent; }
    
    public double getMarketCap() { return marketCap.get(); }
    public void setMarketCap(double cap) { marketCap.set(cap); }
    public DoubleProperty marketCapProperty() { return marketCap; }
    
    public String getMarketCapFormatted() { return marketCapFormatted.get(); }
    public void setMarketCapFormatted(String formattedCap) { marketCapFormatted.set(formattedCap); }
    public StringProperty marketCapFormattedProperty() { return marketCapFormatted; }

    @Override 
    public String toString() { return getSymbol(); }

    @JsonIgnore
    public boolean isStock() { return "Stock".equalsIgnoreCase(getType()); }
    
    @JsonIgnore
    public boolean isCrypto() { return "Crypto".equalsIgnoreCase(getType()); }
    
    @JsonIgnore
    public String getPriceChangeFormatted() {
        double change = getPriceChange();
        double percent = getPriceChangePercent();
        String sign = change >= 0 ? "+" : "";
        return String.format("%s%.2f (%.2f%%)", sign, change, percent);
    }
}
