/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fndidefx;

import fndidefx.model.Simbolo;
import fndidefx.model.Token;
import static fndidefx.model.Token.TokenType.*;
import fndidefx.model.Var;
import java.util.Scanner;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author fnd
 */
public class FndIdeFx extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("Window.fxml"));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("fnd-keywords.css").toExternalForm());
        scene.getStylesheets().add("fndidefx/style.css");

        stage.setTitle("Fnd IDE 1.2");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        launch(args);
        //System.out.println(BEGIN.name());
        Simbolo s = new Var("id", "tk", "valor", 0);
        if (s instanceof Var) { // sem o if da erro
            System.out.println("Utilizada = " + ((Var) s).getUtilizada());
        }
        System.exit(0);
    }

    /*
    Scanner sc = new Scanner(System.in);
        String in = sc.next();
        while (!in.equals("exit")) {
            System.out.println(in.matches("[a-zA-Z](\\d|[a-zA-Z])*"));
            in = sc.next();
        }
    
     */
}
