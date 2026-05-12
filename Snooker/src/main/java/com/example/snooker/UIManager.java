package com.example.snooker;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class UIManager extends Application {

    private static final int WINDOW_WIDTH = 1600;
    private static final int WINDOW_HEIGHT = 800;

    private long lastUpdate = 0;

    private Stage primaryStage;
    private AnimationTimer gameLoop;
    private GameLogic gameLogic;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        this.primaryStage.setTitle("Snooker Game");
        this.primaryStage.setResizable(false);
        this.primaryStage.setScene(createStartScene());
        this.primaryStage.show();
    }

    private Scene createStartScene() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);

        Button startButton = new Button("Start");
        Button optionsButton = new Button("Options");
        Button quitButton = new Button("Quit");

        startButton.setOnAction(e -> switchToGameScene());
        optionsButton.setOnAction(e -> switchToOptionsScene());
        quitButton.setOnAction(e -> primaryStage.close());

        root.getChildren().addAll(
                startButton,
                optionsButton,
                quitButton
        );

        return new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    private void switchToOptionsScene() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);

        Button backButton = new Button("Back");

        // Later you can add volume sliders, difficulty buttons, controls, etc. here.

        backButton.setOnAction(e -> primaryStage.setScene(createStartScene()));

        root.getChildren().add(backButton);

        Scene optionsScene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setScene(optionsScene);
    }

    private void switchToGameScene() {
        Pane root = new Pane();

        Image backgroundImage = new Image("/table.png");

        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                new BackgroundSize(
                        1.0,
                        1.0,
                        true,
                        true,
                        false,
                        false
                )
        );

        root.setBackground(new Background(background));

        Scene gameScene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        gameLogic = new GameLogic();
        gameLogic.onStart(gameScene, root);

        addBallsToRoot(root);
        startGameLoop(gameScene, root);

        primaryStage.setScene(gameScene);
    }

    private void addBallsToRoot(Pane root) {
        for (Ball ball : gameLogic.getBalls()) {
            if (ball == null) {
                continue;
            }

            root.getChildren().add(ball.imageView);
        }
    }

    private void startGameLoop(Scene gameScene, Pane root) {
        lastUpdate = 0;

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }

                double deltaTime = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;

                gameLogic.update(deltaTime, gameScene, root);
            }
        };

        gameLoop.start();
    }
}