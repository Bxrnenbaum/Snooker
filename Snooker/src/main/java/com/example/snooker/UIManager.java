package com.example.snooker;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Insets;


public class UIManager extends Application {

    private int width;
    private int height;

    private long lastUpdate = 0;

    private Stage primaryStage;
    private AnimationTimer gameLoop;

    private StackPane gameRoot;
    private Pane gamePane;
    private VBox pauseMenu;

    private boolean paused = false;

    private final ConfigReader config = new ConfigReader();



    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        loadWindowSize();


        primaryStage.setTitle("Snooker Game");
        primaryStage.setResizable(false);
        primaryStage.setScene(createStartScene());
        primaryStage.show();
    }

    private void loadWindowSize() {
        config.load();

        width = config.getInt("width", 1600);

        if (width < 500) {
            width = 500;
        }

        if (width > 8000) {
            width = 8000;
        }

        height = (int) (width * 0.5);
    }

    private Scene createStartScene() {
        loadWindowSize();

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);

        Button startButton = new Button("Start Game");
        Button optionsButton = new Button("Options");
        Button quitButton = new Button("Quit");
        Button tutorialButton = new Button("Tutorial");



        startButton.setOnAction(e -> startGame());
        optionsButton.setOnAction(e -> primaryStage.setScene(createOptionsScene()));
        quitButton.setOnAction(e -> primaryStage.close());
        tutorialButton.setOnAction(e -> primaryStage.setScene(createTutorialScene()));

        root.getChildren().addAll(
                tutorialButton,
                startButton,
                optionsButton,
                quitButton

        );

        return new Scene(root, width, height);
    }

    private Scene createOptionsScene() {
        loadWindowSize();

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setFillWidth(false);

        int savedSubsteps = config.getInt("substeps", 8);

        Label substepsLabel = new Label("Substeps: " + savedSubsteps);

        Slider substepsSlider = new Slider(1, 20, savedSubsteps);
        substepsSlider.setShowTickLabels(true);
        substepsSlider.setShowTickMarks(true);
        substepsSlider.setMajorTickUnit(1);
        substepsSlider.setMinorTickCount(0);
        substepsSlider.setBlockIncrement(1);
        substepsSlider.setSnapToTicks(true);

        substepsSlider.setMinWidth(150);
        substepsSlider.setPrefWidth(150);
        substepsSlider.setMaxWidth(150);

        substepsSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int substeps = (int) Math.round(newValue.doubleValue());

            substepsSlider.setValue(substeps);
            substepsLabel.setText("Substeps: " + substeps);

            config.setInt("substeps", substeps);
            config.save();

            System.out.println("Saved substeps: " + substeps);
        });

        Label widthLabel = new Label("Window Width:");

        TextField widthInput = new TextField(String.valueOf(width));
        widthInput.setMaxWidth(150);

        Button saveWidthButton = new Button("Save Width");

        saveWidthButton.setOnAction(e -> {
            try {
                int newWidth = Integer.parseInt(widthInput.getText());

                config.setInt("width", newWidth);
                config.save();

                width = newWidth;
                height = (int) (width * 0.5);

                primaryStage.setScene(createOptionsScene());

                System.out.println("Saved width: " + width);
            } catch (NumberFormatException ex) {
                widthInput.setText(String.valueOf(width));
                System.out.println("Invalid width input");
            }
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> primaryStage.setScene(createStartScene()));

        root.getChildren().addAll(
                substepsLabel,
                substepsSlider,
                widthLabel,
                widthInput,
                saveWidthButton,
                backButton
        );

        return new Scene(root, width, height);
    }

    private void startGame() {
        stopGameLoop();

        loadWindowSize();

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

        Scene scene = new Scene(gameRoot, width, height);

        GameLogic gameLogic = new GameLogic(width, height, scene, gamePane);
        gameLogic.onStart();

        for (Ball ball : gameLogic.getBalls()) {
            if (ball == null) {
                continue;
            }

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
                if (paused) {
                    return;
                }

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

        pauseMenu.getChildren().addAll(
                resumeButton,
                mainMenuButton
        );

        gameRoot.getChildren().add(pauseMenu);
    }
    private Scene createTutorialScene() {

        VBox content = new VBox(15);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(20));

        Label title = new Label("How to Play Snooker");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label tutorialText = new Label("""
            CONTROLS

            • Hold LEFT MOUSE BUTTON and drag.
            • Release to strike the cue ball.
            • ESC opens the pause menu.

            OBJECTIVE

            • Pot a red ball first.
            • After a red, pot a coloured ball.
            • Alternate between reds and colours.

            BALL VALUES

            Red = 1 point
            Yellow = 2 points
            Green = 3 points
            Brown = 4 points
            Blue = 5 points
            Pink = 6 points
            Black = 7 points

            BASIC RULES

            • Potting the wrong ball is a foul.
            • If the cue ball enters a pocket, it is a foul.
            • The player with the highest score wins.

            GAMEPLAY TIPS

            • Pull further back for more power.
            • Use cushion bounces to reach difficult shots.
            • Black and pink balls give the most points.
            """);

        tutorialText.setWrapText(true);

        Button backButton = new Button("Back");
        backButton.setOnAction(e ->
                primaryStage.setScene(createStartScene())
        );

        content.getChildren().addAll(
                title,
                tutorialText,
                backButton
        );

        ScrollPane scrollPane = new ScrollPane(content);

        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        Platform.runLater(content::requestFocus);
        return new Scene(scrollPane, width, height);
    }

    private void togglePauseMenu() {
        paused = !paused;
        pauseMenu.setVisible(paused);
        lastUpdate = 0;
    }

    private void goToMainMenu() {
        stopGameLoop();

        paused = false;
        gameRoot = null;
        gamePane = null;
        pauseMenu = null;

        primaryStage.setScene(createStartScene());
    }

    private void stopGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
            gameLoop = null;
        }
    }
}