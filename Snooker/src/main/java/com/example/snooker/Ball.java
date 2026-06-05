package com.example.snooker;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.util.Duration;

public class Ball {

    public ImageView imageView;
    public Image image;
    public Vector2 velocity;
    public Vector2 position;
    public double radius;
    public double mass = 1;
    public int type; // 0=White, 1=Red, 2=Yellow, 3=Green, 4=Brown, 5=Blue, 6=Pink, 7=black

    private ColorAdjust colorAdjust = new ColorAdjust();
    private boolean isFading = false;
    public boolean isActive = true;
    public boolean isPotting = false;

    public Ball(Image image, double radius, Vector2 startingPosition, int type) {
        this.image = image;
        this.radius = radius;
        this.velocity = new Vector2(0, 0);
        this.type = type;

        imageView = new ImageView(image);
        imageView.setFitWidth(radius * 2);
        imageView.setFitHeight(radius * 2);
        imageView.setEffect(colorAdjust);

        setPosition(startingPosition);
    }

    public void fade(double seconds) {
        if (isFading) return;
        isFading = true;

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(colorAdjust.brightnessProperty(), 0)
                ),
                new KeyFrame(Duration.seconds(seconds),
                        new KeyValue(colorAdjust.brightnessProperty(), -1)
                )
        );
        timeline.play();
    }

    public void setImage(Image image) {
        this.image = image;
        imageView.setImage(image);
    }

    public double getX() {
        return imageView.getX();
    }

    public double getY() {
        return imageView.getY();
    }

    public void setPosition(Vector2 position) {
        this.position = position;
        // Offset the image view so the center of the image is at 'position'
        imageView.setX(position.x - radius);
        imageView.setY(position.y - radius);
    }

    public Vector2 getPosition() {
        return position;
    }

    public double getSizeY() {
        return imageView.getFitHeight();
    }

    public double getSizeX() {
        return imageView.getFitWidth();
    }

    public double getRadius() {
        return radius;
    }

}
