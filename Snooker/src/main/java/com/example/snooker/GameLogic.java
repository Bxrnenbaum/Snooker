package com.example.snooker;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;

public class GameLogic
{

    private final int BASE_WIDTH = 3200;
    private final int BASE_HEIGHT = 1600;

    private final double SCALING_FACTOR = .5;

    private Ball[] balls = new Ball[22];

    private InputHandler inputHandler;
    private boolean mouseWasPressed;

    Vector2 startingPoint;
    Vector2 endPoint;

    private Line aimLine;

    public void onStart(Scene scene, Pane pane){ //gets called once when game is started

        balls[0] = new Ball(new Image("/redBall.png"), 11.5, new Vector2(296, 354), 1);
        balls[1] = new Ball(new Image("/redBall.png"), 11.5, new Vector2(296, 377), 1);
        balls[2] = new Ball(new Image("/redBall.png"), 11.5, new Vector2(296, 400), 1);
        balls[3] = new Ball(new Image("/redBall.png"), 11.5, new Vector2(296, 423), 1);
        balls[4] = new Ball(new Image("/redBall.png"), 11.5, new Vector2(296, 446), 1);
        balls[5] = new Ball(new Image("/redBall.png"), 11.5, new Vector2(316, 434.5), 1);
        balls[6] = new Ball(new Image("/redBall.png"), 11.5, new Vector2(316, 411.5), 1);
        balls[7] = new Ball(new Image("/redBall.png"), 11.5, new Vector2(316, 388.5), 1);
        balls[8] = new Ball(new Image("/redBall.png"), 11.5, new Vector2(316, 365.5), 1);
        balls[9] = new Ball(new Image("/redBall.png"), 11.5, new Vector2(336, 423), 1);
        balls[10] = new Ball(new Image("/redBall.png"), 11.5, new Vector2(336, 400), 1);
        balls[11] = new Ball(new Image("/redBall.png"), 11.5, new Vector2(336, 377), 1);
        balls[12] = new Ball(new Image("/redBall.png"), 11.5, new Vector2(356, 411.5), 1);
        balls[13] = new Ball(new Image("/redBall.png"), 11.5, new Vector2(356, 388.5), 1);
        balls[14] = new Ball(new Image("/redBall.png"), 11.5, new Vector2(376, 400), 1);

        balls[15] = new Ball(new Image("/greenBall.png"), 11.5, new Vector2(1280, 467), 2);
        balls[16] = new Ball(new Image("/yellowBall.png"), 11.5, new Vector2(1280, 333), 3);
        balls[17] = new Ball(new Image("/brownBall.png"), 11.5, new Vector2(1280, 400), 4);
        balls[18] = new Ball(new Image("/blueBall.png"), 11.5, new Vector2(800, 400), 5);
        balls[19] = new Ball(new Image("/pinkBall.png"), 11.5, new Vector2(400, 400), 6);
        balls[20] = new Ball(new Image("/blackBall.png"), 11.5, new Vector2(146, 400), 7);

        balls[21] = new Ball(new Image("/cueBall.png"), 11.5, new Vector2(1350, 360), 0);

        for (Ball ball : balls) {
            if (ball == null) continue;

            if(ball.image != null) continue;
            ball.setImage(new Image("/ball2.png"));
        }

        inputHandler = new InputHandler(scene);

        aimLine = new Line();
        aimLine.setStrokeWidth(3);

        pane.getChildren().add(aimLine);
    }

