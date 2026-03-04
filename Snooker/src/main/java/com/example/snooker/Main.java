package com.example.snooker;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {

    private long lastUpdate = 0;
    private Ball[] balls = new Ball[25];
    int i = 0;

    @Override
    public void start(Stage primaryStage) {

        balls[0] = new Ball(null, 11.5, new Vector2(296, 354), 1);
        balls[1] = new Ball(null, 11.5, new Vector2(296, 377), 1);
        balls[2] = new Ball(null, 11.5, new Vector2(296, 400), 1);
        balls[3] = new Ball(null, 11.5, new Vector2(296, 423), 1);
        balls[4] = new Ball(null, 11.5, new Vector2(296, 446), 1);
        balls[5] = new Ball(null, 11.5, new Vector2(316, 434.5), 1);
        balls[6] = new Ball(null, 11.5, new Vector2(316, 411.5), 1);
        balls[7] = new Ball(null, 11.5, new Vector2(316, 388.5), 1);
        balls[8] = new Ball(null, 11.5, new Vector2(316, 365.5), 1);
        balls[9] = new Ball(null, 11.5, new Vector2(336, 423), 1);
        balls[10] = new Ball(null, 11.5, new Vector2(336, 400), 1);
        balls[11] = new Ball(null, 11.5, new Vector2(336, 377), 1);
        balls[12] = new Ball(null, 11.5, new Vector2(356, 411.5), 1);
        balls[13] = new Ball(null, 11.5, new Vector2(356, 388.5), 1);
        balls[14] = new Ball(null, 11.5, new Vector2(376, 400), 1);

        balls[15] = new Ball(null, 11.5, new Vector2(1280, 467), 2);
        balls[16] = new Ball(null, 11.5, new Vector2(1280, 333), 3);
        balls[17] = new Ball(null, 11.5, new Vector2(1280, 400), 4);
        balls[18] = new Ball(null, 11.5, new Vector2(800, 400), 5);
        balls[19] = new Ball(null, 11.5, new Vector2(400, 400), 6);
        balls[20] = new Ball(null, 11.5, new Vector2(146, 400), 7);

        balls[21] = new Ball(null, 11.5, new Vector2(300, 200), 0);

        balls[21].velocity = new Vector2(200, -2500); //simulating a strong break



        for (Ball ball : balls) {
            if (ball == null) continue;
            ball.setImage(new Image("/ball1.png"));
        }

        //Setup scene
        Pane root = new Pane();

        for (Ball ball : balls) {
            if (ball == null) continue;
            root.getChildren().add(ball.imageView);
        }

        Scene scene = new Scene(root, 1600, 800);

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }

                // Calculate delta time (seconds elapsed since last frame)
                double deltaTime = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;

                //calls update function, called once per frame
                update(deltaTime, scene);
            }
        }.start();

        primaryStage.setTitle("Snooker Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void update(double deltaTime, Scene scene) {

        //loop through every ball to check wall collisions and apply movement independently
        for (Ball ball : balls) {
            if (ball == null) continue;

            // move balls. this is completely independent of visual movement (javafx image) as it could cause stutters etc

            ball.position = Vector2.sum(ball.position, Vector2.scalar(deltaTime, ball.velocity));

            // check for wall collisions and invert velocity accordingly with a bit of energy loss (returnEnergy)
            double returnEnergy = .75;
            if (ball.position.x > scene.getWidth() - ball.radius) {
                ball.velocity.x = -Math.abs(ball.velocity.x) * returnEnergy;
                ball.position.x = scene.getWidth() - ball.radius;
            } else if (ball.position.x < ball.radius) {
                ball.velocity.x = Math.abs(ball.velocity.x) * returnEnergy;
                ball.position.x = ball.radius;
            }

            if (ball.position.y > scene.getHeight() - ball.radius) {
                ball.velocity.y = -Math.abs(ball.velocity.y) * returnEnergy;
                ball.position.y = scene.getHeight() - ball.radius;
            } else if (ball.position.y < ball.radius) {
                ball.velocity.y = Math.abs(ball.velocity.y) * returnEnergy;
                ball.position.y = ball.radius;
            }

            // apply friction
            ball.velocity = Vector2.scalar(.997, ball.velocity);
        }

        // resolve collisions between balls
        handleCollisions();

        // sync graphics (javafx images) to the movement of the balls
        for (Ball ball : balls) {
            if (ball == null) continue;
            ball.imageView.setX(ball.position.x - ball.radius);
            ball.imageView.setY(ball.position.y - ball.radius);

            // rotate ball because looks cool
            double speed = Math.sqrt(ball.velocity.x * ball.velocity.x + ball.velocity.y * ball.velocity.y);
            ball.imageView.setRotate(ball.imageView.getRotate() + (speed * deltaTime));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void handleCollisions() {
        for (int i = 0; i < balls.length; i++) {
            if (balls[i] == null) continue;
            for (int j = i + 1; j < balls.length; j++) {
                if (balls[j] == null) continue;

                double dist = Vector2.distance(balls[i].getPosition(), balls[j].getPosition());
                double minDist = balls[i].radius + balls[j].radius;

                //checks if collision has happened and applies counter forces accordingly
                if (dist < minDist) {
                    Vector2 diff = Vector2.difference(balls[i].getPosition(), balls[j].getPosition());
                    Vector2 normal = Vector2.normalized(diff);
                    Vector2 relVel = Vector2.difference(balls[i].velocity, balls[j].velocity);

                    double speedAlongNormal = Vector2.dot(relVel, normal);

                    // make sure collisions are only resolved if balls are approaching each other
                    if (speedAlongNormal < 0) {
                        double e = .95;
                        double jImpulse = (-(1 + e) * speedAlongNormal) / ((1 / balls[i].mass) + (1 / balls[j].mass));

                        Vector2 impulseVec = Vector2.scalar(jImpulse, normal);

                        balls[i].velocity = Vector2.sum(balls[i].velocity, Vector2.scalar(1 / balls[i].mass, impulseVec));
                        balls[j].velocity = Vector2.difference(balls[j].velocity, Vector2.scalar(1 / balls[j].mass, impulseVec));

                        //corrects overlap
                        double overlap = minDist - dist;
                        Vector2 correction = Vector2.scalar(overlap / 2.0, normal);
                        balls[i].setPosition(Vector2.sum(balls[i].getPosition(), correction));
                        balls[j].setPosition(Vector2.difference(balls[j].getPosition(), correction));
                    }
                }
            }
        }
    }
}