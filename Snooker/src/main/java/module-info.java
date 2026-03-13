module com.example.snooker {
        requires javafx.controls;
        requires javafx.fxml;
    requires javafx.graphics;

    opens com.example.snooker to javafx.graphics, javafx.fxml;
        exports com.example.snooker;
}