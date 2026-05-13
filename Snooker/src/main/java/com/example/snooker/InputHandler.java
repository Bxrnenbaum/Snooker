package com.example.snooker;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.HashSet;
import java.util.Set;

public class InputHandler {

    private final Set<KeyCode> activeKeys = new HashSet<>();
    private final Set<MouseButton> activeMouseButtons = new HashSet<>();

    private double mouseX;
    private double mouseY;

    private double scale;
    private double offsetX;
    private double offsetY;

    public InputHandler(Scene scene, double scale, double offsetX, double offsetY) {
        this.scale = scale;
        this.offsetX = offsetX;
        this.offsetY = offsetY;

        scene.addEventHandler(KeyEvent.KEY_PRESSED, e -> activeKeys.add(e.getCode()));
        scene.addEventHandler(KeyEvent.KEY_RELEASED, e -> activeKeys.remove(e.getCode()));

        scene.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> activeMouseButtons.add(e.getButton()));
        scene.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> activeMouseButtons.remove(e.getButton()));

        scene.addEventHandler(MouseEvent.MOUSE_MOVED, e -> {
            mouseX = (e.getSceneX() - offsetX) / scale;
            mouseY = (e.getSceneY() - offsetY) / scale;
        });

        scene.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            mouseX = (e.getSceneX() - offsetX) / scale;
            mouseY = (e.getSceneY() - offsetY) / scale;
        });
    }


    public boolean isPressed(KeyCode code) {
        return activeKeys.contains(code);
    }

    public boolean isPressedMouse(MouseButton button) {
        return activeMouseButtons.contains(button);
    }

    public Vector2 getMousePosition() {
        return new Vector2(mouseX, mouseY);
    }
}