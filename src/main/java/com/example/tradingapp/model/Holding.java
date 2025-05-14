package com.example.tradingapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.beans.property.*;

public class Holding {
    private final ObjectProperty<Instrument> instrument = new SimpleObjectProperty<>();
    private final DoubleProperty quantity = new SimpleDoubleProperty();

    Holding() { }

    public Holding(Instrument instrument, double quantity) {
        this.instrument.set(instrument);
        this.quantity.set(quantity);
    }

    public Instrument getInstrument() { return instrument.get(); }
    public void setInstrument(Instrument i) { instrument.set(i); }
    public double getQuantity() { return quantity.get(); }
    public void setQuantity(double q) { quantity.set(q); }

    @JsonIgnore public ObjectProperty<Instrument> instrumentProperty() { return instrument; }
    @JsonIgnore public DoubleProperty quantityProperty() { return quantity; }
}
