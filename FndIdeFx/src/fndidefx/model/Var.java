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
public class Var extends Simbolo {
    private boolean init, declare;
    private int utilizada;
    private String tipo;
    
    public Var(String id, String token, String valor, int linha) {
        super(id, token, valor, linha);
        init = declare = false;
        utilizada = 0;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public boolean isInit() {
        return init;
    }

    public void setInit(boolean init) {
        this.init = init;
    }

    public boolean isDeclare() {
        return declare;
    }

    public void setDeclare(boolean declare) {
        this.declare = declare;
    }

    public int getUtilizada() {
        return utilizada;
    }

    public void setUtilizada(int utilizada) {
        this.utilizada = utilizada;
    }
    
}
