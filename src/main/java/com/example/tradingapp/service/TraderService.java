package com.example.tradingapp.service;

import com.example.tradingapp.model.Holding;
import com.example.tradingapp.model.Portfolio;
import com.example.tradingapp.model.Trade;
import com.example.tradingapp.model.TradeType;
import com.example.tradingapp.model.Trader;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TraderService {
    private static final TraderService instance = new TraderService();
    private final ObservableList<Trader> traders = FXCollections.observableArrayList();
    private final ObjectProperty<Trader> selectedTrader = new SimpleObjectProperty<>();

    private TraderService() {
    }

    public static TraderService getInstance() {
        return instance;
    }

    public ObservableList<Trader> getTraders() {
        return traders;
    }

    public Trader getSelectedTrader() {
        return selectedTrader.get();
    }

    public void setSelectedTrader(Trader trader) {
        selectedTrader.set(trader);
    }

    public ObjectProperty<Trader> selectedTraderProperty() {
        return selectedTrader;
    }

    public void createTrader(String name) {
        Trader t = new Trader(name);
        traders.add(t);
    }

    public void deleteTrader(Trader t) {
        traders.remove(t);
        if (getSelectedTrader() == t) {
            setSelectedTrader(null);
        }
    }

    public void renameTrader(Trader trader, String newName) {
        trader.setName(newName);
    }

    public void executeTrade(Trader trader, Trade trade) {
        Portfolio portfolio = trader.getPortfolio();
        Holding existing = portfolio.getHoldings().stream()
                .filter(h -> h.getInstrument().getSymbol()
                        .equals(trade.getInstrument().getSymbol()))
                .findFirst()
                .orElse(null);

        if (trade.getType() == TradeType.BUY) {
            if (existing != null) {
                existing.setQuantity(
                        existing.getQuantity() + trade.getQuantity()
                );
            } else {
                portfolio.getHoldings()
                        .add(new Holding(
                                trade.getInstrument(),
                                trade.getQuantity()
                        ));
            }
        } else {
            if (existing != null) {
                double newQty = existing.getQuantity() - trade.getQuantity();
                if (newQty <= 0.0) {
                    portfolio.getHoldings().remove(existing);
                } else {
                    existing.setQuantity(newQty);
                }
            }
        }

        trader.getTradesList().add(trade);
    }
    

    public boolean deleteTrade(Trader trader, Trade trade) {
        boolean removed = trader.getTradesList().remove(trade);
        
        if (!removed) {
            return false;
        }
        
        recalculatePortfolio(trader);
        
        return true;
    }
    

    private void recalculatePortfolio(Trader trader) {
        trader.getPortfolio().getHoldings().clear();
        
        for (Trade trade : trader.getTradesList()) {
            Portfolio portfolio = trader.getPortfolio();
            Holding existing = portfolio.getHoldings().stream()
                    .filter(h -> h.getInstrument().getSymbol()
                            .equals(trade.getInstrument().getSymbol()))
                    .findFirst()
                    .orElse(null);

            if (trade.getType() == TradeType.BUY) {
                if (existing != null) {
                    existing.setQuantity(
                            existing.getQuantity() + trade.getQuantity()
                    );
                } else {
                    portfolio.getHoldings()
                            .add(new Holding(
                                    trade.getInstrument(),
                                    trade.getQuantity()
                            ));
                }
            } else {
                if (existing != null) {
                    double newQty = existing.getQuantity() - trade.getQuantity();
                    if (newQty <= 0.0) {
                        portfolio.getHoldings().remove(existing);
                    } else {
                        existing.setQuantity(newQty);
                    }
                }
            }
        }
    }

    public java.util.List<Trader> getAllTraders() {
        return new java.util.ArrayList<>(traders);
    }

    public java.util.List<Trade> getAllTrades() {
        java.util.List<Trade> all = new java.util.ArrayList<>();
        for (Trader t : traders) {
            all.addAll(t.getTradesList().toList());
        }
        return all;
    }

    public void setAllTraders(java.util.List<Trader> list) {
        traders.setAll(list);
        selectedTrader.set(null);
    }

    public void setAllTrades(java.util.List<Trade> list) {
        for (Trader t : traders) {
            t.getPortfolio().getHoldings().clear();
            t.getTradesList().clear();
        }

        for (Trade tr : list) {
            String ownerName = tr.getTraderName();
            Trader owner = traders.stream()
                    .filter(t -> t.getName().equals(ownerName))
                    .findFirst()
                    .orElse(null);
            if (owner != null) {
                executeTrade(owner, tr);
            }
        }
    }
}

