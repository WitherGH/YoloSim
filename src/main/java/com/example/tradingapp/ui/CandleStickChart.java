package com.example.tradingapp.ui;

import javafx.scene.chart.Axis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Polygon;
import java.util.ArrayList;
import java.util.List;

public class CandleStickChart
        extends XYChart<String,Number> {

    private Polyline priceLine;
    private Polygon fillArea;
    private boolean showPriceLine = true;
    private boolean showAreaFill = true;
    private Color lineColor = Color.BLUE;
    private Color fillColor = Color.rgb(0, 0, 255, 0.3); 

    public CandleStickChart() {
        this(new CategoryAxis(), new NumberAxis());
    }

    public static class ExtraData {
        public final double open, close, high, low;
        public ExtraData(double open,double close,double high,double low) {
            this.open = open;
            this.close= close;
            this.high = high;
            this.low  = low;
        }
    }

    public CandleStickChart(Axis<String> xAxis, Axis<Number> yAxis) {
        super(xAxis,yAxis);
        setAnimated(false);
        
        ((NumberAxis) yAxis).setUpperBound(105);  
        ((NumberAxis) yAxis).setAutoRanging(true); 
        
        priceLine = new Polyline();
        priceLine.setStroke(lineColor);
        priceLine.setStrokeWidth(2);
        priceLine.setOpacity(1.0);
        
        fillArea = new Polygon();
        fillArea.setFill(fillColor);
        fillArea.setOpacity(0.3);
        fillArea.setId("chart-fill-area");
        fillArea.setMouseTransparent(true);
        
        getPlotChildren().addAll(fillArea, priceLine);
    }
    
    public void setShowPriceLine(boolean show) {
        this.showPriceLine = show;
        priceLine.setVisible(show);
    }
    
    public void setShowAreaFill(boolean show) {
        this.showAreaFill = show;
        fillArea.setVisible(show);
    }
    
    public void setLineColor(Color color) {
        this.lineColor = color;
        priceLine.setStroke(color);
    }
    
    public void setFillColor(Color color) {
        this.fillColor = color;
        fillArea.setFill(color);
        fillArea.setOpacity(color.getOpacity() < 1.0 ? color.getOpacity() : 0.3);
    }

    @Override
    protected void layoutPlotChildren() {
        priceLine.getPoints().clear();
        fillArea.getPoints().clear();
        
        List<Double> linePoints = new ArrayList<>();
        List<Double> areaPoints = new ArrayList<>();
        Double maxYAxisPos = null;
        
        for (Series<String,Number> series: getData()) {
            for (Data<String,Number> item : series.getData()) {
                ExtraData ed = (ExtraData)item.getExtraValue();
                double x     = getXAxis().getDisplayPosition(item.getXValue());
                double yOpen = getYAxis().getDisplayPosition(ed.open);
                double yClose= getYAxis().getDisplayPosition(ed.close);
                double yHigh = getYAxis().getDisplayPosition(ed.high);
                double yLow  = getYAxis().getDisplayPosition(ed.low);

                double width = 12; 
                double top    = Math.min(yOpen,yClose);
                double height = Math.abs(yClose - yOpen);

                linePoints.add(x);
                linePoints.add(yClose);
                
                areaPoints.add(x);
                areaPoints.add(yClose);
                
                if (maxYAxisPos == null || maxYAxisPos < getYAxis().getDisplayPosition(0)) {
                    maxYAxisPos = getYAxis().getDisplayPosition(0);
                }

                Node node = item.getNode();
                if (node == null) {
                    Rectangle body = new Rectangle(width, height);
                    body.setFill(Color.TRANSPARENT);
                    body.setOpacity(0.0);
                    body.getStyleClass().add("candlestick-body");

                    Line wick = new Line(width/2, 0, width/2, yHigh-yLow);
                    wick.setTranslateY(top - yLow);
                    wick.setStroke(Color.TRANSPARENT);
                    wick.setOpacity(0.0);
                    wick.getStyleClass().add("candlestick-wick");

                    Group candle = new Group(wick, body);
                    candle.getStyleClass().add("candlestick-candle");
                    item.setNode(candle);
                    getPlotChildren().add(candle);
                    node = candle;
                } else {
                    Group candle = (Group)node;
                    Rectangle body = (Rectangle)candle.getChildren().get(1);
                    Line wick = (Line)candle.getChildren().get(0);

                    body.setHeight(height);

                    wick.setEndY(yHigh-yLow);
                    wick.setTranslateY(top - yLow);
                }

                node.setLayoutX(x - width/2);
                node.setLayoutY(yLow);
            }
        }
        
        if (!linePoints.isEmpty() && maxYAxisPos != null) {
            priceLine.getPoints().addAll(linePoints);
            priceLine.setVisible(showPriceLine);
            
            areaPoints.addAll(getAreaClosePoints(linePoints, maxYAxisPos));
            fillArea.getPoints().addAll(areaPoints);
            fillArea.setVisible(showAreaFill);
        }
    }
    
    private List<Double> getAreaClosePoints(List<Double> points, double maxY) {
        List<Double> closePoints = new ArrayList<>();
        
        double lastX = points.get(points.size() - 2);
        closePoints.add(lastX);
        closePoints.add(maxY);
        
        double firstX = points.get(0);
        closePoints.add(firstX);
        closePoints.add(maxY);
        
        return closePoints;
    }

    public javafx.collections.ObservableList<Node> getPlotChildrenPublic() {
        return super.getPlotChildren();
    }

    @Override
    protected void dataItemAdded(Series<String,Number> series, int itemIndex, Data<String,Number> item) {
    }

    @Override
    protected void dataItemRemoved(Data<String,Number> item, Series<String,Number> series) {
        getPlotChildren().remove(item.getNode());
    }

    @Override
    protected void seriesAdded(Series<String,Number> series, int seriesIndex) {
        for (Data<String,Number> d : series.getData())
            if (d.getNode() != null)
                getPlotChildren().add(d.getNode());
    }

    @Override
    protected void seriesRemoved(Series<String,Number> series) {
        for (Data<String,Number> d : series.getData())
            getPlotChildren().remove(d.getNode());
    }

    @Override
    protected void dataItemChanged(Data<String,Number> item) {
    }
}
