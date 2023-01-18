module com.trivia.triviacracksolver {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires org.json;

    opens com.trivia.triviacracksolver to javafx.fxml;
    exports com.trivia.triviacracksolver;
}