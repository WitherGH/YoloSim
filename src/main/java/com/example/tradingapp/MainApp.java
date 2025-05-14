package com.example.tradingapp;

import com.example.tradingapp.model.Instrument;
import com.example.tradingapp.persistence.DataSnapshot;
import com.example.tradingapp.service.InstrumentService;
import com.example.tradingapp.service.PersistenceService;
import com.example.tradingapp.service.TraderService;
import com.example.tradingapp.util.UIThreadUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

public class MainApp extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Load loader screen first
        FXMLLoader loaderFxml = new FXMLLoader(getClass().getResource("/com/example/tradingapp/ui/LoaderView.fxml"));
        Parent loaderRoot = loaderFxml.load();
        Scene loaderScene = new Scene(loaderRoot, 1024, 768);
        
        // Load main application content
        FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("/com/example/tradingapp/ui/MainView.fxml"));
        Parent mainRoot = mainLoader.load();
        
        // Set the loader scene first
        stage.setTitle("Trading Simulator");
        stage.setScene(loaderScene);
        stage.show();
        
        // Initialize app data in background
        UIThreadUtil.runInBackground(() -> {
            initializeAppData();
            
            try {
                Thread.sleep(3000); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            UIThreadUtil.runOnUIThread(() -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(1000), loaderRoot);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> {
                    // Switch to main scene
                    Scene mainScene = new Scene(mainRoot, 1024, 768);
                    stage.setScene(mainScene);
                });
                fadeOut.play();
            });
        });
    }
    
    private void initializeAppData() {
        TraderService traderSvc = TraderService.getInstance();
        InstrumentService instrSvc = InstrumentService.getInstance();
        PersistenceService ps = new PersistenceService();

        try {
            // Try to load saved state
            DataSnapshot snap = ps.load();
            
            // Process traders
            if (!snap.traders.isEmpty()) {
                UIThreadUtil.runOnUIThread(() -> {
                    traderSvc.setAllTraders(snap.traders);
                    traderSvc.setAllTrades(snap.trades);
                });
            } else {
                System.out.println("No traders in snapshot");
                // Create default values (development only)
                UIThreadUtil.runOnUIThread(() -> {
                    traderSvc.createTrader("Trader 1");
                });
            }
            
            // Process instruments
            if (!snap.instruments.isEmpty()) {
                UIThreadUtil.runOnUIThread(() -> {
                    instrSvc.getStocks().clear();
                    
                    for (Instrument i : snap.instruments) {
                        if ("Stock".equalsIgnoreCase(i.getType())) {
                            instrSvc.getStocks().add(i);
                        }
                    }
                });
            } else {
                System.out.println("No instruments in snapshot – keeping built-in stocks");
            }
            
        } catch (Exception ex) {
            System.out.println("Failed to load saved state: " + ex);
            ex.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        PersistenceService ps = new PersistenceService();
        DataSnapshot snap = new DataSnapshot(
                TraderService.getInstance().getAllTraders(),
                InstrumentService.getInstance().getAllInstruments(),
                TraderService.getInstance().getAllTrades()
        );
        ps.save(snap);
        
        // Shutdown the thread executor
        UIThreadUtil.shutdown();
    }
}
