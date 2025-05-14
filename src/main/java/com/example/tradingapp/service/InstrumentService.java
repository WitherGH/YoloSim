package com.example.tradingapp.service;

import com.example.tradingapp.model.Instrument;
import com.example.tradingapp.util.UIThreadUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class InstrumentService {

    private final ObservableList<Instrument> stocks = FXCollections.observableArrayList();
    private final StockDataService stockDataService = StockDataService.getInstance();

    private InstrumentService() {
        stocks.addAll(List.of(
                new Instrument("AAPL",  "Apple Inc.",      0, "Stock"),
                new Instrument("MSFT",  "Microsoft Corp.", 0, "Stock"),
                new Instrument("GOOGL", "Alphabet Inc.",   0, "Stock"),
                new Instrument("AMZN",  "Amazon.com Inc.", 0, "Stock"),
                new Instrument("NVDA",  "NVIDIA Corp.",    0, "Stock")
        ));

        refreshPrices();
    }

    public void refreshPrices() {
        for (Instrument instrument : stocks) {
            double price = stockDataService.getLastPrice(instrument.getSymbol());
            if (!Double.isNaN(price)) {
                final double finalPrice = price; 
                UIThreadUtil.runOnUIThread(() -> instrument.setPrice(finalPrice));
            }
        }
    }

    public ObservableList<Instrument> getStocks() { return stocks; }

    public void addInstrument(Instrument i) {
        stocks.add(i);
    }
    
    public void removeInstrument(Instrument i) {
        stocks.remove(i);
    }

    public List<Instrument> getAllInstruments() {
        return new ArrayList<>(stocks);
    }

    private static InstrumentService INSTANCE;
    public static synchronized InstrumentService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new InstrumentService();
        }
        return INSTANCE;
    }
}
