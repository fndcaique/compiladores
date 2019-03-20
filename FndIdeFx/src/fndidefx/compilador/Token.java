/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fndidefx.compilador;

import java.util.ArrayList;

/**
 *
 * @author fndcaique
 */
public class Token {

    private String name, lexema;
    private int linha;
    private static ArrayList<TokenType> list = null;

    public Token(String name, String lexema, int linha) {
        this.name = name;
        this.lexema = lexema;
        this.linha = linha;
    }

    public static ArrayList<TokenType> list() {
        if (list == null) {
            TokenType[] arr = TokenType.values();
            list = new ArrayList<>(arr.length);
            for (TokenType t : arr) {
                list.add(t);
            }
        }
        return list;
    }

    public int getLinha() {
        return linha;
    }

    public void setLinha(int linha) {
        this.linha = linha;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLexema() {
        return lexema;
    }

    public void setLexema(String lexema) {
        this.lexema = lexema;
    }

    public static TokenType parent(String lexema) {
        for (TokenType tk : list()) {
            if (tk.matches(lexema)) {
                return tk;
            }
        }
        return null;
    }
    
    public static ArrayList<TokenType> contains(String lexema) {
        ArrayList<TokenType> list = new ArrayList<>();
        for (TokenType tk : list()) {
            if (tk.matches(lexema)
                    || (!tk.getRegex().contains("[") && tk.getRegex().startsWith(lexema))) {
                list.add(tk);
            }
        }
        return list.isEmpty() ? null : list;
    }

    public enum TokenType {
        BEGIN("begin"), //"begin"},
        END("end"), //"end"},
        INT("int"), //"int"},
        DOUBLE("double"), //"double"},
        EXP("exp"), //"exp"},
        WHILE("while"), //"while"},
        FOR("for"), //"do"},
        IF("if"), //"if"},
        ELSE("else"), //"else"},
        VIRGULA(","), //","},
        PONTO_VIRGULA(";"), //";"},
        ABRE_PARENTESE("\\("), //"[(]"},
        FECHA_PARENTESE("\\)"), //"[)]"},
        ABRE_CHAVE("\\{"), //"[{]"},
        FECHA_CHAVE("\\}"), //"[}]"},
        MAIS("\\+"), //"[+]"},
        MENOS("-"), //"[-]"},
        MULT("\\*"), //"[*]"},
        DIV("/"), //"[/]"},
        ATRIBUIR("="), //"="},
        MENOR("<"), //"<"},
        MAIOR(">"), //">"},
        IGUAL("=="), //"=="},
        MENOR_IGUAL("<="), //"<="},
        MAIOR_IGUAL(">="), //">="},
        DIFERENTE("!="), //"!="},
        AND("&&"), //"&&"},
        OR("\\|\\|"), //"\\|\\|"},
        NOT("!"), //"!"},
        VALOR_INT("[0-9]+"), //"[0-9]+"},
        VALOR_DOUBLE("[0-9]+(\\.[0-9])*"), //"[0-9]+(\\.[0-9]+)*"},
        VALOR_EXP("[0-9]+(\\.[0-9]+)*e(\\+|-)[0-9]+"), //"[0-9]+(\\.[0-9]+)*e(\\+|-)[0-9]+"},
        ID_VAR("[a-zA-Z](\\d|[a-zA-Z])*"), //"[a-zA-Z](\\d|[a-zA-Z])*"},
        COMENTARIO_LINHA("//(.)*(\\n)?$"), //"//"},
        ABRE_COMENTARIO("/\\*"), //"/\\*"},
        FECHA_COMENTARIO("\\*/"), //"\\*/"},
        COMENTARIO_BLOCO("/\\*(.|\\R)*\\*/$"),
        NL("\\n"), //"\\n"}};
        INICIO("#"),
        FIM("\\$"),
        ERRO_LEXICO(""),
        ERRO_SINTATICO("");
        

        private String regex;
        TokenType(String regex) {
            this.regex = regex;
        }

        public String getRegex() {
            return regex;
        }

        public boolean matches(String lexema) {
            return lexema.matches(regex);
        }

    }

}
