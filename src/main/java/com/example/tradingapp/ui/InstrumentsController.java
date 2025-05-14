package com.example.tradingapp.ui;

import com.example.tradingapp.model.Instrument;
import com.example.tradingapp.model.Trade;
import com.example.tradingapp.model.TradeType;
import com.example.tradingapp.model.Trader;
import com.example.tradingapp.service.InstrumentService;
import com.example.tradingapp.service.StockDataService;
import com.example.tradingapp.service.TraderService;
import com.example.tradingapp.util.SortUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

public class InstrumentsController {
    @FXML private TableView<Instrument> stockTable;
    @FXML private TableColumn<Instrument, String> colStockSymbol;
    @FXML private TableColumn<Instrument, String> colStockName;
    @FXML private TableColumn<Instrument, Number> colStockPrice;
    @FXML private TableColumn<Instrument, String> colStockChange;
    @FXML private TableColumn<Instrument, String> colStockMarketCap;
    @FXML private Label lblSelected;
    @FXML private TextField txtQuantity;
    @FXML private Button btnBuy, btnSell;
    @FXML private DatePicker datePicker;
    @FXML private VBox stockSection;
    @FXML private ComboBox<Trader> cmbTraders;
    
    // Sort buttons
    @FXML private Button btnSortBySymbol;
    @FXML private Button btnSortByName;
    @FXML private Button btnSortByPrice;

    private final InstrumentService instrumentService = InstrumentService.getInstance();
    private final TraderService traderService = TraderService.getInstance();
    private final StockDataService stockDataService = StockDataService.getInstance();

