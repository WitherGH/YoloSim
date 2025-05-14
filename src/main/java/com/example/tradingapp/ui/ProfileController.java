package com.example.tradingapp.ui;

import com.example.tradingapp.model.Holding;
import com.example.tradingapp.model.Trader;
import com.example.tradingapp.service.TraderService;
import com.example.tradingapp.util.SortUtils;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ProfileController {
    @FXML private ComboBox<Trader> cmbTraders;
    @FXML private Button btnAddTrader;
    @FXML private Button btnEditTrader;
    @FXML private Button btnDeleteTrader;
    @FXML private Button btnSortBySymbol;
    @FXML private Button btnSortByQuantity;
    @FXML private Button btnSortByPrice;
    @FXML private TableView<Holding> tblHoldings;
    @FXML private TableColumn<Holding, String> colHoldSym;
    @FXML private TableColumn<Holding, Number> colHoldQty;
    @FXML private TableColumn<Holding, Number> colHoldPrice;
    @FXML private Label profileTitle;
    
    @FXML private TextField txtBudget;
    @FXML private Button btnSetBudget;
    @FXML private Label lblBudget;
    @FXML private Label lblPortfolioBalance;
    @FXML private Label lblBudgetRemaining;
    @FXML private Label lblTotalBalance;

    private final TraderService traderService = TraderService.getInstance();

    @FXML
    public void initialize() {
        // populate and listen for selection
        cmbTraders.setItems(traderService.getTraders());
        cmbTraders.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldT, newT) -> onTraderSelected(newT));

        // check if a trader is already selected in the TraderService
        Trader selectedTrader = traderService.getSelectedTrader();
        if (selectedTrader != null) {
            cmbTraders.getSelectionModel().select(selectedTrader);
        }

        btnAddTrader.setOnAction(e -> {
            TextInputDialog dlg = new TextInputDialog();
            dlg.setTitle("New Trader");
            dlg.setHeaderText("Enter trader name:");
            dlg.showAndWait().ifPresent(name -> {
                if (!name.isBlank()) {
                    traderService.createTrader(name);
                    cmbTraders.getSelectionModel().selectLast();
                }
            });
        });

        btnEditTrader.setOnAction(e -> {
            Trader selected = cmbTraders.getValue();
            if (selected == null) {
                new Alert(Alert.AlertType.WARNING, "Select a trader to rename").showAndWait();
                return;
            }
            TextInputDialog dlg = new TextInputDialog(selected.getName());
            dlg.setTitle("Rename Trader");
            dlg.setHeaderText("Enter new name:");
            dlg.showAndWait().ifPresent(newName -> {
                if (!newName.isBlank()) {
                    traderService.renameTrader(selected, newName);
                    // Refresh combo box so the updated name appears immediately
                    ObservableList<Trader> items = cmbTraders.getItems();
                    cmbTraders.setItems(FXCollections.observableArrayList(items));
                    cmbTraders.getSelectionModel().select(selected);
                }
            });
        });

        btnDeleteTrader.setOnAction(evt -> {
            Trader t = cmbTraders.getValue();
            if (t == null) {
                new Alert(Alert.AlertType.WARNING, "Select a trader first").showAndWait();
                return;
            }
            if (!confirmYes("Delete trader \"" + t.getName() + "\"?")) {
                return;
            }

            traderService.deleteTrader(t);

            ObservableList<Trader> items = traderService.getTraders();
            cmbTraders.setItems(items);
            cmbTraders.getSelectionModel().clearSelection();
        });
        
        btnSortBySymbol.setOnAction(e -> sortHoldingsBySymbol());
        btnSortByQuantity.setOnAction(e -> sortHoldingsByQuantity());
        btnSortByPrice.setOnAction(e -> sortHoldingsByPrice());
        
        setupBudgetControls();

        if (profileTitle != null) {
            profileTitle.setText("Profile");
        }
    }
    

    private void setupBudgetControls() {
        btnSetBudget.setOnAction(e -> {
            Trader trader = cmbTraders.getValue();
            if (trader == null) {
                new Alert(Alert.AlertType.WARNING, "Select a trader first").showAndWait();
                return;
            }
            
            try {
                String budgetStr = txtBudget.getText();
                if (budgetStr == null || budgetStr.isBlank()) {
                    new Alert(Alert.AlertType.ERROR, "Enter a budget amount").showAndWait();
                    return;
                }
                
                double budgetAmount = Double.parseDouble(budgetStr);
                if (budgetAmount < 0) {
                    new Alert(Alert.AlertType.ERROR, "Budget amount cannot be negative").showAndWait();
                    return;
                }
                
                trader.setBudget(budgetAmount);
                updateBalanceDisplay(trader);
                
                // Clear input field and show confirmation
                txtBudget.clear();
                new Alert(Alert.AlertType.INFORMATION, "Budget set successfully").showAndWait();
                
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Invalid budget amount: " + ex.getMessage()).showAndWait();
            }
        });
    }
    

    private void sortHoldingsBySymbol() {
        Trader trader = traderService.getSelectedTrader();
        if (trader == null) return;
        
        List<Holding> holdings = new ArrayList<>(trader.getPortfolio().getHoldings());
        SortUtils.mergeSort(holdings, Comparator.comparing(h -> h.getInstrument().getSymbol()));
        tblHoldings.setItems(FXCollections.observableArrayList(holdings));
        System.out.println("Sorted holdings by symbol using custom merge sort");
    }
    

    private void sortHoldingsByQuantity() {
        Trader trader = traderService.getSelectedTrader();
        if (trader == null) return;
        
        List<Holding> holdings = new ArrayList<>(trader.getPortfolio().getHoldings());
        SortUtils.mergeSort(holdings, Comparator.comparingDouble(Holding::getQuantity));
        tblHoldings.setItems(FXCollections.observableArrayList(holdings));
        System.out.println("Sorted holdings by quantity using custom merge sort");
    }
    

    private void sortHoldingsByPrice() {
        Trader trader = traderService.getSelectedTrader();
        if (trader == null) return;
        List<Holding> holdings = new ArrayList<>(trader.getPortfolio().getHoldings());
        SortUtils.mergeSort(holdings, Comparator.comparingDouble(h -> h.getInstrument().getPrice()));
        tblHoldings.setItems(FXCollections.observableArrayList(holdings));
        System.out.println("Sorted holdings by price using custom merge sort");
    }
    
    
     //Updates all balance-related displays
     
    private void updateBalanceDisplay(Trader trader) {
        if (trader == null) {
            lblBudget.setText("0.00");
            lblPortfolioBalance.setText("0.00");
            lblBudgetRemaining.setText("0.00");
            lblTotalBalance.setText("0.00");
            return;
        }
        
        double portfolioBalance = trader.getPortfolio().calculateTotalBalance();
        double budget = trader.getBudget();
        double totalBalance = portfolioBalance + budget;
        
        lblBudget.setText(String.format("%.2f", budget));
        lblPortfolioBalance.setText(String.format("%.2f", portfolioBalance));
        lblBudgetRemaining.setText(String.format("%.2f", budget));
        lblTotalBalance.setText(String.format("%.2f", totalBalance));
    }

    private void onTraderSelected(Trader trader) {
        if (trader == null) {
            profileTitle.setText("Profile");
            tblHoldings.setItems(FXCollections.emptyObservableList());
            updateBalanceDisplay(null);
            return;
        }

        //set the selected trader in TraderService so it's available globally
        traderService.setSelectedTrader(trader);

        profileTitle.setText("Profile of " + trader.getName());
        tblHoldings.setItems(trader.getPortfolio().getHoldings());
        
        colHoldSym.setCellValueFactory(c -> c.getValue().getInstrument().symbolProperty());
        colHoldQty.setCellValueFactory(c -> c.getValue().quantityProperty());
        colHoldPrice.setCellValueFactory(c ->
                new SimpleDoubleProperty(c.getValue().getInstrument().getPrice())
        );
        updateBalanceDisplay(trader);
    }

    private boolean confirmYes(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                message,
                ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = alert.showAndWait();
        return result.filter(btn -> btn == ButtonType.YES).isPresent();
    }
}
