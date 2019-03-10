package compiladores;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.swing.JOptionPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

public class ConsoleController implements Initializable
{
    @FXML
    private CodeArea console;
    @FXML
    private TextArea erros;
    @FXML
    private Label filename;
    @FXML
    private ImageView btnSalvar;
    @FXML
    private MenuItem miSalvar;
    @FXML
    private ImageView btnNovo;
    @FXML
    private ImageView btnAbrir;
    @FXML
    private ImageView btnCompilar;
    @FXML
    private TextArea ignored;
    @FXML
    private TabPane tabs;
    @FXML
    private ImageView btnLimpar;
    
    private Gramatica gramatica;
    private boolean alter;
    private File file;
    
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        console.clear();
        console.setDisable(true);
        console.setParagraphGraphicFactory(LineNumberFactory.get(console));
        filename.setText("");
        btnSalvar.setDisable(true);
        btnSalvar.setStyle("-fx-opacity: 0.3;");
        miSalvar.setDisable(true);
        ignored.setText("Erros ignorados durante método do pânico:\n");
        tabs.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        
        Tooltip.install(btnSalvar, new Tooltip("Salvar Arquivo"));  
        Tooltip.install(btnAbrir, new Tooltip("Abrir Arquivo"));  
        Tooltip.install(btnCompilar, new Tooltip("Compilar Arquivo"));
        Tooltip.install(btnNovo, new Tooltip("Novo Arquivo"));  
        
        gramatica = new Gramatica();
        alter = false;
        file = null;
    }
    
    @FXML
    private void evtNovo(MouseEvent event)
    {
        console.setDisable(false);
        if(alter)
        {
            if(JOptionPane.showConfirmDialog(null, "Deseja salvar alterações?", "Salvar", JOptionPane.YES_NO_OPTION) == 0)
                evtSalvar(null);
        }
        
        console.clear();
        erros.clear();
        ignored.setText("Erros ignorados durante método do pânico:\n");
        
        filename.setText("");
        alter=false;
        file = null;
    }

    @FXML
    private void evtAbrir(MouseEvent event)
    {
        console.setDisable(false);
        if(alter)
        {
            if(JOptionPane.showConfirmDialog(null, "Deseja salvar alterações?", "Salvar", JOptionPane.YES_NO_OPTION) == 0)
                evtSalvar(null);
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        file = fileChooser.showOpenDialog(null);
        if(file != null)
        {
            try
            {
                filename.setText(file.getPath());
                String linha, code="";
                RandomAccessFile arquivo = new RandomAccessFile(file, "r");
                while ( (linha = arquivo.readLine()) != null )
                    code+=linha+"\n";
                console.replaceText(code);
                arquivo.close();
            }
            catch(Exception e){}
        }
    }

    @FXML
    private boolean evtSalvar(MouseEvent event)
    {
        FileChooser fileChooser = new FileChooser();
        
        try
        {
            if(file == null)
            {
                fileChooser.setTitle("Save File");
                file = fileChooser.showSaveDialog(null);
                if(file == null)
                    return false;
                filename.setText(file.getPath());
            }
            else
            {
                file.delete();
                file = new File(filename.getText());
            }
            RandomAccessFile arquivo = new RandomAccessFile(file, "rw");
            String[] linha=console.getText().split("\n");
            arquivo.seek(0);
            for(int i = 0; i < linha.length; i++)
            {
                arquivo.writeBytes(linha[i]+"\r\n");
            }
            arquivo.close();
            btnSalvar.setDisable(true);
            btnSalvar.setStyle("-fx-opacity: 0.3;");
            miSalvar.setDisable(true);
            alter=false;
        }
        catch(Exception e){
            return false;
        }
        return true;
    }

    @FXML
    private void evtCompilar(MouseEvent event)
    {
        if(evtSalvar(null));
        {
            erros.setText(gramatica.compilar(console.getText()));
            ignored.setText("Erros ignorados durante método do pânico:\n"+gramatica.getIgnored());
        }
    }

    @FXML
    private void evtTecla(KeyEvent event)
    {
        miSalvar.setDisable(false);
        btnSalvar.setDisable(false);
        btnSalvar.setStyle("-fx-opacity: 1;");
        alter = true;
    }

    @FXML
    private void evtAbout(ActionEvent event)
    {   
        try {
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("About.fxml")));
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("About");
            stage.getIcons().add(new Image(getClass().getResourceAsStream("icones/info.png")));
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(ConsoleController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void evtSalvar2(ActionEvent event) {
        evtSalvar(null);
    }

    @FXML
    private void evtNovo2(ActionEvent event) {
        evtNovo(null);
    }

    @FXML
    private void evtAbrir2(ActionEvent event) {
        evtAbrir(null);
    }

    @FXML
    private void evtCompilar2(ActionEvent event) {
        evtCompilar(null);
    }

    @FXML
    private void evtLimpar(MouseEvent event) {
        erros.clear();
        ignored.setText("Erros ignorados durante método do pânico:\n");
    }
}