    public void update(double deltaTime, Scene scene, Pane pane) {
        boolean areAllBallsStanding = true;
        // loop through every ball to check wall collisions and apply movement independently
        for (Ball ball : balls) {
            if (ball == null) continue;

            // move balls. this is completely independent of visual movement (javafx image) as it could cause stutters etc
            ball.position = ball.position.sum(ball.velocity.scalar(deltaTime));

            // check for wall collisions and invert velocity accordingly with a bit of energy loss (returnEnergy)
            calculateWallCollisions(ball, scene);

            // apply friction
            double friction = .45;
            ball.velocity = ball.velocity.scalar(Math.pow(friction, deltaTime));

            // check each balls velocity. if its magnitude is over .5 the player should not be able to shoot the que ball.
            if(ball.velocity.magnitude() >= 1)
            {
                areAllBallsStanding = false;
            }
        }

        if(areAllBallsStanding) shootCueBall(); // the movement function is only called if the previous check detected no moving balls

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

        updateAimLine(areAllBallsStanding);
    }

    private void updateAimLine(boolean visible){

        aimLine.setVisible(visible);

        if(!visible) return;

        Vector2 mouse = inputHandler.getMousePosition();
        Vector2 cueBallPos = balls[21].position;

        Vector2 drag = mouse.difference(cueBallPos);
        Vector2 aim = drag.scalar(-1);

        double power = drag.magnitude() * 2;

        Vector2 aimEnd = cueBallPos.sum(aim.normalize().scalar(power));

        aimLine.setStartX(cueBallPos.x);
        aimLine.setStartY(cueBallPos.y);

        aimLine.setEndX(aimEnd.x);
        aimLine.setEndY(aimEnd.y);
    }

    public void handleCollisions() {
        // here the collisions are calculated which are checked for every ball combination, hence the for loops.
        for (int i = 0; i < balls.length; i++) {
            if (balls[i] == null) continue;

            // the second loop only starts at i as all balls before that have already been checked and would waste processing power
            for (int j = i + 1; j < balls.length; j++) {
                if (balls[j] == null) continue;

                double dist = balls[i].getPosition().distance(balls[j].getPosition());
                double minDist = balls[i].radius + balls[j].radius;

                // checks if collision has happened and applies counter forces accordingly
                if (dist < minDist) {
                    Vector2 diff = balls[i].getPosition().difference(balls[j].getPosition());
                    Vector2 normal = diff.normalize();
                    Vector2 relVel = balls[i].velocity.difference(balls[j].velocity);

                    double speedAlongNormal = relVel.dot(normal);

                    // make sure collisions are only resolved if balls are approaching each other
                    if (speedAlongNormal < 0) {
                        double e = .95;
                        double jImpulse = (-(1 + e) * speedAlongNormal) / ((1 / balls[i].mass) + (1 / balls[j].mass));

                        Vector2 impulseVec = normal.scalar(jImpulse);

                        balls[i].velocity = balls[i].velocity.sum(impulseVec.scalar(1 / balls[i].mass));
                        balls[j].velocity = balls[j].velocity.difference(impulseVec.scalar(1 / balls[j].mass));

                        // corrects overlap
                        double overlap = minDist - dist;
                        Vector2 correction = normal.scalar(overlap / 2.0);
                        balls[i].setPosition(balls[i].getPosition().sum(correction));
                        balls[j].setPosition(balls[j].getPosition().difference(correction));
                    }
                }
            }
        }
    }

    public void shootCueBall(){

        boolean isCurrentlyPressed = inputHandler.isPressedMouse(MouseButton.PRIMARY);

        // if the primary button is pressed and wasn't pressed in the frame before, set the startingPoint
        if(isCurrentlyPressed && !mouseWasPressed){
            startingPoint = inputHandler.getMousePosition();
        }

        // if the primary button isn't pressed but was in the last frame, set the endPoint and apply a force to the cue ball
        if(!isCurrentlyPressed && mouseWasPressed){
            endPoint = inputHandler.getMousePosition();
            balls[21].velocity = endPoint.difference(startingPoint).scalar(-7.5);
        }

        mouseWasPressed = isCurrentlyPressed;
    }

    public Ball[] getBalls(){
        return balls;
    }

    public void calculateWallCollisions(Ball ball, Scene scene){
        double returnEnergy = .87;
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
    }
}