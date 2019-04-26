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
public class Simbolo implements Comparable<Simbolo>{
    private String id, token, valor;
    private int linha;

    public Simbolo(String id, String token, String valor, int linha) {
        this.id = id;
        this.token = token;
        this.valor = valor;
        this.linha = linha;
    }

    public Simbolo(String id, String token, int linha) {
        this.id = id;
        this.token = token;
        this.linha = linha;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public int getLinha() {
        return linha;
    }

    public void setLinha(int linha) {
        this.linha = linha;
    }

    @Override
    public int compareTo(Simbolo o) {
        int dif = id.compareTo(o.getId());
        if(dif != 0){
            return dif;
        }
        return token.compareTo(o.getToken());
    }

    @Override
    public String toString() {
        return "Simbolo{" + "id=" + id + ", token=" + token + ", valor=" + valor + ", linha=" + linha + '}';
    }
}