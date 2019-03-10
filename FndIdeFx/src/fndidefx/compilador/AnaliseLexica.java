/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fndidefx.compilador;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author fndcaique
 */
public class AnaliseLexica {

    private String cadeia, entrada;
    private final ArrayList<Token> tokens;
    private final String[] arrtokens = new String[]{
        "t_begin:begin",
        "t_end:end",
        "t_int:int",
        "t_double:double",
        "t_char:char",
        "t_virgula:,",
        "t_abre_chave:[{]",
        "t_fecha_chave:[}]",
        "t_if:if",
        "t_else:else",
        "t_menor:<",
        "t_maior:>",
        "t_igual:==",
        "t_menor_igual:<=",
        "t_maior_igual:>=",
        "t_diferente:!=",
        "t_and:&&",
        "t_or:||",
        "t_while:while",
        "t_do:do",
        "t_letra:[a-zA-Z]",
        "t_algarismo:[0-9]",
        "t_valor_int:[0-9]+",
        "t_valor_double:[0-9]+[.][0-9]+",
        "t_op_mais:[+]",
        "t_op_menos:[-]",
        "t_op_mult:[*]",
        "t_op_div:[/]",
        "t_atribuidor:=",
        "t_id_var:[a-zA-Z]+[\\w]*"
    };

    public AnaliseLexica(String entrada) {
        tokens = new ArrayList<>();
        cadeia = entrada + "$";
        for (String token : arrtokens) {
            String[] ar = token.split(":");
            tokens.add(new Token(ar[0], ar[1]));
        }
    }
    
    /**
     *
     * @return
     */
    public String nextLexema() {

        String tk = "";
        int i = 0;
        char c = cadeia.charAt(i++);
        
        tk += c;
        while (i < cadeia.length() && Character.isLetterOrDigit(c)) { // revisar erro em a*a, a*-a, -a*b
            tk += c;
            c = cadeia.charAt(i++);
        }

        while (i < cadeia.length() && cadeia.charAt(i) == ' ') {
            ++i;
        }

        cadeia = cadeia.substring(i);

        return tk;
    }

    public Token findToken(String lexema) {
        for (Token token : tokens) {
            if(token.produz(lexema))
                return token;
        }
        return null;
    }

}
