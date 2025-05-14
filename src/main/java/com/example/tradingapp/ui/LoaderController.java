package com.example.tradingapp.ui;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class LoaderController {
    @FXML private ImageView logoImage;
    
    @FXML
    public void initialize() {
        // Створюємо комбіновану анімацію для зображення
        setupAnimations(logoImage);
    }
    
    private void setupAnimations(ImageView imageView) {
        // Створюємо ефект Gaussian blur з початковим значенням
        GaussianBlur blur = new GaussianBlur(0);
        imageView.setEffect(blur);
        
        // 1. Анімація blur
        Timeline blurTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(blur.radiusProperty(), 0, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.seconds(1.5), 
                new KeyValue(blur.radiusProperty(), 10, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.seconds(3), 
                new KeyValue(blur.radiusProperty(), 0, Interpolator.EASE_BOTH))
        );
        
        // 2. Анімація пульсації розміру
        // Запам'ятовуємо початковий розмір
        double originalWidth = imageView.getFitWidth();
        
        Timeline pulseTimeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(imageView.fitWidthProperty(), originalWidth, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.seconds(1.5),
                new KeyValue(imageView.fitWidthProperty(), originalWidth * 1.05, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.seconds(3),
                new KeyValue(imageView.fitWidthProperty(), originalWidth, Interpolator.EASE_BOTH))
        );
        
        // Об'єднуємо анімації
        ParallelTransition parallelTransition = new ParallelTransition(blurTimeline, pulseTimeline);
        parallelTransition.setCycleCount(Animation.INDEFINITE);
        
        // Запускаємо анімацію
        parallelTransition.play();
        
        System.out.println("Logo animations started with original width: " + originalWidth);
    }
} 