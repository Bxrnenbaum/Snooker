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
    public Vector2 nominalPosition;

    private ColorAdjust colorAdjust = new ColorAdjust();
    private boolean isFading = false;
    public boolean isActive = true;
    BallType ballType;
    public boolean isPotting;

    public Ball(Image image, double radius, Vector2 startingPosition, BallType ballType) {
        this.image = image;
        this.radius = radius;
        this.velocity = new Vector2(0, 0);
        this.ballType = ballType;

        imageView = new ImageView(image);
        imageView.setFitWidth(radius * 2);
        imageView.setFitHeight(radius * 2);
        imageView.setEffect(colorAdjust);

        setPosition(startingPosition);
        nominalPosition = startingPosition;
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

    public void unfade() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(colorAdjust.brightnessProperty(), -1)
                ),
                new KeyFrame(Duration.seconds(0.01),
                        new KeyValue(colorAdjust.brightnessProperty(), 0)
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
