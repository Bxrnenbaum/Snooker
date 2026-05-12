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

    public InputHandler(Scene scene) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED, e -> activeKeys.add(e.getCode()));
        scene.addEventHandler(KeyEvent.KEY_RELEASED, e -> activeKeys.remove(e.getCode()));

        scene.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> activeMouseButtons.add(e.getButton()));
        scene.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> activeMouseButtons.remove(e.getButton()));

        scene.addEventHandler(MouseEvent.MOUSE_MOVED, e -> {
            mouseX = e.getSceneX();
            mouseY = e.getSceneY();
        });

        scene.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            mouseX = e.getSceneX();
            mouseY = e.getSceneY();
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