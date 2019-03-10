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
public class Token implements Comparable<Token>{
    private String name, match;
    public Token(String name, String match) {
        this.name = name;
        this.match = match;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMatch() {
        return match;
    }

    public void setMatch(String match) {
        this.match = match;
    }

    @Override
    public int compareTo(Token o) {
        return match.compareTo(o.getMatch());
    }

    public boolean produz(String lexema){
        return lexema.matches(match);
    }
    
}
