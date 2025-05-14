package com.example.tradingapp.ui;

import com.example.tradingapp.model.Instrument;
import com.example.tradingapp.service.InstrumentService;
import com.example.tradingapp.service.StockDataService;
import com.example.tradingapp.service.MarketDataService.OHLC;
import com.example.tradingapp.util.UIThreadUtil;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChartsController {

    @FXML private ComboBox<Instrument> cmbInstruments;
    @FXML private ComboBox<String> cmbRange;
    @FXML private Button btnLoadData;
    @FXML private CandleStickChart stockChart;
    @FXML private DatePicker datePicker;
    @FXML private Label stockNameLabel;
    @FXML private Label openLabel;
    @FXML private Label closeLabel;
    @FXML private Label highLabel;
    @FXML private Label lowLabel;
    @FXML private Label changeLabel;

    private static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");
    private static final Color CHART_COLOR = Color.web("#1a0b2a");
    private static final double CHART_OPACITY = 0.5;

    private final InstrumentService instrSvc = InstrumentService.getInstance();
    private final StockDataService stockDataSvc = StockDataService.getInstance();

    @FXML 
    public void initialize() {
        ObservableList<Instrument> allInstruments = FXCollections.observableArrayList(instrSvc.getStocks());
        cmbInstruments.setItems(allInstruments);
        datePicker.setValue(stockDataSvc.getCurrentDate());
        
        XYChart.Series<String, Number> emptySeries = new XYChart.Series<>();
        stockChart.setData(FXCollections.observableArrayList(emptySeries));
        
        configureChart(stockChart);
        enableZoomAndPan(stockChart);
        
        instrSvc.getStocks().addListener(this::onInstrumentsChanged);
        
        cmbInstruments.valueProperty().addListener((obs, old, newInstr) -> {
            if (newInstr != null) {
                updateStockNameLabel(newInstr);
                loadSelectedData();
            }
        });
        
        datePicker.valueProperty().addListener((obs, old, newDate) -> {
            if (newDate != null) {
                loadSelectedData();
            }
        });
        
        if (!allInstruments.isEmpty()) {
            cmbInstruments.setValue(allInstruments.get(0));
        } else {
            System.out.println("No instruments in instrument service");
        }
    }
    
    private void updateStockNameLabel(Instrument instrument) {
        stockNameLabel.setText(instrument.getName());
    }

    private void onInstrumentsChanged(ListChangeListener.Change<? extends Instrument> change) {
        cmbInstruments.setItems(FXCollections.observableArrayList(instrSvc.getStocks()));
    }

    private void configureChart(CandleStickChart chart) {
        CategoryAxis xAxis = (CategoryAxis) chart.getXAxis();
        NumberAxis yAxis = (NumberAxis) chart.getYAxis();
        
        // Enable auto-ranging when data is initialized
        if (chart.getData() != null) {
            xAxis.setAutoRanging(true);
        }
        
        // Disable auto-ranging for Y axis to set bounds manually
        yAxis.setAutoRanging(false);
        
        chart.setFocusTraversable(true);
        chart.setTitle("Stock Price");
        yAxis.setLabel("Price");
        xAxis.setLabel("Time");
        
        // Set appearance for line and fill area
        chart.setLineColor(CHART_COLOR);
        chart.setFillColor(Color.web(CHART_COLOR.toString(), CHART_OPACITY));
        chart.setShowPriceLine(true);
        chart.setShowAreaFill(true);
        chart.setLegendVisible(false);
    }

    private void setYAxisBounds(CandleStickChart chart, double minValue, double maxValue) {
        NumberAxis yAxis = (NumberAxis) chart.getYAxis();
        
        double range = maxValue - minValue;
        
        double upperBound = maxValue + (range * 0.2);
        
        double lowerBound = Math.max(0, minValue - (range * 0.1));
        
        yAxis.setLowerBound(lowerBound);
        yAxis.setUpperBound(upperBound);
        yAxis.setTickUnit(range / 5);
    }

    private void loadSelectedData() {
        Instrument inst = cmbInstruments.getValue();
        LocalDate date = datePicker.getValue();
        
        if (inst == null || date == null) return;
        
        updateStockNameLabel(inst);
        
        UIThreadUtil.runTaskWithCallback(
            () -> stockDataSvc.getOHLCForDay(inst.getSymbol(), date),
            bars -> {
                if (bars.isEmpty()) {
                    System.out.println("No data available for " + inst.getSymbol() + " on " + date);
                    return;
                }
                
                OHLC firstBar = bars.get(0);
                OHLC lastBar = bars.get(bars.size() - 1);
                
                double open = firstBar.getOpen();
                double close = lastBar.getClose();
                
                double high = bars.stream().mapToDouble(OHLC::getHigh).max().orElse(open);
                double low = bars.stream().mapToDouble(OHLC::getLow).min().orElse(open);
                
                setYAxisBounds(stockChart, low, high);
                
                double change = close - open;
                double changePercent = open != 0 ? (change / open * 100) : 0;
                
                updatePriceLabels(open, close, high, low, change, changePercent);
                updateChartData(bars);
            }
        );
    }
    
    private void updatePriceLabels(double open, double close, double high, double low, double change, double changePercent) {
        openLabel.setText(String.format("Open: $%.2f", open));
        closeLabel.setText(String.format("Close: $%.2f", close));
        highLabel.setText(String.format("High: $%.2f", high));
        lowLabel.setText(String.format("Low: $%.2f", low));
        
        String changeSign = change >= 0 ? "+" : "";
        changeLabel.setText(String.format("Change: %s$%.2f (%.2f%%)", changeSign, change, changePercent));
        
        if (change > 0) {
            changeLabel.setStyle("-fx-text-fill: green;");
        } else if (change < 0) {
            changeLabel.setStyle("-fx-text-fill: red;");
        } else {
            changeLabel.setStyle("");
        }
    }
    
    private void updateChartData(List<OHLC> bars) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        
        for (OHLC bar : bars) {
            String time = bar.getTime().format(HHMM);
            XYChart.Data<String, Number> d = new XYChart.Data<>(time, bar.getClose());
            d.setExtraValue(new CandleStickChart.ExtraData(
                    bar.getOpen(), bar.getClose(),
                    bar.getHigh(), bar.getLow()));
            series.getData().add(d);
        }
        
        stockChart.setData(FXCollections.observableArrayList(series));
        makeCandlesTransparent(stockChart);
    }

    private void makeCandlesTransparent(CandleStickChart chart) {
        chart.lookupAll(".candlestick-candle").forEach(n -> n.setMouseTransparent(true));
    }

    private void enableZoomAndPan(CandleStickChart chart) {
        NumberAxis yAxis = (NumberAxis) chart.getYAxis();

        // Wheel zoom 
        chart.addEventFilter(ScrollEvent.SCROLL, ev -> {
            double factor = ev.getDeltaY() > 0 ? 0.9 : 1.1;
            double lo = yAxis.getLowerBound(), hi = yAxis.getUpperBound();
            double mid = (hi + lo) / 2, half = (hi - lo) / 2 * factor;
            yAxis.setLowerBound(mid - half);
            yAxis.setUpperBound(mid + half);
            ev.consume();
        });

        // Drag-and-pan 
        final double[] anchor = new double[1];
        chart.addEventFilter(MouseEvent.MOUSE_PRESSED, ev -> anchor[0] = ev.getY());
        chart.addEventFilter(MouseEvent.MOUSE_DRAGGED, ev -> {
            double shift = (anchor[0] - ev.getY())
                    * (yAxis.getUpperBound() - yAxis.getLowerBound())
                    / chart.getHeight();
            yAxis.setLowerBound(yAxis.getLowerBound() + shift);
            yAxis.setUpperBound(yAxis.getUpperBound() + shift);
            anchor[0] = ev.getY();
        });
    }
}
