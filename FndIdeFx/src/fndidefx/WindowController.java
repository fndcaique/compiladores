/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fndidefx;

import com.jfoenix.controls.JFXButton;
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
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.event.EventHandler;

import javafx.scene.control.Button;
import javafx.scene.control.Tab;
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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initializeCodeArea();
        Tab tb = tabcode;
        tb.setContent(stackpane);
        new Thread(() -> {
            try {
                Thread.sleep(2000); // 2 segundos
            } catch (InterruptedException ex) {
                Logger.getLogger(WindowController.class.getName()).log(Level.SEVERE, null, ex);
            }
            ((Stage) btnew.getScene().getWindow()).setOnCloseRequest(closeWindow());
        }).start();
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
        codearea.setDisable(true);
        codearea.setOnKeyTyped(codeAreaKeyTyped());
        codearea.requestFocus();
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
            KEYTYPES = {"int", "double", "doubleE", "char"},
            KEYCOMMAND = {"if", "else", "while", "do"};

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String KEYTYPES_PATTERN = "\\b(" + String.join("|", KEYTYPES) + ")\\b";
    private static final String KEYCOMMAND_PATTERN = "\\b(" + String.join("|", KEYCOMMAND) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String CHARACTER_PATTERN = "\'([^\"\\\\]|\\\\.)*\'";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ')' // #c586c0
            + "|(?<TYPE>" + KEYTYPES_PATTERN + ')' // #569cd6
            + "|(?<COMMAND>" + KEYCOMMAND_PATTERN + ')' // #
            + "|(?<PAREN>" + PAREN_PATTERN + ')'
            + "|(?<BRACE>" + BRACE_PATTERN + ')'
            + "|(?<BRACKET>" + BRACKET_PATTERN + ')'
            + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ')'
            + "|(?<CHARACTER>" + CHARACTER_PATTERN + ')'
            + "|(?<COMMENT>" + COMMENT_PATTERN + ')'
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
                    : matcher.group("CHARACTER") != null ? "character"
                    : matcher.group("COMMENT") != null ? "comment"
                    : "textsimple";

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
        codearea.setDisable(false);
        codearea.requestFocus();
    }

    private void lastLine() {
        if (!codearea.getText().isEmpty() && !codearea.getText().endsWith("\n")) {
            codearea.replaceText(codearea.getText() + "\n");
        }
    }

    @FXML
    private void clkSave(ActionEvent event) {
        if (file != null) {
            if (codechanged) {
                lastLine();
                save(file, codearea.getText());
            }
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
        System.out.println("compilando...");
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
        open(new File("codeexample.fnd"));
        tabcode.setText("Sem Título");
    }

    private EventHandler<WindowEvent> closeWindow() {
        return (WindowEvent event) -> {
            event.consume();
            clkExit(null);
        };
    }

}