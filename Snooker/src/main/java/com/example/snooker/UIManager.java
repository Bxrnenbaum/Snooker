package com.example.snooker;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

public class UIManager extends Application {

    private static final int WIDTH = 900;
    private static final int HEIGHT = (int) (WIDTH * 0.5);

    private long lastUpdate = 0;

    private Stage primaryStage;
    private AnimationTimer gameLoop;
    private StackPane gameRoot;
    private Pane gamePane;
    private VBox pauseMenu;

    private boolean paused = false;
    private final ConfigReader config = new ConfigReader("src/main/resources/config.properties");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        primaryStage.setTitle("Snooker Game");
        primaryStage.setResizable(false);
        primaryStage.setScene(createStartScene());
        primaryStage.show();
    }

    private Scene createStartScene() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);

        Button startButton = new Button("Start Game");
        Button optionsButton = new Button("Options");
        Button quitButton = new Button("Quit");

        startButton.setOnAction(e -> startGame());
        optionsButton.setOnAction(e -> primaryStage.setScene(createOptionsScene()));
        quitButton.setOnAction(e -> primaryStage.close());

        root.getChildren().addAll(startButton, optionsButton, quitButton);

        return new Scene(root, WIDTH, HEIGHT);
    }

    private Scene createOptionsScene() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setFillWidth(false);

        int savedSubsteps = config.getInt("substeps", 10);

        Label substepsLabel = new Label("Substeps: " + savedSubsteps);

        Slider substepsSlider = new Slider(1, 10, savedSubsteps);
        substepsSlider.setShowTickLabels(true);
        substepsSlider.setShowTickMarks(true);
        substepsSlider.setMajorTickUnit(1);
        substepsSlider.setMinorTickCount(0);
        substepsSlider.setBlockIncrement(1);
        substepsSlider.setSnapToTicks(true);
        substepsSlider.setPrefWidth(150);

        substepsSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int substeps = (int) Math.round(newValue.doubleValue());

            substepsSlider.setValue(substeps);
            substepsLabel.setText("Substeps: " + substeps);

            config.setInt("substeps", substeps);
            config.save();

            System.out.println("Saved substeps: " + substeps);
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> primaryStage.setScene(createStartScene()));

        root.getChildren().addAll(
                substepsLabel,
                substepsSlider,
                backButton
        );

        return new Scene(root, WIDTH, HEIGHT);
    }

    private void startGame() {
        stopGameLoop();

        paused = false;
        lastUpdate = 0;

        gameRoot = new StackPane();
        gamePane = new Pane();

        Image backgroundImage = new Image("/table.png");
        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                new BackgroundSize(1.0, 1.0, true, true, false, false)
        );

        gamePane.setBackground(new Background(background));
        gameRoot.getChildren().add(gamePane);

        Scene scene = new Scene(gameRoot, WIDTH, HEIGHT);

        GameLogic gameLogic = new GameLogic(WIDTH, HEIGHT, scene, gamePane);
        gameLogic.onStart();

        for (Ball ball : gameLogic.getBalls()) {
            if (ball == null) continue;
            gamePane.getChildren().add(ball.imageView);
        }

        createPauseMenu();

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                togglePauseMenu();
            }
        });

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (paused) return;

                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }

                double deltaTime = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;

                gameLogic.update(deltaTime);
            }
        };

        gameLoop.start();

        primaryStage.setScene(scene);
    }

    private void createPauseMenu() {
        pauseMenu = new VBox(20);
        pauseMenu.setAlignment(Pos.CENTER);
        pauseMenu.setVisible(false);

        pauseMenu.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 0.7);" +
                        "-fx-padding: 50;"
        );

        Button resumeButton = new Button("Resume");
        Button mainMenuButton = new Button("Main Menu");

        resumeButton.setOnAction(e -> togglePauseMenu());
        mainMenuButton.setOnAction(e -> goToMainMenu());

        pauseMenu.getChildren().addAll(resumeButton, mainMenuButton);
        gameRoot.getChildren().add(pauseMenu);
    }

    private void togglePauseMenu() {
        paused = !paused;
        pauseMenu.setVisible(paused);
        lastUpdate = 0;
    }

    private void goToMainMenu() {
        stopGameLoop();
        paused = false;
        primaryStage.setScene(createStartScene());
    }

    private void stopGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
            gameLoop = null;
        }
    }
}