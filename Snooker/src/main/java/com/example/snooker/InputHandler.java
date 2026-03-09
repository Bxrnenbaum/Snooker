package com.example.snooker;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

import java.util.HashSet;
import java.util.Set;

public class InputHandler
{
    private final Set<KeyCode> activeKeys = new HashSet<>();
    private final Set<MouseButton> activeMouseButtons = new HashSet<>();

    private double mouseX;
    private double mouseY;

    public InputHandler(Scene scene) {
        scene.setOnKeyPressed(e -> activeKeys.add(e.getCode()));
        scene.setOnKeyReleased(e -> activeKeys.remove(e.getCode()));

        scene.setOnMousePressed(e -> activeMouseButtons.add(e.getButton()));
        scene.setOnMouseReleased(e -> activeMouseButtons.remove(e.getButton()));


        scene.setOnMouseMoved(e -> {
           mouseX = e.getSceneX();
           mouseY = e.getSceneY();
        });

        scene.setOnMouseDragged(e -> {
            mouseX = e.getSceneX();
            mouseY = e.getSceneY();
        });
    }

    public boolean isPressed(KeyCode code){
        return activeKeys.contains(code);
    }

    public boolean isPressedMouse(MouseButton button){
        return activeMouseButtons.contains(button);
    }

    public Vector2 getMousePosition(){
        return new Vector2(mouseX, mouseY);
    }
}
