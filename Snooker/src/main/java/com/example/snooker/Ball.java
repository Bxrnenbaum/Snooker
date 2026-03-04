package com.example.snooker;

import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

public class Ball {

    public ImageView imageView;
    public Image image;
    public Vector2 velocity;
    public Vector2 position;
    public double radius;
    public double mass = 100; //mass of a standard snooker ball is usually 142g

    public Ball(Image image, double radius) {
        this.image = image;
        imageView = new ImageView(image);
        imageView.setFitWidth(radius*2);
        imageView.setFitHeight(radius*2);
        velocity = new Vector2(0, 0);
    }

    public Ball(Image image, double radius, Vector2 startingPosition){
        this.image = image;
        imageView = new ImageView(image);
        imageView.setFitWidth(radius*2);
        imageView.setFitHeight(radius*2);
        setPosition(startingPosition);
        velocity = new Vector2(0, 0);
    }

    public Ball(Image image, double radius, Vector2 startingPosition, Vector2 startingVelocity){
        this.image = image;
        imageView = new ImageView(image);
        imageView.setFitWidth(radius*2);
        imageView.setFitHeight(radius*2);
        setPosition(startingPosition);
        this.velocity = startingVelocity;
        this.radius = radius;
    }

    public void setImage(Image image) {
        this.image = image;
        imageView.setImage(image);
    }

    public double getX(){
        return imageView.getX();
    }

    public double getY(){
        return imageView.getY();
    }

    public void setPosition(Vector2 position){
        this.position = position;
        // Offset the image view so the center of the image is at 'position'
        imageView.setX(position.x - radius);
        imageView.setY(position.y - radius);
    }
    public Vector2 getPosition(){
        return position;
    }

    public double getSizeY(){
        return imageView.getFitHeight();
    }

    public double getSizeX(){
        return imageView.getFitWidth();
    }

    public double getRadius(){
        return radius;
    }


}
