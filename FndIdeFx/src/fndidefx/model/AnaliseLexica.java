/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fndidefx.model;

import fndidefx.model.Token.TokenType;
import java.util.ArrayList;

/**
 *
 * @author fndcaique
 */
public class AnaliseLexica {

    public static String ERROR = "ERRO_LEXICO";
    public static Simbolo FIM_CADEIA = new Simbolo("$", TokenType.FIM.name(), 0);
    private ArrayList<Erro> erros;
    private boolean continuar;
    private int linha;
    private String text;
    private boolean finalisado;
    //private final ArrayList<TokenType> tokens;

    public AnaliseLexica(String entrada) {
        linha = 1;
        if (!entrada.endsWith("\n")) {
            entrada += '\n';
        }
        text = entrada;
        erros = new ArrayList<>();
        finalisado = false;
    }

    public ArrayList<Erro> getErros() {
        return erros;
    }


    /*
    aEb = a * (10^b)
    
     */
    private void removerEspacosInicio() {
        int i = 0;
        while (i < text.length() && text.charAt(i) == ' ') {
            i++;
        }
        text = text.substring(i);
    }

    private boolean mais(char c) {
        return c == '+';
    }

    private boolean menos(char c) {
        return c == '-';
    }

    private boolean mult(char c) {
        return c == '*';
    }

    private boolean div(char c) {
        return c == '/';
    }

    private boolean operadorAritmetico(char c) {
        return mais(c) || menos(c) || mult(c) || div(c);
    }

    private boolean ponto(char c) {
        return c == '.';
    }

    private boolean virgula(char c) {
        return c == ',';
    }

    private boolean pontoVirgula(char c) {
        return c == ';';
    }

    private boolean not(char c) {
        return c == '!';
    }

    private boolean and(char c) {
        return c == '&';
    }

    private boolean or(char c) {
        return c == '|';
    }

    private boolean abreParentese(char c) {
        return c == '(';
    }

    private boolean fechaParentese(char c) {
        return c == ')';
    }

    private boolean abreChave(char c) {
        return c == '{';
    }

    private boolean fechaChave(char c) {
        return c == '}';
    }

    private boolean nl(char c) {
        return c == '\n';
    }

    private boolean space(char c) {
        return c == ' ';
    }

    private boolean atribuir(char c) {
        return c == '=';
    }

    private boolean letra(char c) {
        return Character.isLetter(c);
    }

    private boolean numero(char c) {
        return Character.isDigit(c);
    }

    private boolean delimitador(char c) {
        return /*nl(c) || space(c) || virgula(c) || pontoVirgula(c) || atribuir(c)
                || operadorAritmetico(c) || and(c) || or(c) || not(c)
                || abreParentese(c) || fechaParentese(c) || abreChave(c) || fechaChave(c)
                || */ (!letra(c) && !numero(c));
    }

    /**
     * retorna a posição do simbolo delimitador
     */
    private int findDelimitador(int posini) {
        while (posini < text.length() && !delimitador(text.charAt(posini))) {
            ++posini;
        }
        return posini;
    }

    private int findDelimitadorID(int p) {
        while (p < text.length() && letra(text.charAt(p))) {
            p++;
        }
        return p;
    }

    private int iniciaComNumero() {
        int i = 0;
        char c;
        while (i < text.length() && numero(text.charAt(i))) {
            i++;
        }
        if (i < text.length()) { // tem mais simbolo
            if (ponto(text.charAt(i))) { // double
                ++i;
                while (i < text.length() && numero(text.charAt(i))) {
                    ++i;
                }
                if (i < text.length()
                        && text.charAt(i) == 'e' && ++i < text.length() /*exp*/
                        && (mais(c = text.charAt(i)) || menos(c))) {
                    i = findDelimitador(i + 1);
                } else {
                    i = findDelimitador(i);
                }
            } else if (text.charAt(i) == 'e' && ++i < text.length() /*exp*/
                    && (mais(c = text.charAt(i)) || menos(c))) {
                i = findDelimitador(i + 1);
            } else {
                i = findDelimitador(i);
            }
        }
        return i;
    }

    public Simbolo nextSimbolo() {

        if (finalisado) {
            return FIM_CADEIA;
        }
        String lex;
        String tipo = "";
        char c;
        ArrayList<TokenType> tks;
        TokenType tk;
        int fim;
        do {
            removerEspacosInicio();
            if (text.isEmpty()) {
                finalisado = true;
                FIM_CADEIA.setLinha(linha);
                return FIM_CADEIA;
            }
            fim = 0;
            tk = null;
            tks = Token.list();
            continuar = false;
            c = text.charAt(0);
            /* somente exp e comentario que não são achados com o findDelimitador*/
            if (numero(c)) { // tokens que iniciam por numero
                fim = iniciaComNumero();
            } else if (text.startsWith("\n")) {
                ++linha;
                continuar = true;
            } else if (letra(c)) {
                fim = findDelimitadorID(0);
            } else if (text.length() >= 2) { // se pode ser comentario
                if (text.startsWith("//")) {
                    fim = text.indexOf("\n");
                } else if (text.startsWith("/*")) {
                    fim = text.indexOf("*/");
                    fim = fim < 0 ? text.length() : fim + 2;
                    for (int i = 0; i < fim; ++i) {
                        if (text.charAt(i) == '\n') {
                            ++linha;
                        }
                    }
                } else if (Token.parent(text.substring(0, 2)) != null) {
                    fim = 2;
                } else {
                    fim = findDelimitador(0);
                }
            }
            if (fim == 0) { // delimitador é o primeiro caracter
                ++fim;
            }
            lex = text.subSequence(0, fim) + "";
            tk = Token.parent(lex);
            if (tk != null) {
                switch (tk) {
                    case COMENTARIO_BLOCO:
                    case COMENTARIO_LINHA:
                        continuar = true;
                        break;
                    case VALOR_INT:
                    case VALOR_DOUBLE:
                    case VALOR_EXP:
                        tipo = tk.name().substring(tk.name().indexOf("_") + 1).toLowerCase();
                        break;
                }
            } else { // erro lexico

                // if (tk == null) {
                erros.add(new Erro(linha, ERROR + " = " + lex));
                //return new Simbolo(lex, TokenType.ERRO_LEXICO.name(), linha);
                continuar = true;
                //  }
            }
            text = text.substring(fim); // remove lexema atual

        } while (continuar);
        return new Simbolo(tipo, lex, tk.name(), linha);
    }
}
