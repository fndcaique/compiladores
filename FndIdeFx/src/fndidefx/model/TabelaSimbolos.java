/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fndidefx.model;

import java.util.ArrayList;

/**
 *
 * @author fnd
 */
public class TabelaSimbolos {

    private ArrayList<Simbolo> table;
    private ArrayList<Integer> indexlist;

    public TabelaSimbolos() {
        table = new ArrayList<>();
        indexlist = new ArrayList<>();
    }

    public boolean add(Simbolo simb) {
        table.add(simb);
        int pos = 0;
        if (table.size() > 1) {
            pos = find(simb.getId());
            if (pos >= indexlist.size()) {
                pos -= indexlist.size();
            }
        }
        indexlist.add(pos, table.size() - 1);
        return true;
    }

    public Simbolo search(Simbolo s) {
        if(table.isEmpty()){
            return null;
        }
        int pos = find(s.getId());
        if (pos >= table.size()) {
            return null;
        }
        return table.get(indexlist.get(pos));
    }

    private int find(String id) {
        int ini = 0, fim = indexlist.size() - 1;
        int comp = 0, med = (ini + fim) >> 1;
        while (ini <= fim) {
            med = (ini + fim) >> 1;
            comp = id.compareTo(table.get(indexlist.get(med)).getId());
            if (comp == 0) {
                return med;
            }
            if (comp > 0) {
                ini = med + 1;
            } else {
                fim = med - 1;
            }
        }
        if (comp > 0) {
            return med + indexlist.size() + 1;
        }
        return med + indexlist.size();
    }

    public ArrayList<Simbolo> getTable() {
        return table;
    }

    public ArrayList<Integer> getIndexList() {
        return indexlist;
    }

}
