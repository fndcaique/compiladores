/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fndidefx.model;

import static fndidefx.model.Token.TokenType.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author fnd
 */
public final class Linguagem {

    private static Linguagem fnd = null;
    private HashMap<String, String[]> mapfirst, mapfollow;
    private String[] arrbegin = new String[]{BEGIN.name()};
    private String[] arrend = new String[]{END.name()};
    private String[] arrfinal = new String[]{"$"};
    private String[] arrtipos = new String[]{INT.name(), DOUBLE.name(), EXP.name()};
    private String[] arridvar = new String[]{ID_VAR.name()};
    private String[] arrabrechave = new String[]{ABRE_CHAVE.name()};
    private String[] arrfechachave = new String[]{FECHA_CHAVE.name()};
    private String[] arrabreparentese = new String[]{ABRE_PARENTESE.name()};
    private String[] arrfechaparentese = new String[]{FECHA_PARENTESE.name()};
    private String[] arrcmd = new String[]{FOR.name(), WHILE.name(), IF.name(), ID_VAR.name()};
    private String[] arrnot = new String[]{NOT.name()};
    private String[] arroprel = new String[]{MENOR.name(), MAIOR.name(), IGUAL.name(),
        MENOR_IGUAL.name(), MAIOR_IGUAL.name(), DIFERENTE.name()};
    private String[] arropari = new String[]{MAIS.name(), MENOS.name(), MULT.name(), DIV.name()};
    private String[] arrvirgula = new String[]{VIRGULA.name()};
    private String[] arrif = new String[]{IF.name()};
    private String[] arrmenor = new String[]{MENOR.name()};
    private String[] arrmaior = new String[]{MAIOR.name()};
    private String[] arrigual = new String[]{IGUAL.name()};
    private String[] arrmenorigual = new String[]{MENOR_IGUAL.name()};
    private String[] arrmaiorigual = new String[]{MAIOR_IGUAL.name()};
    private String[] arrdiferente = new String[]{DIFERENTE.name()};
    private String[] arrand = new String[]{AND.name()};
    private String[] arror = new String[]{OR.name()};
    private String[] arrelse = new String[]{ELSE.name()};
    private String[] arrwhile = new String[]{WHILE.name()};
    private String[] arrfor = new String[]{FOR.name()};
    private String[] arrpontovirgula = new String[]{PONTO_VIRGULA.name()};
    private String[] arratribuidor = new String[]{ATRIBUIR.name()};
    private String[] arrvalorint = new String[]{VALOR_INT.name()};
    private String[] arrvalordouble = new String[]{VALOR_DOUBLE.name()};
    private String[] arrvalorexp = new String[]{VALOR_EXP.name()};
//    private String[] arr = new String[]{};
//    private String[] arr = new String[]{};

