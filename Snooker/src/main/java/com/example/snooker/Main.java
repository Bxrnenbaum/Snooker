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

        //Initializing balls for testing with random velocities
        balls[0] = new Ball(null, 30, new Vector2(100, 100), new Vector2(500, 200));
        balls[1] = new Ball(null, 25, new Vector2(300, 100), new Vector2(-400, 600));
        balls[2] = new Ball(null, 35, new Vector2(500, 100), new Vector2(200, -800));
        balls[3] = new Ball(null, 20, new Vector2(700, 100), new Vector2(-700, -300));
        balls[4] = new Ball(null, 40, new Vector2(900, 100), new Vector2(100, 900));

        balls[5] = new Ball(null, 30, new Vector2(100, 300), new Vector2(600, -100));
        balls[6] = new Ball(null, 25, new Vector2(300, 300), new Vector2(-200, -500));
        balls[7] = new Ball(null, 35, new Vector2(500, 300), new Vector2(800, 400));
        balls[8] = new Ball(null, 20, new Vector2(700, 300), new Vector2(-500, 200));
        balls[9] = new Ball(null, 45, new Vector2(900, 300), new Vector2(300, -600));

        balls[10] = new Ball(null, 30, new Vector2(100, 500), new Vector2(-400, 400));
        balls[11] = new Ball(null, 25, new Vector2(300, 500), new Vector2(700, -700));
        balls[12] = new Ball(null, 50, new Vector2(500, 500), new Vector2(-100, 1000));
        balls[13] = new Ball(null, 20, new Vector2(700, 500), new Vector2(900, 300));
        balls[14] = new Ball(null, 35, new Vector2(900, 500), new Vector2(-800, -200));

        balls[15] = new Ball(null, 30, new Vector2(100, 700), new Vector2(250, 850));
        balls[16] = new Ball(null, 25, new Vector2(300, 700), new Vector2(-650, -450));
        balls[17] = new Ball(null, 35, new Vector2(500, 700), new Vector2(400, -350));
        balls[18] = new Ball(null, 20, new Vector2(700, 700), new Vector2(-300, 750));
        balls[19] = new Ball(null, 40, new Vector2(900, 700), new Vector2(550, -150));

        balls[20] = new Ball(null, 30, new Vector2(100, 900), new Vector2(-900, 100));
        balls[21] = new Ball(null, 25, new Vector2(300, 900), new Vector2(150, -950));
        balls[22] = new Ball(null, 45, new Vector2(500, 900), new Vector2(-350, 450));
        balls[23] = new Ball(null, 20, new Vector2(700, 900), new Vector2(750, 650));
        balls[24] = new Ball(null, 35, new Vector2(900, 900), new Vector2(-200, -850));

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

        Scene scene = new Scene(root, 800, 800);

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
            ball.velocity = Vector2.scalar(.998, ball.velocity);
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