package com.example.tradingapp.service;

import com.example.tradingapp.model.Instrument;
import com.example.tradingapp.service.MarketDataService.OHLC;
import com.example.tradingapp.util.UIThreadUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StockDataService {
    private static final Path CSV_PATH = Path.of("15 Years Stock Data of NVDA AAPL MSFT GOOGL and AMZN.csv");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private final Map<String, NavigableMap<LocalDate, StockData>> stockDataBySymbol = new HashMap<>();
    private final Map<String, Double> lastPriceCache = new ConcurrentHashMap<>();
    private final Map<String, Double> marketCapCache = new ConcurrentHashMap<>();
    private final Map<String, Double> priceChangeCache = new ConcurrentHashMap<>();
    
    private LocalDate currentDate = LocalDate.now();
    
    private static final String[] SYMBOLS = {"AAPL", "MSFT", "GOOGL", "AMZN", "NVDA"};
    
    private static StockDataService INSTANCE;
    
    public static class StockData {
        private final double open;
        private final double close;
        private final double high;
        private final double low;
        private final long volume;

        public StockData(double open, double close, double high, double low, long volume) {
            this.open = open;
            this.close = close;
            this.high = high;
            this.low = low;
            this.volume = volume;
        }

        public double getOpen() { return open; }
        public double getClose() { return close; }
        public double getHigh() { return high; }
        public double getLow() { return low; }
        public long getVolume() { return volume; }
    }
    
    private StockDataService() {
        loadData();
    }
    
    public static synchronized StockDataService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new StockDataService();
        }
        return INSTANCE;
    }
    
    public void loadData() {
        try {
            File csvFile = CSV_PATH.toFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
                String headerLine = reader.readLine();
                if (headerLine == null) {
                    System.err.println("Warning: CSV file is empty");
                    return;
                }
                String line;
                
                while ((line = reader.readLine()) != null) {
                    processLine(line);
                }
            }
            
            System.out.println("Loaded historical data for: " + stockDataBySymbol.keySet());
            
            if (!stockDataBySymbol.isEmpty()) {
                String firstSymbol = SYMBOLS[0];
                if (stockDataBySymbol.containsKey(firstSymbol)) {
                    NavigableMap<LocalDate, StockData> dateMap = stockDataBySymbol.get(firstSymbol);
                    if (!dateMap.isEmpty()) {
                        currentDate = dateMap.lastKey();
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error loading CSV data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void processLine(String line) {
        String[] values = line.split(",");
        if (values.length < 25) return; 
        
        try {
            LocalDate date = LocalDate.parse(values[0], DATE_FORMATTER);
            
            for (int i = 0; i < SYMBOLS.length; i++) {
                String symbol = SYMBOLS[i];
                double close = Double.parseDouble(values[i + 1]); // Close prices are at index 1-5
                double high = Double.parseDouble(values[i + 6]);  // High prices are at index 6-10
                double low = Double.parseDouble(values[i + 11]);  // Low prices are at index 11-15
                double open = Double.parseDouble(values[i + 16]); // Open prices are at index 16-20
                long volume = Long.parseLong(values[i + 21]);     // Volumes are at index 21-25
                
                StockData data = new StockData(open, close, high, low, volume);
                
                stockDataBySymbol
                    .computeIfAbsent(symbol, k -> new TreeMap<>())
                    .put(date, data);
            }
        } catch (Exception e) {
            System.err.println("Error processing line: " + line);
            e.printStackTrace();
        }
    }
    
    public LocalDate getCurrentDate() {
        return currentDate;
    }
    
    public void setCurrentDate(LocalDate date) {
        this.currentDate = date;
        //cache clear
        lastPriceCache.clear();
        marketCapCache.clear();
        priceChangeCache.clear();
        
        // Update prices for all instruments
        InstrumentService instrService = InstrumentService.getInstance();
        for (Instrument instrument : instrService.getStocks()) {
            double price = getLastPrice(instrument.getSymbol());
            if (!Double.isNaN(price)) {
                final double finalPrice = price; 
                UIThreadUtil.runOnUIThread(() -> instrument.setPrice(finalPrice));
            }
        }
    }
    
    public double getLastPrice(String symbol) {
        return lastPriceCache.computeIfAbsent(symbol, s -> {
            NavigableMap<LocalDate, StockData> dateMap = stockDataBySymbol.get(s);
            if (dateMap == null) return Double.NaN;
            
            Map.Entry<LocalDate, StockData> entry = dateMap.floorEntry(currentDate);
            if (entry == null) return Double.NaN;
            
            return entry.getValue().getClose();
        });
    }
    
    public double getPriceChange(String symbol) {
        return priceChangeCache.computeIfAbsent(symbol, s -> {
            NavigableMap<LocalDate, StockData> dateMap = stockDataBySymbol.get(s);
            if (dateMap == null) return Double.NaN;
            
            Map.Entry<LocalDate, StockData> entry = dateMap.floorEntry(currentDate);
            if (entry == null) return Double.NaN;
            
            Map.Entry<LocalDate, StockData> prevEntry = dateMap.lowerEntry(entry.getKey());
            if (prevEntry == null) return 0.0; 
            return entry.getValue().getClose() - prevEntry.getValue().getClose();
        });
    }
    
    public double getPriceChangePercent(String symbol) {
        NavigableMap<LocalDate, StockData> dateMap = stockDataBySymbol.get(symbol);
        if (dateMap == null) return Double.NaN;

        Map.Entry<LocalDate, StockData> entry = dateMap.floorEntry(currentDate);
        if (entry == null) return Double.NaN;
        

        Map.Entry<LocalDate, StockData> prevEntry = dateMap.lowerEntry(entry.getKey());
        if (prevEntry == null) return 0.0; 
        
        double prevClose = prevEntry.getValue().getClose();
        double currentClose = entry.getValue().getClose();
        
        return (currentClose - prevClose) / prevClose * 100.0;
    }
    
    public double getEstimatedMarketCap(String symbol) {
        return marketCapCache.computeIfAbsent(symbol, s -> {
            double price = getLastPrice(s);
            if (Double.isNaN(price)) return Double.NaN;
            
            // Get the number of shares outstanding for the stock
            long sharesOutstanding = switch(s) {
                case "AAPL" -> 15570; 
                case "MSFT" -> 7430;  
                case "GOOGL" -> 12800; 
                case "AMZN" -> 10350;  
                case "NVDA" -> 2470;   
                default -> 1000;     
            };
            
            // market cap calculation
            return price * sharesOutstanding;
        });
    }
    
    public String formatMarketCap(double marketCap) {
        if (Double.isNaN(marketCap)) return "N/A";
        
        if (marketCap >= 1_000_000) {
            return String.format("$%.2fT", marketCap / 1_000_000);  
        } else if (marketCap >= 1_000) {
            return String.format("$%.2fB", marketCap / 1_000);     
        } else {
            return String.format("$%.2fM", marketCap);           
        }
    }
    
    public List<OHLC> getOHLCForDay(String symbol, LocalDate date) {
        NavigableMap<LocalDate, StockData> dateMap = stockDataBySymbol.get(symbol);
        if (dateMap == null) return Collections.emptyList();
        
        Map.Entry<LocalDate, StockData> entry = dateMap.floorEntry(date);
        if (entry == null) return Collections.emptyList();
        
        StockData data = entry.getValue();
        LocalDate entryDate = entry.getKey();
        
        // Generate bars for the trading day 
        List<OHLC> bars = new ArrayList<>();
        
        LocalDateTime startTime = LocalDateTime.of(entryDate, LocalTime.of(9, 30));
        
        double openPrice = data.getOpen();
        double closePrice = data.getClose();
        double highPrice = data.getHigh();
        double lowPrice = data.getLow();
        
 
        bars.add(new OHLC(startTime, openPrice, openPrice, openPrice, openPrice));
        

        bars.add(new OHLC(startTime.plusMinutes(15), 
                  openPrice, 
                  openPrice * 1.0005, 
                  Math.min(openPrice * 1.002, highPrice), 
                  Math.max(openPrice * 0.999, lowPrice)));
        

        bars.add(new OHLC(startTime.plusMinutes(30), 
                  openPrice * 1.0005, 
                  openPrice * 1.001, 
                  Math.min(openPrice * 1.005, highPrice), 
                  Math.max(openPrice * 0.998, lowPrice)));
        
        // 10:15 AM
        bars.add(new OHLC(startTime.plusMinutes(45), 
                  openPrice * 1.001, 
                  openPrice * 1.0015, 
                  Math.min(openPrice * 1.006, highPrice), 
                  Math.max(openPrice * 0.9975, lowPrice)));
        
        // 10:30 AM
        bars.add(new OHLC(startTime.plusHours(1), 
                  openPrice * 1.0015, 
                  openPrice * 1.0018, 
                  Math.min(openPrice * 1.007, highPrice), 
                  Math.max(openPrice * 0.997, lowPrice)));
                 
        // 10:45 AM
        bars.add(new OHLC(startTime.plusHours(1).plusMinutes(15), 
                  openPrice * 1.0018, 
                  openPrice * 1.002, 
                  Math.min(openPrice * 1.0075, highPrice), 
                  Math.max(openPrice * 0.9965, lowPrice)));
                  
        // 11:00 AM
        bars.add(new OHLC(startTime.plusHours(1).plusMinutes(30), 
                  openPrice * 1.002, 
                  openPrice * 1.0022, 
                  Math.min(openPrice * 1.008, highPrice), 
                  Math.max(openPrice * 0.996, lowPrice)));
                  
        // 11:15 AM
        bars.add(new OHLC(startTime.plusHours(1).plusMinutes(45), 
                  openPrice * 1.0022, 
                  openPrice * 1.0025, 
                  Math.min(openPrice * 1.0085, highPrice), 
                  Math.max(openPrice * 0.9955, lowPrice)));
        
        // 11:30 AM
        bars.add(new OHLC(startTime.plusHours(2), 
                  openPrice * 1.0025, 
                  openPrice * 1.0028, 
                  Math.min(openPrice * 1.009, highPrice), 
                  Math.max(openPrice * 0.995, lowPrice)));
                  
        // 11:45 AM
        bars.add(new OHLC(startTime.plusHours(2).plusMinutes(15), 
                  openPrice * 1.0028, 
                  openPrice * 1.003, 
                  Math.min(openPrice * 1.0095, highPrice), 
                  Math.max(openPrice * 0.9945, lowPrice)));
        
        // 12:00 PM
        bars.add(new OHLC(startTime.plusHours(2).plusMinutes(30), 
                  openPrice * 1.003, 
                  openPrice * 1.0032, 
                  Math.min(openPrice * 1.01, highPrice), 
                  Math.max(openPrice * 0.994, lowPrice)));
                 
        // 12:15 PM
        bars.add(new OHLC(startTime.plusHours(2).plusMinutes(45), 
                  openPrice * 1.0032, 
                  openPrice * 1.0033, 
                  highPrice, 
                  lowPrice));
                          
        // 12:30 PM
        bars.add(new OHLC(startTime.plusHours(3), 
                  openPrice * 1.0033, 
                  openPrice * 1.0034, 
                  highPrice, 
                  lowPrice));
                  
        // 12:45 PM
        bars.add(new OHLC(startTime.plusHours(3).plusMinutes(15), 
                  openPrice * 1.0034, 
                  openPrice * 1.0035, 
                  highPrice, 
                  lowPrice));
        
        // 1:00 PM
        bars.add(new OHLC(startTime.plusHours(3).plusMinutes(30), 
                  openPrice * 1.0035, 
                  openPrice * 1.0036, 
                  highPrice, 
                  lowPrice));
                  
        // 1:15 PM
        bars.add(new OHLC(startTime.plusHours(3).plusMinutes(45), 
                  openPrice * 1.0036, 
                  openPrice * 1.0037, 
                  highPrice, 
                  lowPrice));
        
        // 1:30 PM
        bars.add(new OHLC(startTime.plusHours(4), 
                  openPrice * 1.0037, 
                  openPrice * 1.0038, 
                  highPrice, 
                  lowPrice));
                  
        // 1:45 PM
        bars.add(new OHLC(startTime.plusHours(4).plusMinutes(15), 
                  openPrice * 1.0038, 
                  openPrice * 1.0039, 
                  highPrice, 
                  lowPrice));
        
        // 2:00 PM
        bars.add(new OHLC(startTime.plusHours(4).plusMinutes(30), 
                  openPrice * 1.0039, 
                  openPrice * 1.004, 
                  highPrice, 
                  lowPrice));
                  
        // 2:15 PM
        bars.add(new OHLC(startTime.plusHours(4).plusMinutes(45), 
                  openPrice * 1.004, 
                  openPrice * 1.0038, 
                  highPrice, 
                  lowPrice));
        
        // 2:30 PM
        bars.add(new OHLC(startTime.plusHours(5), 
                  openPrice * 1.0038, 
                  openPrice * 1.0036, 
                  highPrice, 
                  lowPrice));
                  
        // 2:45 PM
        bars.add(new OHLC(startTime.plusHours(5).plusMinutes(15), 
                  openPrice * 1.0036, 
                  openPrice * 1.002, 
                  highPrice, 
                  lowPrice));
        
        // 3:00 PM
        bars.add(new OHLC(startTime.plusHours(5).plusMinutes(30), 
                  openPrice * 1.002, 
                  closePrice * 0.999, 
                  highPrice, 
                  lowPrice));
                  
        // 3:15 PM
        bars.add(new OHLC(startTime.plusHours(5).plusMinutes(45), 
                  closePrice * 0.999, 
                  closePrice * 0.9995, 
                  highPrice, 
                  lowPrice));
        
        // 3:30 PM
        bars.add(new OHLC(startTime.plusHours(6), 
                  closePrice * 0.9995, 
                  closePrice * 0.9998, 
                  highPrice, 
                  lowPrice));
                  
        // 3:45 PM
        bars.add(new OHLC(startTime.plusHours(6).plusMinutes(15), 
                  closePrice * 0.9998, 
                  closePrice * 0.9999, 
                  highPrice, 
                  lowPrice));
        
        // 4:00 PM Closing
        bars.add(new OHLC(startTime.plusHours(6).plusMinutes(30), 
                  closePrice * 0.9999, 
                  closePrice, 
                  highPrice, 
                  lowPrice));
                          
        return bars;
    }
    
    public List<LocalDate> getAvailableDates() {
        // return all available dates from any stock
        Set<LocalDate> allDates = new TreeSet<>();
        
        for (NavigableMap<LocalDate, StockData> dateMap : stockDataBySymbol.values()) {
            allDates.addAll(dateMap.keySet());
        }
        
        return new ArrayList<>(allDates);
    }
    
    public LocalDate getFirstAvailableDate() {
        List<LocalDate> dates = getAvailableDates();
        return dates.isEmpty() ? LocalDate.now() : dates.get(0);
    }
    
    public LocalDate getLastAvailableDate() {
        List<LocalDate> dates = getAvailableDates();
        return dates.isEmpty() ? LocalDate.now() : dates.get(dates.size() - 1);
    }
} 