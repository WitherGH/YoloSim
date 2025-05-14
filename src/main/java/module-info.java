module com.example.tradingapp {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires java.net.http;


    opens com.example.tradingapp.ui to javafx.fxml;
    opens com.example.tradingapp to javafx.graphics;
    opens com.example.tradingapp.model to com.fasterxml.jackson.databind;
    opens com.example.tradingapp.persistence to com.fasterxml.jackson.databind;
}
