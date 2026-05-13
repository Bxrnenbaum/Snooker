package com.example.snooker;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class ConfigReader {

    private final Properties properties = new Properties();
    private final String filePath;

    public ConfigReader(String filePath) {
        this.filePath = filePath;
        load();
    }

    public void load() {
        try (FileInputStream input = new FileInputStream(filePath)) {
            properties.load(input);
        } catch (Exception e) {
            System.out.println("Could not load config file: " + e.getMessage());
        }
    }

    public void save() {
        try (FileOutputStream output = new FileOutputStream(filePath)) {
            properties.store(output, "Snooker Config");
        } catch (Exception e) {
            System.out.println("Could not save config file: " + e.getMessage());
        }
    }

    public String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);

        if (value == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public double getDouble(String key, double defaultValue) {
        String value = properties.getProperty(key);

        if (value == null) {
            return defaultValue;
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);

        if (value == null) {
            return defaultValue;
        }

        return Boolean.parseBoolean(value);
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