package com.example.snooker;

import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

public class Ball {

    public ImageView imageView;
    public Image image;
    public Vector2 velocity;
    public Vector2 position;
    public double radius;
    public double mass = 1; //mass of a standard snooker ball is usually 142g
    public int type; // 0=White, 1=Red, 2=Yellow, 3=Green, 4=Brown, 5=Blue, 6=Pink, 7=black


    public Ball(Image image, double radius, Vector2 startingPosition, int type){
        this.image = image;
        imageView = new ImageView(image);
        imageView.setFitWidth(radius*2);
        imageView.setFitHeight(radius*2);
        setPosition(startingPosition);
        this.radius = radius;
        this.velocity = new Vector2(0, 0);
        this.type = type;
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
