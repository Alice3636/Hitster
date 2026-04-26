package com.hitster.client.utils;

import javafx.beans.value.ChangeListener;
import javafx.scene.Parent;
import javafx.scene.Scene;

public final class ResponsiveScaler {
    private static final double DESIGN_WIDTH = 1920.0;
    private static final double BASE_FONT_SIZE = 16.0;
    private static final double MIN_FONT_SIZE = 10.0;
    private static final double MAX_FONT_SIZE = 22.0;

    private ResponsiveScaler() {}

    public static void bindToWidth(Parent root) {
        if (root == null) {
            return;
        }

        ChangeListener<Number> widthListener = (obs, oldWidth, newWidth) -> applyScale(root, newWidth.doubleValue());

        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null) {
                oldScene.widthProperty().removeListener(widthListener);
            }
            if (newScene != null) {
                newScene.widthProperty().addListener(widthListener);
                applyScale(root, newScene.getWidth());
            }
        });

        Scene scene = root.getScene();
        if (scene != null) {
            scene.widthProperty().addListener(widthListener);
            applyScale(root, scene.getWidth());
        }
    }

    private static void applyScale(Parent root, double sceneWidth) {
        double fontSize = BASE_FONT_SIZE * (sceneWidth / DESIGN_WIDTH);
        fontSize = Math.max(MIN_FONT_SIZE, Math.min(MAX_FONT_SIZE, fontSize));
        root.setStyle("-fx-font-size: " + fontSize + "px;");
    }
}
