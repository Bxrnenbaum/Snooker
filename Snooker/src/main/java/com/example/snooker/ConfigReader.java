package com.example.snooker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfigReader {

    private final Properties properties = new Properties();
    private final Path configPath;

    public ConfigReader() {
        configPath = Paths.get(
                System.getProperty("user.home"),
                ".snooker",
                "config.properties"
        );

        createIfMissing();
        load();
    }

    private void createIfMissing() {
        try {
            Files.createDirectories(configPath.getParent());

            if (!Files.exists(configPath)) {
                properties.setProperty("width", "1600");
                properties.setProperty("substeps", "8");
                save();
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create config file", e);
        }
    }

    public void load() {
        try (InputStream input = Files.newInputStream(configPath)) {
            properties.load(input);
        } catch (IOException e) {
            System.out.println("Could not load config file: " + e.getMessage());
        }
    }

    public void save() {
        try (OutputStream output = Files.newOutputStream(configPath)) {
            properties.store(output, "Snooker Config");
        } catch (IOException e) {
            System.out.println("Could not save config file: " + e.getMessage());
        }
    }

    public String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(
                    properties.getProperty(key, String.valueOf(defaultValue))
            );
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public double getDouble(String key, double defaultValue) {
        try {
            return Double.parseDouble(
                    properties.getProperty(key, String.valueOf(defaultValue))
            );
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(
                properties.getProperty(key, String.valueOf(defaultValue))
        );
    }

    public void setString(String key, String value) {
        properties.setProperty(key, value);
    }

    public void setInt(String key, int value) {
        properties.setProperty(key, String.valueOf(value));
    }

    public void setDouble(String key, double value) {
        properties.setProperty(key, String.valueOf(value));
    }

    public void setBoolean(String key, boolean value) {
        properties.setProperty(key, String.valueOf(value));
    }

    public void printAllValues() {
        for (String key : properties.stringPropertyNames()) {
            System.out.println(key + " = " + properties.getProperty(key));
        }
    }
}