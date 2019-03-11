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
    
    private class PrivateToken{
        private String token, match;

        public PrivateToken(String token, String match) {
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
        
        public boolean produz(String lexema){
            return lexema.matches(match);
        }
        
    }

    private String cadeia;
    private final ArrayList<PrivateToken> tokens;
    private final String[] arrtokens = new String[]{
        "t_begin:begin",
        "t_end:end",
        "t_int:int",
        "t_double:double",
        "t_exp:exp",
        "t_while:while",
        "t_do:do",
        "t_if:if",
        "t_else:else",
        "t_virgula:,",
        "t_abre_parenteses:[(]",
        "t_fecha_parenteses:[)]",
        "t_abre_chave:[{]",
        "t_fecha_chave:[}]",
        "t_op_mais:[+]",
        "t_op_menos:[-]",
        "t_op_mult:[*]",
        "t_op_div:[/]",
        "t_atribuidor:=",
        "t_menor:<",
        "t_maior:>",
        "t_igual:==",
        "t_menor_igual:<=",
        "t_maior_igual:>=",
        "t_diferente:!=",
        "t_and:&&",
        "t_or:||",
        "t_valor_int:[0-9]+",
        "t_valor_double:[0-9]+[[.][0-9]+]*",
        "t_valor_exp:[0-9]+[[.][0-9]+]*E[0-9]+",
        "t_id_var:[a-zA-Z]+"};

    /*
    aEb = a * (10^b)
    
     */

    public AnaliseLexica(String entrada) {
        cadeia = entrada;
        tokens = new ArrayList<>();
        for (String token : arrtokens) {
            String[] ar = token.split(":");
            tokens.add(new PrivateToken(ar[0], ar[1]));
        }
    }

    /**
     *
     * @return
     */
    public Token nextToken() {
        String lex = "";
        ArrayList<PrivateToken> tks = tokens;
        PrivateToken tk = null;
        int ini = 0, fim = 0;
        while (fim < cadeia.length() && tks != null) { /*  2t#  */
            lex += cadeia.charAt(fim++);
            tks = findToken(tks, lex);
        }
        lex = cadeia.subSequence(ini, fim-1)+"";
        cadeia = cadeia.substring(fim).trim();
        return new Token(findToken(tokens, lex).get(0).getToken(), lex);
    }

    /**
     * returna os tokens que produzem o lexema
     */
    private ArrayList<PrivateToken> findToken(ArrayList<PrivateToken> tokens, String lexema) {
        ArrayList<PrivateToken> list = new ArrayList<>();
        for (PrivateToken tk : tokens) {
            if(tk.produz(lexema) || (!lexema.contains("[") && tk.getMatch().startsWith(lexema))){
                list.add(tk);
            }
        }
        return list.isEmpty() ? null : list;
    }

}
