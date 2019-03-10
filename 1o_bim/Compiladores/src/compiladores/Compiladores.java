package compiladores;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Compiladores extends Application {
    
    @Override
    public void start(Stage stage) throws Exception
    {
        Parent root = FXMLLoader.load(getClass().getResource("Console.fxml"));        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setTitle("Compiladores");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("icones/icon.png")));
        stage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}
