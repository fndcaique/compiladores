/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fndidefx.compilador;

/**
 *
 * @author fndcaique
 */
public class Token {
    private String name, lexema;
    private int linha;
    public Token(String name, String lexema) {
        this.name = name;
        this.lexema = lexema;
        linha = 0;
    }
    
    public Token(String name, String lexema, int linha) {
        this.name = name;
        this.lexema = lexema;
        this.linha = linha;
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
    
}
