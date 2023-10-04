module com.example.crosswordsample {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.crosswordsample to javafx.fxml;
    exports com.example.crosswordsample;
}