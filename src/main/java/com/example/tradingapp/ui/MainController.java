package com.example.tradingapp.ui;

import com.example.tradingapp.persistence.DataSnapshot;
import com.example.tradingapp.service.InstrumentService;
import com.example.tradingapp.service.PersistenceService;
import com.example.tradingapp.service.TraderService;
import javafx.scene.Parent;
import java.net.URL;
import java.util.Objects;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.ToggleButton;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

public class MainController {
    @FXML private VBox sidebar;
    @FXML private ToggleButton btnToggle;
    @FXML private Button btnLoadData;
    @FXML private Button btnSaveData;
    @FXML private StackPane mainContainer;
    @FXML private Button btnHideSidebar;
    @FXML private Button btnShowSidebar;
    @FXML private VBox profileSection;
    @FXML private VBox profileButtons;
    @FXML private VBox instrumentsSection;
    @FXML private VBox instrumentsButtons;
    @FXML private VBox chartsSection;
    @FXML private VBox chartsButtons;
    @FXML private VBox manageDataSection;
    @FXML private VBox manageDataButtons;

    private final PersistenceService ps = new PersistenceService();
    @FXML
    public void initialize() {
        sidebar.setVisible(true);
        sidebar.setManaged(true);
        btnShowSidebar.setVisible(false);
        btnShowSidebar.setManaged(false);

        try { openProfile(); } catch (Exception ignored) {}
        btnHideSidebar.setOnAction(e -> {
            sidebar.setVisible(false);
            sidebar.setManaged(false);
            btnShowSidebar.setVisible(true);
            btnShowSidebar.setManaged(true);
        });
        btnShowSidebar.setOnAction(e -> {
            sidebar.setVisible(true);
            sidebar.setManaged(true);
            btnShowSidebar.setVisible(false);
            btnShowSidebar.setManaged(false);
        });
        btnLoadData.setOnAction(e -> doLoad());
        btnSaveData.setOnAction(e -> doSave());

        setSectionExpanded(profileSection, profileButtons, true);
        setSectionExpanded(instrumentsSection, instrumentsButtons, true);
        setSectionExpanded(chartsSection, chartsButtons, true);
        setSectionExpanded(manageDataSection, manageDataButtons, true);
    }

    private void doLoad() {
        try {
            DataSnapshot snap = ps.load();
            InstrumentService.getInstance().getStocks().setAll(snap.instruments.stream()
                    .filter(i -> "Stock".equalsIgnoreCase(i.getType()))
                    .toList());
            TraderService.getInstance().setAllTraders(snap.traders);
            TraderService.getInstance().setAllTrades(snap.trades);
            new Alert(Alert.AlertType.INFORMATION, "Data loaded").showAndWait();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Load failed: "+ex.getMessage()).showAndWait();
        }
    }

    private void doSave() {
        try {
            DataSnapshot snap = new DataSnapshot(
                    TraderService.getInstance().getAllTraders(),
                    InstrumentService.getInstance().getAllInstruments(),
                    TraderService.getInstance().getAllTrades()
            );
            ps.save(snap);
            new Alert(Alert.AlertType.INFORMATION, "Data saved").showAndWait();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Save failed: "+ex.getMessage()).showAndWait();
        }
    }

    @FXML
    void openProfile() throws Exception {
        loadView("/com/example/tradingapp/ui/ProfileView.fxml");
    }

    @FXML
    void openStocks() throws Exception {
        loadView("/com/example/tradingapp/ui/InstrumentsView.fxml");
        Object controller = mainContainer.getChildren().get(0).getUserData();
        if (controller instanceof InstrumentsController) {
            ((InstrumentsController) controller).setInstrumentType("Stock");
        }
    }

    @FXML
    void openTrades() throws Exception {
        loadView("/com/example/tradingapp/ui/TradesView.fxml");
    }

    @FXML
    void openCharts() throws Exception {
        loadView("/com/example/tradingapp/ui/ChartsView.fxml");
    }

    private void loadView(String fxml) throws Exception {
        URL url = Objects.requireNonNull(
                getClass().getResource(fxml),
                "Cannot find FXML: " + fxml
        );

        FXMLLoader loader = new FXMLLoader(url);
        Parent view = loader.load();
        view.setUserData(loader.getController());

        // Fade transition logic
        if (!mainContainer.getChildren().isEmpty()) {
            var oldView = mainContainer.getChildren().get(0);
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), oldView);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                mainContainer.getChildren().setAll(view);
                if (btnShowSidebar != null && !mainContainer.getChildren().contains(btnShowSidebar)) {
                    mainContainer.getChildren().add(btnShowSidebar);
                }
                view.setOpacity(0.0);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(250), view);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fadeOut.play();
        } else {
            mainContainer.getChildren().setAll(view);
            if (btnShowSidebar != null && !mainContainer.getChildren().contains(btnShowSidebar)) {
                mainContainer.getChildren().add(btnShowSidebar);
            }
            view.setOpacity(0.0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(250), view);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        }
    }

    private void setSectionExpanded(VBox section, VBox buttons, boolean expanded) {
        section.getStyleClass().removeAll("collapsed", "expanded");
        if (expanded) {
            section.getStyleClass().add("expanded");
            buttons.setVisible(true);
            buttons.setManaged(true);
        } else {
            section.getStyleClass().add("collapsed");
            buttons.setVisible(false);
            buttons.setManaged(false);
        }
    }

    @FXML
    private void toggleProfileSection() {
        toggleSidebarSection(profileSection, profileButtons);
    }

    @FXML
    private void toggleInstrumentsSection() {
        toggleSidebarSection(instrumentsSection, instrumentsButtons);
    }

    @FXML
    private void toggleChartsSection() {
        toggleSidebarSection(chartsSection, chartsButtons);
    }

    @FXML
    private void toggleManageDataSection() {
        toggleSidebarSection(manageDataSection, manageDataButtons);
    }

    private void toggleSidebarSection(VBox section, VBox buttons) {
        boolean collapsed = section.getStyleClass().contains("collapsed");
        section.getStyleClass().removeAll("collapsed", "expanded");
        if (collapsed) {
            section.getStyleClass().add("expanded");
            buttons.setVisible(true);
            buttons.setManaged(true);
        } else {
            section.getStyleClass().add("collapsed");
            buttons.setVisible(false);
            buttons.setManaged(false);
        }
    }
}
