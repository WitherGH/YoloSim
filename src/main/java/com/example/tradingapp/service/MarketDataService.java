package com.example.tradingapp.service;

import com.example.tradingapp.model.Instrument;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MarketDataService {

    public static class OHLC {
        private final LocalDateTime time;
        private final double open, close, high, low;

        public OHLC(LocalDateTime time, double open, double close, double high, double low) {
            this.time = time;
            this.open = open;
            this.close = close;
            this.high = high;
            this.low = low;
        }

        public LocalDateTime getTime() { return time; }
        public double getOpen() { return open; }
        public double getClose() { return close; }
        public double getHigh() { return high; }
        public double getLow() { return low; }
    }

    private final StockDataService stockDataService = StockDataService.getInstance();
    private final Random rnd = new Random();
    private final Map<String, Double> lastKnownPrices = new ConcurrentHashMap<>();

    private double randomWalk(double last) {
        return last * (1 + (rnd.nextDouble() - 0.5) * 0.01);
    }

    public double getPrice(Instrument instr) {
        String symbol = instr.getSymbol();
        double price = stockDataService.getLastPrice(symbol);
        
        if (!Double.isNaN(price)) {
            lastKnownPrices.put(symbol, price);
            instr.setPrice(price);
            return price;
        }
        
        // If not found in CSV, use random walk
        double lastKnown = lastKnownPrices.getOrDefault(symbol, 100.0);
        double simulatedPrice = randomWalk(lastKnown);
        lastKnownPrices.put(symbol, simulatedPrice);
        instr.setPrice(simulatedPrice);
        return simulatedPrice;
    }

    public List<OHLC> getOHLC(String symbol, String interval, int maxBars) {
        LocalDate currentDate = stockDataService.getCurrentDate();
        // Get OHLC data for the day
        List<OHLC> bars = stockDataService.getOHLCForDay(symbol, currentDate);
        
        if (!bars.isEmpty()) {
            if (bars.size() > maxBars) {
                bars = bars.subList(bars.size() - maxBars, bars.size());
            }
            return bars;
        }
        
        // If no data available - generate simulated data
        bars = new ArrayList<>();
        double p = lastKnownPrices.getOrDefault(symbol, 100.0);
        LocalDateTime t = LocalDateTime.now().minusMinutes(maxBars);
        
        for (int i = 0; i < maxBars; i++, t = t.plusMinutes(1)) {
            double o = p;
            double c = p = randomWalk(p);
            double h = Math.max(o, c) * 1.002;
            double l = Math.min(o, c) * 0.998;
            bars.add(new OHLC(t, o, c, h, l));
        }

        bars.sort(Comparator.comparing(OHLC::getTime));  
        if (bars.size() > maxBars) {
            bars = bars.subList(bars.size() - maxBars, bars.size());
        }

        return Collections.unmodifiableList(bars);
    }
}
