/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fndidefx.model;

/**
 *
 * @author fnd
 */
public class Erro {
    private int line;
    private String msg;

    public Erro(int line, String msg) {
        this.line = line;
        this.msg = msg;
    }

    public int getLinha() {
        return line;
    }

    public void setLinha(int line) {
        this.line = line;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
    
    
}
