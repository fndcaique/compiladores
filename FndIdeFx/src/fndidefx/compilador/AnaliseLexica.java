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
public class AnaliseLexica {

    public static String ERROR = "t_erro_lexico";

    private int linha;
    private String cadeia;
    private final ArrayList<TokenMatch> tokens;
    private final String[] arrtokens = new String[]{ // falta tratar comentarios
        "t_begin:begin",
        "t_end:end",
        "t_int:int",
        "t_double:double",
        "t_exp:exp",
        "t_while:while",
        "t_do:do",
        "t_if:if",
        "t_else:else",
        "t_ponto:\\.",
        "t_virgula:,",
        "t_ponto_virgula:;",
        "t_abre_parenteses:[(]",
        "t_fecha_parenteses:[)]",
        "t_abre_chaves:[{]",
        "t_fecha_chaves:[}]",
        "t_mais:[+]",
        "t_menos:[-]",
        "t_mult:[*]",
        "t_div:[/]",
        "t_atribuidor:=",
        "t_menor:<",
        "t_maior:>",
        "t_igual:==",
        "t_menor_igual:<=",
        "t_maior_igual:>=",
        "t_diferente:!=",
        "t_and:&&",
        "t_or:\\|\\|",
        "t_valor_int:[0-9]+",
        "t_valor_double:[0-9]+(\\.[0-9]+)*",
        "t_valor_exp:[0-9]+(\\.[0-9]+)*e(\\+|-)[0-9]+",
        "t_id_var:[a-zA-Z](\\d|[a-zA-Z])*",
        "t_comentario_linha://",
        "t_abre_comentario:/\\*",
        "t_fecha_comentario:\\*/",
        "t_quebra_linha:\\n"};

    /*
    aEb = a * (10^b)
    
     */
    public AnaliseLexica(String entrada) {
        linha = 0;
        if (!entrada.endsWith("\n")) {
            entrada += '\n';
        }
        cadeia = entrada;
        tokens = new ArrayList<>();
        for (String token : arrtokens) {
            String[] ar = token.split(":");
            tokens.add(new TokenMatch(ar[0], ar[1]));
        }
    }

    private boolean isSimbleSimbol(char c) {
        return c == '+' || c == '-' || c == '.';
    }

    private void trimStart() {
        int i = 0;
        while (i < cadeia.length() && cadeia.charAt(i) == ' ') {
            i++;
        }
        cadeia = cadeia.substring(i);
    }

    /**
     *
     * @return
     */
    public Token nextToken() {
        if (cadeia.isEmpty()) {
            return null;
        }
        boolean continuar;
        String lex;
        char c;
        ArrayList<TokenMatch> tks;
        TokenMatch tk = null;
        int ini, fim;
        do {
            ini = fim = 0;
            tks = tokens;
            continuar = false;
            lex = "";
            trimStart();
            if (cadeia.isEmpty()) {
                return null;
            }
            c = cadeia.charAt(fim++);
            lex += c;
            if (Character.isLetterOrDigit(c)) { // letra ou numero
                /*Verificar mudança para: (letra, [letra|numero]*) | [simbesp]+$   */
                while (fim < cadeia.length()
                        && (Character.isLetterOrDigit(c = cadeia.charAt(fim++)) || isSimbleSimbol(c))) {
                    lex += c;
                }
                --fim;
                tk = tokenProdutor(lex);
            } else { // simbolos especial

                while (fim < cadeia.length() && tks != null) {
                    /*  2t#  */
                    c = cadeia.charAt(fim++);
                    lex += c;
                    tks = tokenContais(tks, lex);
                }
                if (tks == null) {
                    --fim;
                }
                lex = cadeia.subSequence(ini, fim) + "";
                tk = tokenProdutor(lex);
                if (tk != null) {
                    switch (tk.getToken()) {
                        case "t_abre_comentario":
                            continuar = true;
                            lex = "";
                            while (fim < cadeia.length() && !lex.endsWith("*/")) {
                                lex += cadeia.charAt(fim++);
                            }
                            break;
                        case "t_comentario_linha":
                            continuar = true;
                            while (fim < cadeia.length() && cadeia.charAt(fim) != '\n') {
                                ++fim;
                            }
                            break;
                        case "t_quebra_linha":
                            ++linha;
                            continuar = true;
                            break;
                        default:
                            break;
                    }
                }
            }
            /*Verificar mudança para: (letra, [letra|numero]*) | [simbesp]+$   */

            cadeia = cadeia.substring(fim); // atualiza cadeia
        } while (continuar);
        if (tk != null) {
            return new Token(tk.getToken(), lex, linha);
        } else {
            return new Token(ERROR, lex, linha);
        }
    }

    private TokenMatch tokenProdutor(String lexema) {
        for (TokenMatch token : tokens) {
            if (token.produz(lexema)) {
                return token;
            }
        }
        return null;
    }

    /**
     * returna os tokens que produzem o lexema
     */
    private ArrayList<TokenMatch> tokenContais(ArrayList<TokenMatch> tokens, String lexema) {
        ArrayList<TokenMatch> list = new ArrayList<>();
        for (TokenMatch tk : tokens) {
            if (tk.produz(lexema)
                    || (!tk.getMatch().contains("[") && tk.getMatch().startsWith(lexema))) {
                list.add(tk);
            }
        }
        return list.isEmpty() ? null : list;
    }

    private class TokenMatch {

        private String token, match;

        public TokenMatch(String token, String match) {
            this.token = token;
            this.match = match;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getMatch() {
            return match;
        }

        public void setMatch(String match) {
            this.match = match;
        }

        public boolean produz(String lexema) {
            return lexema.matches(match);
        }

    }

}
