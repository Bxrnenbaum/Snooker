package com.example.snooker;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class UIManager extends Application
{

    private long lastUpdate = 0;
    private GameLogic gameLogic = new GameLogic();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        //Setup scene
        Pane root = new Pane();

        Image backgroundImage = new Image("/table.png");
        BackgroundImage background = new BackgroundImage(backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                new BackgroundSize(1.0, 1.0, true, true, false, false));

        root.setBackground(new Background(background));



        Scene scene = new Scene(root, 1600, 800);

        gameLogic.onStart(scene);

        for (Ball ball : gameLogic.getBalls()) {
            if (ball == null) continue;
            root.getChildren().add(ball.imageView);
        }

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
                gameLogic.update(deltaTime, scene);
            }
        }.start();

        primaryStage.setTitle("Snooker Game");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}
