package com.example.tradingapp.persistence;

import com.example.tradingapp.model.Trader;
import com.example.tradingapp.model.Instrument;
import com.example.tradingapp.model.Trade;
import java.util.ArrayList;
import java.util.List;

public class DataSnapshot {
    public List<Trader> traders;
    public List<Instrument> instruments;
    public List<Trade> trades;

    public DataSnapshot() {
        this.traders = new ArrayList<>();
        this.instruments = new ArrayList<>();
        this.trades = new ArrayList<>();
    }

    public DataSnapshot(List<Trader> traders, List<Instrument> instruments, List<Trade> trades) {
        this.traders = traders;
        this.instruments = instruments;
        this.trades = trades;
    }
}
