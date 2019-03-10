/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fndidefx;

import fndidefx.compilador.AnaliseLexica;
import fndidefx.compilador.Token;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 *
 * @author fnd
 */
public class FndIdeFx extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("Window.fxml"));
        FadeTransition ft = new FadeTransition(Duration.millis(200), root);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("fnd-keywords.css").toExternalForm());
        scene.getStylesheets().add("fndidefx/style.css");

        //stage.initStyle(StageStyle.TRANSPARENT); // desativar barra da janela
        stage.setTitle("Fnd IDE 1.2");
        stage.setScene(scene);
        stage.show();
    }

    private static String cadeia = "int main () { int a = 0 ; return a ; } $";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //launch(args);
        Scanner sc = new Scanner(System.in);
        String str = sc.nextLine(), code = "";
        while (!str.endsWith("end")) {
            code+=str+'\n';
            str = sc.nextLine();
        }
        code+="end\n";
        
        AnaliseLexica al = new AnaliseLexica(code);
        str = al.nextLexema();
        while(!"$".equals(str)){
            System.out.println(str);
            Token tk = al.findToken(str);
            System.out.println(tk.getName() + ": "+tk.getMatch());
            str = al.nextLexema();
        }
        
        System.exit(0);
    }

    public static String nextToken() {
        String tk = "";
        int i = 0;
        while (i < cadeia.length() && cadeia.charAt(i) != ' ') {
            tk += cadeia.charAt(i++);
        }
        while (i < cadeia.length() && cadeia.charAt(i) == ' ') {
            ++i;
        }

        cadeia = cadeia.substring(i);

        return tk;
    }

}