    private Linguagem() {
        // inicializando map isFirst
        mapfirst = new HashMap<>();

        mapfirst.put("start", arrbegin);
        mapfirst.put("begin", arrbegin);
        mapfirst.put("end", arrend);
        mapfirst.put("cmd_var", arrtipos);
        mapfirst.put("tipo_var", arrtipos);
        mapfirst.put("definir_var", arridvar);
        mapfirst.put("virgula", arrvirgula);
        mapfirst.put("id_var", arridvar);
        mapfirst.put("cmd", arrcmd);
        mapfirst.put("cmd_if", arrif);
        mapfirst.put("valor_int", arrvalorint);
        mapfirst.put("valor_double", arrvalordouble);
        mapfirst.put("valor_exp", arrvalorexp);
        mapfirst.put("valor", join(arrvalorint, arrvalordouble, arrvalorexp));
        mapfirst.put("valor_gen", join( new String[]{MENOS.name()}, first("valor"),
                first("id_var"), arrabreparentese));
        mapfirst.put("abre_parentese", arrabreparentese);
        mapfirst.put("fecha_parentese", arrfechaparentese);
        mapfirst.put("abre_chave", arrabrechave);
        mapfirst.put("fecha_chave", arrfechachave);
        mapfirst.put("bool", join(arrnot, arridvar, first("valor"), arrabreparentese));
        mapfirst.put("operador_relacional", arroprel);
        mapfirst.put("menor", arrmenor);
        mapfirst.put("maior", arrmaior);
        mapfirst.put("igual", arrigual);
        mapfirst.put("menor_igual", arrmenorigual);
        mapfirst.put("maior_igual", arrmaiorigual);
        mapfirst.put("diferente", arrdiferente);
        mapfirst.put("and_or", join(arrand, arror));
        mapfirst.put("and", arrand);
        mapfirst.put("or", arror);
        mapfirst.put("not", arrnot);
        mapfirst.put("cmd_else", arrelse);
        mapfirst.put("else", arrelse);
        mapfirst.put("cmd_while", arrwhile);
        mapfirst.put("while", arrwhile);
        mapfirst.put("cmd_for", arrfor);
        mapfirst.put("for", arrfor);
        mapfirst.put("ponto_virgula", arrpontovirgula);
        mapfirst.put("operador_aritmetico", arropari);
        mapfirst.put("atribuidor", arratribuidor);
        mapfirst.put("atribuicao", arridvar);
        mapfirst.put("operacao_aritmetica", first("valor_gen"));
        //mapfirst.put("operacao_aritmetica", first("valor_gen"));

        //mapfirst.put("", );
        // inicializando map follow
        mapfollow = new HashMap<>(mapfirst.size());

        mapfollow.put("start", arrfinal);
        mapfollow.put("begin", first("cmd"));
        mapfollow.put("end", arrfinal);
        mapfollow.put("cmd_var", first("cmd"));
        mapfollow.put("tipo_var", arridvar);
        mapfollow.put("definir_var", join(arrvirgula, first("cmd")));
        mapfollow.put("virgula", first("definir_var"));
        mapfollow.put("id_var", join(first("atribuidor"), arrvirgula, arrpontovirgula, first("and_or"), first("operador_relacional")));
        mapfollow.put("cmd", join(first("end"), arrfechachave));
        mapfollow.put("cmd_if", follow("cmd"));
        mapfollow.put("bool", join(arrfechaparentese, first("and_or"),
                first("operador_relacional"), first("operador_aritmetico")));
        mapfollow.put("atribuicao", arrpontovirgula);
        mapfollow.put("valor_gen", join(first("operador_aritmetico"), follow("bool"),
                follow("atribuicao"), first("operador_aritmetico"),
                first("operacao_aritmetica"), first("fecha_parentese")));
        mapfollow.put("abre_parentese", join(arrnot, arrabreparentese, first("valor_gen")));
        mapfollow.put("fecha_parentese", join(arrpontovirgula, arrabrechave, first("operador_aritmetico"), first("operador_relacional")));
        mapfollow.put("abre_chave", first("cmd"));
        mapfollow.put("fecha_chave", join(first("cmd"), follow("cmd"), first("else")));
        mapfollow.put("operador_relacional", join(first("valor_gen")));
        mapfollow.put("menor", first("valor_gen"));
        mapfollow.put("maior", follow("menor"));
        mapfollow.put("igual", follow("menor"));
        mapfollow.put("menor_igual", follow("menor"));
        mapfollow.put("maior_igual", follow("menor"));
        mapfollow.put("diferente", follow("menor"));
        mapfollow.put("and_or", join(first("valor_gen"), first("bool")));
        mapfollow.put("and", follow("and_or"));
        mapfollow.put("or", follow("and"));
        mapfollow.put("not", arrabreparentese);
        mapfollow.put("cmd_else", follow("cmd_if"));
        mapfollow.put("else", join(arrif, arrabrechave));
        mapfollow.put("cmd_while", follow("cmd"));
        mapfollow.put("while", arrabreparentese);
        mapfollow.put("cmd_for", follow("cmd"));
        mapfollow.put("for", arrabreparentese);
        mapfollow.put("ponto_virgula", join(first("cmd"), first("cmd_var")));
//        mapfollow.put("letra",);
        mapfollow.put("valor", follow("valor_gen"));
        mapfollow.put("atribuidor", first("valor_gen"));
        mapfollow.put("operador_aritmetico", first("valor_gen"));
        mapfollow.put("operacao_aritmetica", join(first("fecha_parentese"),
                follow("valor_gen")));
    }

    public static Linguagem getInstace() {
        if (fnd == null) {
            fnd = new Linguagem();
        }
        return fnd;
    }

    public String[] first(String exp) {
        return mapfirst.get(exp);
    }

    public String[] follow(String exp) {
        return mapfollow.get(exp);
    }

    public String[] join(String[]... a) {
        HashSet<String> set = new HashSet<>(a.length);

        for (String[] arr : a) {
            set.addAll(Arrays.asList(arr));
        }

        String[] arr = new String[set.size()];
        return set.toArray(arr);
    }

    public boolean isFirst(String tkparent, String tkchild) {
        String[] tks = mapfirst.get(tkparent);
        if (tks != null) {
            for (String ts : tks) {
                if (ts.equals(tkchild)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isFollow(String tkparent, String tkchild) {
        String[] tks = mapfollow.get(tkparent);
        for (String ts : tks) {
            if (ts.equals(tkchild)) {
                return true;
            }
        }
        return false;
    }

}