    @FXML
    public void initialize() {
        System.out.println("Available stocks:");
        instrumentService.getStocks().forEach(instr -> 
            System.out.println(" - " + instr.getSymbol() + ": " + instr.getName()));
        
        setupTraderComboBox();
        
        datePicker.setValue(stockDataService.getCurrentDate());
        
        datePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) {
                updateStocksForDate(newDate);
            }
        });
        
        setupTableColumns();
        
        stockTable.setItems(instrumentService.getStocks());
        updateStocksForDate(datePicker.getValue());

        stockTable.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n == null) return;
            lblSelected.setText("Selected: " + n.getSymbol());
            updateTradeButtonsState();
        });

        btnBuy.setOnAction(e -> executeTrade(TradeType.BUY, stockTable));
        btnSell.setOnAction(e -> executeTrade(TradeType.SELL, stockTable));
        
        btnSortBySymbol.setOnAction(e -> sortStocks(Comparator.comparing(Instrument::getSymbol), "symbol"));
        btnSortByName.setOnAction(e -> sortStocks(Comparator.comparing(Instrument::getName), "name"));
        btnSortByPrice.setOnAction(e -> sortStocks(Comparator.comparingDouble(Instrument::getPrice), "price"));
    }
    
    private void setupTraderComboBox() {
        System.out.println("Setting up trader combobox");
        if (cmbTraders == null) {
            System.out.println("Warning: cmbTraders is null");
            return;
        }
        
        // Populate ComboBox with traders
        cmbTraders.setItems(FXCollections.observableArrayList(traderService.getTraders()));
        
        // Setup display of trader names
        cmbTraders.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Trader item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
        
        cmbTraders.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Trader item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
        
        // Select current trader in ComboBox
        Trader selectedTrader = traderService.getSelectedTrader();
        if (selectedTrader != null) {
            cmbTraders.setValue(selectedTrader);
        } else if (!traderService.getTraders().isEmpty()) {
            cmbTraders.setValue(traderService.getTraders().get(0));
            traderService.setSelectedTrader(traderService.getTraders().get(0));
        }
        
        // Update selected trader when ComboBox selection changes
        cmbTraders.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                traderService.setSelectedTrader(newVal);
                updateTradeButtonsState();
            }
        });
    }
    
    private void updateTradeButtonsState() {
        boolean hasTrader = traderService.getSelectedTrader() != null;
        boolean hasSelectedStock = stockTable.getSelectionModel().getSelectedItem() != null;
        
        btnBuy.setDisable(!hasTrader || !hasSelectedStock);
        btnSell.setDisable(!hasTrader || !hasSelectedStock);
    }
    
    private void setupTableColumns() {
        colStockSymbol.setCellValueFactory(c -> c.getValue().symbolProperty());
        colStockName.setCellValueFactory(c -> c.getValue().nameProperty());
        colStockPrice.setCellValueFactory(c -> c.getValue().priceProperty());
        
        colStockChange.setCellValueFactory(data -> {
            double change = data.getValue().getPriceChange();
            double percent = data.getValue().getPriceChangePercent();
            String sign = change >= 0 ? "+" : "";
            return new javafx.beans.property.SimpleStringProperty(
                String.format("%s%.2f (%.2f%%)", sign, change, percent));
        });
        
        colStockChange.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    
                    if (item.contains("+")) {
                        setStyle("-fx-text-fill: green;");
                    } else if (item.contains("-")) {
                        setStyle("-fx-text-fill: red;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        
        colStockMarketCap.setCellValueFactory(data -> data.getValue().marketCapFormattedProperty());
    }
    
    private void sortStocks(Comparator<Instrument> comparator, String criteria) {
        List<Instrument> stocks = new ArrayList<>(instrumentService.getStocks());
        SortUtils.mergeSort(stocks, comparator);
        stockTable.setItems(FXCollections.observableArrayList(stocks));
        System.out.println("Sorted stocks by " + criteria + " using custom merge sort");
    }
    
    private void updateStocksForDate(LocalDate date) {
        stockDataService.setCurrentDate(date);
        
        for (Instrument instrument : instrumentService.getStocks()) {
            String symbol = instrument.getSymbol();
            
            double price = stockDataService.getLastPrice(symbol);
            if (!Double.isNaN(price)) {
                instrument.setPrice(price);
            }
            
            double priceChange = stockDataService.getPriceChange(symbol);
            instrument.setPriceChange(priceChange);
            
            double priceChangePercent = stockDataService.getPriceChangePercent(symbol);
            instrument.setPriceChangePercent(priceChangePercent);
            
            double marketCap = stockDataService.getEstimatedMarketCap(symbol);
            instrument.setMarketCap(marketCap);
            
            String formattedMarketCap = stockDataService.formatMarketCap(marketCap);
            instrument.setMarketCapFormatted(formattedMarketCap);
        }
        
        stockTable.refresh();
    }

    public void setInstrumentType(String type) {
        if ("Stock".equalsIgnoreCase(type)) {
            stockSection.setVisible(true);
            stockSection.setManaged(true);
            stockTable.setItems(instrumentService.getStocks());
            System.out.println("Stock count in setInstrumentType: " + instrumentService.getStocks().size());
        }
    }

    private void executeTrade(TradeType type, TableView<Instrument> table) {
        Instrument instr = table.getSelectionModel().getSelectedItem();
        if (instr == null) return;

        String qtyStr = txtQuantity.getText();
        if (qtyStr == null || qtyStr.isBlank()) {
            new Alert(Alert.AlertType.ERROR, "Enter quantity").showAndWait();
            return;
        }

        double qty;
        try {
            qty = Double.parseDouble(qtyStr);
            if (qty <= 0) throw new NumberFormatException("Qty must be positive");
        } catch (NumberFormatException ex) {
            new Alert(Alert.AlertType.ERROR, "Invalid quantity: " + ex.getMessage()).showAndWait();
            return;
        }

        Trader trader = traderService.getSelectedTrader();
        if (trader == null) {
            new Alert(Alert.AlertType.ERROR, "No trader selected").showAndWait();
            return;
        }
        
        if (type == TradeType.SELL && !hasEnoughShares(trader, instr, qty)) {
            return;
        }
        
        double cost = qty * instr.getPrice();
        
        if (type == TradeType.BUY && !hasEnoughBudget(trader, cost)) {
            return;
        }

        Trade trade = new Trade(
                trader.getName(),
                instr,
                type,
                qty,
                instr.getPrice()
        );
        
        updateTraderBudget(trader, type, cost);
        traderService.executeTrade(trader, trade);
    }
    
    private boolean hasEnoughShares(Trader trader, Instrument instr, double qty) {
        double ownedQuantity = trader.getPortfolio().getHoldings().stream()
            .filter(holding -> holding.getInstrument().getSymbol().equals(instr.getSymbol()))
            .mapToDouble(holding -> holding.getQuantity())
            .sum();
            
        if (qty > ownedQuantity) {
            new Alert(Alert.AlertType.ERROR, 
                "Cannot sell more than owned. You have " + ownedQuantity + 
                " shares of " + instr.getSymbol()).showAndWait();
            return false;
        }
        return true;
    }
    
    private boolean hasEnoughBudget(Trader trader, double cost) {
        if (!trader.hasSufficientBudget(cost)) {
            new Alert(Alert.AlertType.ERROR, 
                "Insufficient budget. Required: " + String.format("%.2f", cost) + 
                ", Available: " + String.format("%.2f", trader.getBudget())).showAndWait();
            return false;
        }
        return true;
    }
    
    private void updateTraderBudget(Trader trader, TradeType type, double cost) {
        if (type == TradeType.BUY) {
            trader.deductFromBudget(cost);
        } else if (type == TradeType.SELL) {
            trader.addToBudget(cost);
        }
    }
}
