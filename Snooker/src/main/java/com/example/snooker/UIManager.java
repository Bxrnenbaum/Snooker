package com.example.snooker;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
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
        javafx.scene.text.Font customFont = javafx.scene.text.Font.loadFont(
                getClass().getResourceAsStream("/fonts/Baskervville-Regular.ttf"), 16
        );

        if (customFont != null) {
            System.out.println("Loaded font: " + customFont.getFamily());
        } else {
            System.err.println("Font couldnt be loaded");
        }

        this.primaryStage = primaryStage;
        loadWindowSize();
        primaryStage.setTitle("Snooker");
        primaryStage.setResizable(false);
        primaryStage.setScene(createStartScene());
        primaryStage.show();
    }

    private void loadWindowSize() {
        config.load();
        width = config.getInt("width", 1600);
        if (width < 500) width = 500;
        if (width > 8000) width = 8000;
        height = (int) (width * 0.5);
    }

    private Scene styledScene(javafx.scene.Parent root) {
        Scene scene = new Scene(root, width, height);
        String css;
        if (width >= 600 && width < 1000) {
            css = getClass().getResource("/styles-compact.css").toExternalForm();
        } else {
            css = getClass().getResource("/styles.css").toExternalForm();
        }
        scene.getStylesheets().add(css);
        return scene;
    }

    private Scene createStartScene() {
        loadWindowSize();


        VBox root = new VBox(width < 1000 ? 7 : 12);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #4a1a0e;");

        Button startButton = new Button("Start Game");
        Button tutorialButton = new Button("Tutorial");
        Button optionsButton = new Button("Options");
        Button quitButton = new Button("Quit");


        int btnWidth = width < 1000 ? 140 : 220;
        for (Button b : new Button[]{startButton, tutorialButton, optionsButton, quitButton}) {
            b.setMaxWidth(btnWidth);
            b.setPrefWidth(btnWidth);
        }

        startButton.setOnAction(e -> startGame());
        optionsButton.setOnAction(e -> primaryStage.setScene(createOptionsScene()));
        quitButton.setOnAction(e -> primaryStage.close());
        tutorialButton.setOnAction(e -> primaryStage.setScene(createTutorialScene()));

        root.getChildren().addAll(startButton, tutorialButton, optionsButton, quitButton);

        return styledScene(root);
    }

    private Scene createOptionsScene() {
        loadWindowSize();

        // Outer brown background
        BorderPane outer = new BorderPane();
        outer.setStyle("-fx-background-color: #4a1a0e; -fx-padding: " + (width < 1000 ? "12" : "40") + ";");
        // Title above card
        Label title = new Label("Options");
        title.setId("optionsTitle");
        BorderPane.setMargin(title, new Insets(0, 0, 16, 0));
        outer.setTop(title);

        // Green card
        VBox card = new VBox(width < 1000 ? 6 : 16);
        card.setId("optionsCard");
        card.setFillWidth(true);
        card.setMaxWidth(560);
        BorderPane.setAlignment(card, Pos.CENTER);

        // Substeps
        int savedSubsteps = config.getInt("substeps", 8);
        Label substepsLabel = new Label("Substeps: " + savedSubsteps);

        Slider substepsSlider = new Slider(1, 20, savedSubsteps);

        substepsSlider.setShowTickMarks(true);
        substepsSlider.setMajorTickUnit(1);
        substepsSlider.setMinorTickCount(0);
        substepsSlider.setBlockIncrement(1);
        substepsSlider.setSnapToTicks(true);
        substepsSlider.setMaxWidth(Double.MAX_VALUE);

        substepsSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int substeps = (int) Math.round(newVal.doubleValue());
            substepsSlider.setValue(substeps);
            substepsLabel.setText("Substeps: " + substeps);
            config.setInt("substeps", substeps);
            config.save();
        });

        // Width field
        Label widthLabel = new Label("Window Width:");
        TextField widthInput = new TextField(String.valueOf(width));
        widthInput.setMaxWidth(Double.MAX_VALUE);

        // Checkbox
        CheckBox debugCheckBox = new CheckBox("Show debug lines");
        debugCheckBox.setSelected(config.getBoolean("showDebugLines", false));
        debugCheckBox.setOnAction(e -> {
            config.setBoolean("showDebugLines", debugCheckBox.isSelected());
            config.save();
        });

        // Buttons row
        Button saveWidthButton = new Button("Save Width");
        Button backButton = new Button("Back");
        saveWidthButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(saveWidthButton, Priority.ALWAYS);
        HBox.setHgrow(backButton, Priority.ALWAYS);
        HBox buttonRow = new HBox(12, saveWidthButton, backButton);
        buttonRow.setFillHeight(true);

        saveWidthButton.setOnAction(e -> {
            try {
                int newWidth = Integer.parseInt(widthInput.getText());
                config.setInt("width", newWidth);
                config.save();
                width = newWidth;
                height = (int) (width * 0.5);
                primaryStage.setScene(createOptionsScene());
            } catch (NumberFormatException ex) {
                widthInput.setText(String.valueOf(width));
            }
        });

        backButton.setOnAction(e -> primaryStage.setScene(createStartScene()));

        card.getChildren().addAll(
                substepsLabel, substepsSlider,
                widthLabel, widthInput,
                debugCheckBox,
                buttonRow
        );

        outer.setCenter(card);

        return styledScene(outer);
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

        Scene scene = styledScene(gameRoot);

        GameLogic gameLogic = new GameLogic(width, height, scene, gamePane);
        gameLogic.onStart();

        for (Ball ball : gameLogic.getBalls()) {
            if (ball == null) continue;
            gamePane.getChildren().add(ball.imageView);
        }

        createPauseMenu();

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) togglePauseMenu();
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
        pauseMenu.setId("pauseMenu");
        pauseMenu.setAlignment(Pos.CENTER);
        pauseMenu.setVisible(false);
        pauseMenu.setStyle("-fx-background-color: #00000055;");

        Button resumeButton = new Button("Resume");
        Button mainMenuButton = new Button("Main Menu");
        resumeButton.setPrefWidth(240);
        mainMenuButton.setPrefWidth(240);

        resumeButton.setOnAction(e -> togglePauseMenu());
        mainMenuButton.setOnAction(e -> goToMainMenu());

        pauseMenu.getChildren().addAll(resumeButton, mainMenuButton);
        gameRoot.getChildren().add(pauseMenu);
    }

    private Scene createTutorialScene() {
        VBox content = new VBox(15);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #2e7a45;");

        Label title = new Label("How to Play Snooker");
        title.setId("tutorialTitle");

        Label tutorialText = getLabel();

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> primaryStage.setScene(createStartScene()));

        content.getChildren().addAll(title, tutorialText, backButton);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        Platform.runLater(content::requestFocus);

        return styledScene(scrollPane);
    }

    private static Label getLabel() {
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
                
                GAMEPLAY TIPS
                
                • Pull further back for more power.
                • Use cushion bounces to reach difficult shots.
                • Black and pink balls give the most points.
                """);
        tutorialText.setWrapText(true);
        return tutorialText;
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