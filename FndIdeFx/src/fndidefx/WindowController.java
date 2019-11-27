/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fndidefx;

import com.jfoenix.controls.JFXButton;
import fndidefx.model.AnaliseLexica;
import fndidefx.model.AnaliseSintatica;
import fndidefx.model.Erro;
import fndidefx.model.Simbolo;
import fndidefx.model.TabelaSimbolos;
import fndidefx.model.Token;
import java.io.File;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.EventHandler;

import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;

/**
 *
 * @author fnd
 */
public class WindowController implements Initializable {

    private AnaliseSintatica anasin;
    private AnaliseLexica analex;
    private boolean codechanged;
    private File file;
    private String dialogResult;
    private CodeArea codearea;
    private StackPane stackpane;
    private VirtualizedScrollPane virtscrollpane;

    private TabPane tabpane;
    @FXML
    private Button btcompile;
    @FXML
    private Button btnew;
    @FXML
    private Button btsave;
    @FXML
    private JFXButton btopen;
    @FXML
    private Tab tabcode;
    @FXML
    private TextArea txLog;
    @FXML
    private MenuItem itopen;
    @FXML
    private MenuItem itnew;
    @FXML
    private MenuItem itsave;
    @FXML
    private MenuItem itsaveas;
    @FXML
    private MenuItem itcompile;
    @FXML
    private TableColumn<Simbolo, String> colLexema;
    @FXML
    private TableColumn<Simbolo, String> colToken;
    @FXML
    private TableColumn<Simbolo, String> colLinha;
    @FXML
    private TableColumn<Simbolo, String> colValor;
    @FXML
    private TableView<Simbolo> tbvsimbolos;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initializeCodeArea();
        disable(true);
        colLexema.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getId()));
        colToken.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getToken()));
        colLinha.setCellValueFactory(c -> new SimpleStringProperty((c.getValue().getLinha()) + ""));
        colValor.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getValor()));

        colLinha.setComparator((String o1, String o2) -> Integer.parseInt(o1) - Integer.parseInt(o2));

        startEventClose();
    }

    private void startEventClose() {
        new Thread(() -> {
            try {
                Thread.sleep(2000); // 2 segundos
            } catch (InterruptedException ex) {
                Logger.getLogger(WindowController.class.getName()).log(Level.SEVERE, null, ex);
            }
            ((Stage) btnew.getScene().getWindow()).setOnCloseRequest(closeWindow());
        }).start();
    }

    private void disable(boolean v) {
        codearea.setDisable(v);
        btcompile.setDisable(v);
        btsave.setDisable(v);
    }

    private void initializeCodeArea() {
        codearea = new CodeArea();
        // add line numbers to the left of area
        codearea.setParagraphGraphicFactory(LineNumberFactory.get(codearea));

        // recompute the syntax highlighting 50 ms after user stops editing area
        Subscription cleanupWhenNoLongerNeedIt = codearea
                // plain changes = ignore style changes that are emitted when syntax highlighting is reapplied
                // multi plain changes = save computation by not rerunning the code multiple times
                //   when making multiple changes (e.g. renaming a method at multiple parts in file)
                .multiPlainChanges()
                // do not emit an event until 50 ms have passed since the last emission of previous stream
                .successionEnds(Duration.ofMillis(50))
                // run the following code block when previous stream emits an event
                .subscribe(ignore -> codearea.setStyleSpans(0, computeHighlighting(codearea.getText())));

        // when no longer need syntax highlighting and wish to clean up memory leaks
        // run: `cleanupWhenNoLongerNeedIt.unsubscribe();`
        virtscrollpane = new VirtualizedScrollPane(codearea);
        stackpane = new StackPane(virtscrollpane);
        codearea.setOnKeyTyped(codeAreaKeyTyped());
        tabcode.setContent(stackpane);
    }

    private EventHandler<KeyEvent> codeAreaKeyTyped() {
        return (KeyEvent event) -> {

            if (event.getCharacter().equals("\t")) {
                codearea.replaceText(codearea.getCaretPosition() - 1, codearea.getCaretPosition(), "    ");
            }
            if (!(event.isControlDown())) {
                markChange();
            }
            //System.out.println(event.getCharacter()+", "+event.getCode());
        };
    }

    private static final String[] KEYWORDS = {"begin", "end"},
            KEYTYPES = {"int", "double", "exp"},
            KEYCOMMAND = {"if", "else", "while", "for"};

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String TYPES_PATTERN = "\\b(" + String.join("|", KEYTYPES) + ")\\b";
    private static final String COMMAND_PATTERN = "\\b(" + String.join("|", KEYCOMMAND) + ")\\b";
    private static final String NUMBER_PATTERN = "\\d";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/" + "|" + "/\\*(.|\\R)*?$";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ')' // #c586c0
            + "|(?<TYPE>" + TYPES_PATTERN + ')' // #569cd6
            + "|(?<COMMAND>" + COMMAND_PATTERN + ')' // #
            + "|(?<PAREN>" + PAREN_PATTERN + ')'
            + "|(?<BRACE>" + BRACE_PATTERN + ')'
            + "|(?<BRACKET>" + BRACKET_PATTERN + ')'
            + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ')'
            + "|(?<COMMENT>" + COMMENT_PATTERN + ')'
            + "|(?<NUMBER>" + NUMBER_PATTERN + ')'
    );

    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass
                    = matcher.group("KEYWORD") != null ? "keyword"
                    : matcher.group("TYPE") != null ? "type"
                    : matcher.group("COMMAND") != null ? "command"
                    : matcher.group("NUMBER") != null ? "number"
                    : matcher.group("PAREN") != null ? "paren"
                    : matcher.group("BRACE") != null ? "brace"
                    : matcher.group("BRACKET") != null ? "bracket"
                    : matcher.group("SEMICOLON") != null ? "semicolon"
                    : matcher.group("COMMENT") != null ? "comment"
                    : null;

            //System.out.println(styleClass+", start:"+matcher.start()+", lastKwEnd:"+lastKwEnd+", end:"+matcher.end());
            //* never happens */ assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    @FXML
    private void clkOpen(ActionEvent event) {
        saveChanges();
        FileChooser fc = new FileChooser();
        fc.setTitle("Open Fnd File");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text File", "*.fnd"));
        file = fc.showOpenDialog(null);
        if (file != null) {
            open(file);
            disable(false);
        }
    }

    private boolean open(File file) {
        String out;
        try {

            tabcode.setText(file.getName());
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            codearea.setDisable(false);
            byte[] all = new byte[(int) raf.length()];
            raf.read(all);
            out = new String(all);
            raf.close();
            codearea.replaceText(out + "");
            codechanged = false;
            tabcode.setText(file.getName());
            codearea.requestFocus();
        } catch (Exception ex) {
            Logger.getLogger(WindowController.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    @FXML
    private void clkNew(ActionEvent event) {
        saveChanges();
        codechanged = false;
        file = null;
        tabcode.setText("Sem Título");
        codearea.replaceText("");
        disable(false);
        codearea.requestFocus();
    }

    private void lastLine() {
        if (!codearea.getText().isEmpty() && !codearea.getText().endsWith("\n")) {
            codearea.appendText("\n");
        }
    }

    @FXML
    private void clkSave(ActionEvent event) {
        if (file != null) {
            if (codechanged) {
                lastLine();
            }
            save(file, codearea.getText());
        } else {
            clkSaveAs(event);
        }
    }

    @FXML
    private void clkSaveAs(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Fnd File");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text File", "*.fnd"));
        File newfile = fc.showSaveDialog(null);
        if (newfile != null) {

            file = newfile;
            System.out.println(file.getName());
            if (!file.getName().endsWith(".fnd")) {
                file = new File(file.getPath() + ".fnd");
            }
            lastLine();
            save(file, codearea.getText());
        }

    }

    private boolean save(File file, String data) {
        boolean res = true;
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.setLength(0);
            raf.writeBytes(data);
            raf.close();
            codechanged = false;
            tabcode.setText(file.getName());
        } catch (Exception ex) {
            Logger.getLogger(WindowController.class.getName()).log(Level.SEVERE, null, ex);
            res = false;
        }
        return res;
    }

    @FXML
    private void clkExit(ActionEvent event) {
        if (codechanged) {
            saveChanges();
            if (dialogResult.equals("cancel")) {
                return;
            }
        }
        Platform.exit();
    }

    private void saveChanges() {
        if (codechanged) {
            Alert alerta = new Alert(AlertType.CONFIRMATION);
            ButtonType btsim = new ButtonType("Sim");
            ButtonType btnao = new ButtonType("Não");
            ButtonType btcancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
            alerta.setTitle("Atenção");
            alerta.setContentText("Deseja salvar o arquivo antes?");
            // INSERINDO BOTOES NO DIALOGO
            alerta.getButtonTypes().setAll(btsim, btnao, btcancelar);
            alerta.showAndWait().ifPresent(b -> { // abre a janela e trava o programa
                if (b != btcancelar) {
                    if (b == btsim) {
                        clkSave(null);
                        dialogResult = "yes";
                    } else {
                        dialogResult = "no";
                    }
                } else {
                    dialogResult = "cancel";
                }
            });
        }
    }

    @FXML
    private void clkCompile(ActionEvent event) {
        clkSave(event);
        txLog.clear();
        if (file != null) {
            /* // trecho de codigo para remover linha pintada de vermelho
            String text = codearea.getText();
            initializeCodeArea(); 
            codearea.appendText(text);*/
            tbvsimbolos.getItems().clear();
            //new Thread(() -> {

                System.out.println("compilando...");
                System.out.println("#########################################33");
                anasin = new AnaliseSintatica(codearea.getText());
                Platform.runLater(() -> {
                    double ini = System.currentTimeMillis();
                    ArrayList<Erro> le = null;
                    le = anasin.start();
                    if (le != null) {
                        for (Erro e : le) {
                            //codearea.setParagraphStyle(e.getLine(), Collections.singleton("error")); // pinta linha do erro
                            txLog.appendText(e + "\n");
                        }

                    }
                    double fim = System.currentTimeMillis();
                    txLog.appendText("Compilado em " + ((fim - ini) / 1000) + " segundos");
                });
                Platform.runLater(() -> {
                    this.tbvsimbolos.getItems().clear();
                    this.tbvsimbolos.getItems().addAll(anasin.getTabelaSimbolos().getTable());
                });
                System.out.println("COMPILADO");
            //}).start();
        }
    }

    private void markChange() {
        codechanged = true;
        String name = tabcode.getText();
        if (!name.startsWith("*")) {
            tabcode.setText('*' + name);
        }
    }

    @FXML
    private void clkCodeExample(ActionEvent event) {
        saveChanges();
        open((file = new File("codeexample.fnd")));
        disable(false);

    }

    private EventHandler<WindowEvent> closeWindow() {
        return (WindowEvent event) -> {
            event.consume();
            clkExit(null);
        };
    }

}
