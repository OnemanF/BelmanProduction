module dk.easv.mytunes.belman {
    requires javafx.controls;
    requires javafx.fxml;


    opens dk.easv.mytunes.belman to javafx.fxml;
    exports dk.easv.mytunes.belman;
}