package com.example.tradingapp.ui;

import com.example.tradingapp.model.Trade;
import com.example.tradingapp.model.TradeType;
import com.example.tradingapp.model.Trader;
import com.example.tradingapp.service.TraderService;
import com.example.tradingapp.util.SortUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TradesController {
    @FXML private TableView<Trade> tblTrades;
    @FXML private TableColumn<Trade, String> colDate;
    @FXML private TableColumn<Trade, String> colSymbol;
    @FXML private TableColumn<Trade, String> colType;
    @FXML private TableColumn<Trade, Number> colQty;
    @FXML private TableColumn<Trade, Number> colPrice;
    @FXML private TableColumn<Trade, Number> colTotal;
    @FXML private Button btnSortBySymbol;
    @FXML private Button btnSortByQuantity;
    @FXML private Button btnSortByPrice; 
    @FXML private Button btnSortByTotal;
    @FXML private Button btnFindSymbol;
    @FXML private Button btnDeleteTrade;
    @FXML private TextField txtSearchSymbol;
    @FXML private ComboBox<Trader> cmbTraders;
    @FXML private Button btnFilterAll;
    @FXML private Button btnFilterBuy;
    @FXML private Button btnFilterSell;

    private final TraderService traderService = TraderService.getInstance();
    private List<Trade> allTrades = new ArrayList<>();

    @FXML
    public void initialize() {
        setupTableColumns();
        setupButtonHandlers();
        setupTraderComboBox();
        
        Trader selectedTrader = traderService.getSelectedTrader();
        if (selectedTrader != null) {
            System.out.println("Using already selected trader: " + selectedTrader.getName());
            refreshTrades();
        } else if (!traderService.getAllTraders().isEmpty()) {
            System.out.println("Auto-selecting first trader");
            traderService.setSelectedTrader(traderService.getAllTraders().get(0));
            refreshTrades();
        }
    }
    
    private void setupTableColumns() {
        colDate.setCellValueFactory(c ->
                new SimpleObjectProperty<>(c.getValue().getTimestamp().toString())
        );
        colSymbol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getInstrument().getSymbol())
        );
        colType.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getType().name())
        );
        colQty.setCellValueFactory(c ->
                new SimpleDoubleProperty(c.getValue().getQuantity())
        );
        colPrice.setCellValueFactory(c ->
                new SimpleDoubleProperty(c.getValue().getPriceAtTrade())
        );
        if (colTotal != null) {
            colTotal.setCellValueFactory(c -> {
                double price = c.getValue().getPriceAtTrade();
                double qty = c.getValue().getQuantity();
                return new SimpleDoubleProperty(price * qty);
            });
        }
    }
    
    private void setupTraderComboBox() {
        System.out.println("Setting up trader combobox");
        if (cmbTraders == null) {
            System.out.println("Warning: cmbTraders is null");
            return;
        }
        
        cmbTraders.setItems(FXCollections.observableArrayList(traderService.getAllTraders()));
        
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
        
        // Select current trader in the combobox
        Trader selectedTrader = traderService.getSelectedTrader();
        if (selectedTrader != null) {
            cmbTraders.setValue(selectedTrader);
        } else if (!traderService.getAllTraders().isEmpty()) {
            cmbTraders.setValue(traderService.getAllTraders().get(0));
        }
        
        // Update data when trader selection changes
        cmbTraders.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                traderService.setSelectedTrader(newVal);
                refreshTrades();
            }
        });
    }
    
    private void setupButtonHandlers() {
        System.out.println("Setting up button handlers");
        
        // Configure sort buttons
        if (btnSortBySymbol != null) {
            btnSortBySymbol.setOnAction(e -> sortTradesBySymbol());
        } else {
            System.out.println("Warning: btnSortBySymbol is null");
        }
        
        if (btnSortByQuantity != null) {
            btnSortByQuantity.setOnAction(e -> sortTradesByQuantity());
        } else {
            System.out.println("Warning: btnSortByQuantity is null");
        }
        
        if (btnSortByPrice != null) {
            btnSortByPrice.setOnAction(e -> sortTradesByPrice());
        } else {
            System.out.println("Warning: btnSortByPrice is null");
        }
        
        if (btnSortByTotal != null) {
            btnSortByTotal.setOnAction(e -> sortTradesByTotal());
        } else {
            System.out.println("Warning: btnSortByTotal is null");
        }
        
        // Configure other buttons
        if (btnFindSymbol != null) {
            btnFindSymbol.setOnAction(e -> findBySymbol());
        } else {
            System.out.println("Warning: btnFindSymbol is null");
        }
        
        if (btnDeleteTrade != null) {
            btnDeleteTrade.setOnAction(e -> deleteTrade());
            // Initially disable the delete button until user selects a trade
            btnDeleteTrade.setDisable(true);
        } else {
            System.out.println("Warning: btnDeleteTrade is null");
        }
        
        // Handlers for filter buttons
        if (btnFilterAll != null) {
            btnFilterAll.setOnAction(e -> showAllTrades());
        } else {
            System.out.println("Warning: btnFilterAll is null");
        }
        
        if (btnFilterBuy != null) {
            btnFilterBuy.setOnAction(e -> filterTradesByType(TradeType.BUY));
        } else {
            System.out.println("Warning: btnFilterBuy is null");
        }
        
        if (btnFilterSell != null) {
            btnFilterSell.setOnAction(e -> filterTradesByType(TradeType.SELL));
        } else {
            System.out.println("Warning: btnFilterSell is null");
        }
        
        // Add listener for trade selection in the table
        tblTrades.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (btnDeleteTrade != null) {
                btnDeleteTrade.setDisable(newVal == null);
            }
        });
    }

    private void refreshTrades() {
        System.out.println("Refreshing trades");
        Trader t = traderService.getSelectedTrader();
        if (t == null) {
            System.out.println("No trader selected");
            tblTrades.setItems(FXCollections.emptyObservableList());
            allTrades.clear();
            return;
        }
        
        System.out.println("Loading trades for trader: " + t.getName());
        // Store all operations for filtering
        allTrades = new ArrayList<>(t.getTradesList().toList());
        tblTrades.setItems(FXCollections.observableArrayList(allTrades));
        System.out.println("Loaded " + allTrades.size() + " trades");
    }
    
    private void showAllTrades() {
        tblTrades.setItems(FXCollections.observableArrayList(allTrades));
    }
    
    private void filterTradesByType(TradeType type) {
        List<Trade> filteredTrades = allTrades.stream()
                .filter(trade -> trade.getType() == type)
                .collect(Collectors.toList());
        
        tblTrades.setItems(FXCollections.observableArrayList(filteredTrades));
    }
    
    private void sortTradesBySymbol() {
        if (allTrades.isEmpty()) return;
        
        List<Trade> list = new ArrayList<>(allTrades);
        SortUtils.mergeSort(list, Comparator.comparing(tr -> tr.getInstrument().getSymbol()));
        tblTrades.setItems(FXCollections.observableArrayList(list));
        System.out.println("Sorted trades by symbol");
    }
    
    private void sortTradesByQuantity() {
        if (allTrades.isEmpty()) return;
        
        List<Trade> list = new ArrayList<>(allTrades);
        SortUtils.mergeSort(list, Comparator.comparingDouble(Trade::getQuantity));
        tblTrades.setItems(FXCollections.observableArrayList(list));
        System.out.println("Sorted trades by quantity");
    }

    private void sortTradesByPrice() {
        if (allTrades.isEmpty()) return;
        
        List<Trade> list = new ArrayList<>(allTrades);
        SortUtils.mergeSort(list, Comparator.comparingDouble(Trade::getPriceAtTrade));
        tblTrades.setItems(FXCollections.observableArrayList(list));
        System.out.println("Sorted trades by price");
    }
    
    private void sortTradesByTotal() {
        if (allTrades.isEmpty()) return;
        
        List<Trade> list = new ArrayList<>(allTrades);
        SortUtils.mergeSort(list, Comparator.comparingDouble(
            trade -> trade.getPriceAtTrade() * trade.getQuantity()
        ));
        tblTrades.setItems(FXCollections.observableArrayList(list));
        System.out.println("Sorted trades by total");
    }

    private void findBySymbol() {
        String symbol = txtSearchSymbol.getText().trim().toUpperCase();
        if (symbol.isEmpty()) return;
        if (allTrades.isEmpty()) return;

        // Filter trades by symbol
        List<Trade> filteredTrades = allTrades.stream()
                .filter(trade -> trade.getInstrument().getSymbol().equals(symbol))
                .collect(Collectors.toList());
        
        if (filteredTrades.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Symbol not found").showAndWait();
            return;
        }
        
        tblTrades.setItems(FXCollections.observableArrayList(filteredTrades));
    }
    
    private void deleteTrade() {
        Trader trader = traderService.getSelectedTrader();
        if (trader == null) return;
        
        Trade selectedTrade = tblTrades.getSelectionModel().getSelectedItem();
        if (selectedTrade == null) return;
        
        // Ask for user confirmation
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Trade");
        confirmDialog.setHeaderText("Delete Selected Trade");
        confirmDialog.setContentText(
                "Are you sure you want to delete this trade?\n" +
                "Symbol: " + selectedTrade.getInstrument().getSymbol() + "\n" +
                "Type: " + selectedTrade.getType() + "\n" +
                "Quantity: " + selectedTrade.getQuantity() + "\n" +
                "Price: " + selectedTrade.getPriceAtTrade() + "\n\n" +
                "This will also update your portfolio accordingly."
        );
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = traderService.deleteTrade(trader, selectedTrade);
            
            if (success) {
                refreshTrades();
                
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Trade Deleted");
                successAlert.setHeaderText(null);
                successAlert.setContentText("The trade has been deleted and portfolio updated.");
                successAlert.showAndWait();
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText("Failed to delete the trade. Please try again.");
                errorAlert.showAndWait();
            }
        }
    }
}
