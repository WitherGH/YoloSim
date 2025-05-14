package com.example.tradingapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class Portfolio {

    @JsonIgnore
    private final ObservableList<Holding> holdings =
            FXCollections.observableArrayList();

    @JsonProperty("holdings")
    public List<Holding> getHoldingsForJson() {
        return new ArrayList<>(holdings);
    }

    @JsonProperty("holdings")
    public void setHoldingsForJson(List<Holding> list) {
        holdings.setAll(list);
    }


    public ObservableList<Holding> getHoldings() {
        return holdings;
    }

    public double calculateTotalBalance() {
        return holdings.stream()
                .mapToDouble(h -> h.getInstrument().getPrice()
                        * h.getQuantity())
                .sum();
    }
}
