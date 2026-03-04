module com.example.snooker {
        requires javafx.controls;
        requires javafx.fxml;

        opens com.example.snooker to javafx.graphics, javafx.fxml;
        exports com.example.snooker;
}