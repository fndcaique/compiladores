/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fndidefx.compilador;

import static fndidefx.compilador.Token.TokenType.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author fndcaique
 */
public class AnaliseSintatica {

    public static String ERROR = "########T_ERRO_SINTATICO########";
    private AnaliseLexica analex;
    private ArrayList<Erro> erros;
    private Token tk, tkprevius, tknext;
    private ArrayList<Token> arrtokens;
    private int proximo, anterior;
    private HashMap<String, String[]> mapfirst, mapfollow;

    public AnaliseSintatica(String code) {
        anterior = -1;
        proximo = 0;
        arrtokens = new ArrayList<>();
        analex = new AnaliseLexica(code);

        Token t = analex.nextToken();
        while (t != null) {
            arrtokens.add(t);
            t = analex.nextToken();
        }

        tknext = arrtokens.get(proximo);

        erros = new ArrayList<>();
        String[] arrtipos = new String[]{INT.name(), DOUBLE.name(), EXP.name()};
        String[] arrvalor = new String[]{VALOR_INT.name(), VALOR_DOUBLE.name(),
            VALOR_EXP.name()};
        String[] arridvar = new String[]{ID_VAR.name()};
        String[] arrabrechave = new String[]{ABRE_CHAVE.name()};
        String[] arrfechachave = new String[]{FECHA_CHAVE.name()};
        String[] arrabreparentese = new String[]{ABRE_PARENTESE.name()};
        String[] arrfechaparentese = new String[]{FECHA_PARENTESE.name()};
        String[] arrcmd = new String[]{FOR.name(), WHILE.name(), IF.name(), ID_VAR.name()};
        String[] arrnot = new String[]{NOT.name()};
        String[] arroprel = new String[]{MENOR.name(), MAIOR.name(), IGUAL.name(),
            MENOR_IGUAL.name(), MAIOR_IGUAL.name(), DIFERENTE.name()};
        String[] arrandor = new String[]{AND.name(), OR.name()};
        String[] arropari = new String[]{MAIS.name(), MENOS.name(), MULT.name(), DIV.name()};

        // inicializando map first
        mapfirst = new HashMap<>();
        mapfirst.put("start", new String[]{BEGIN.name()});
        mapfirst.put("cmd_var", arrtipos);
        mapfirst.put("tipo_var", arrtipos);
        mapfirst.put("valor", arrvalor);
        mapfirst.put("definir_var", arridvar);
        mapfirst.put("cmd", arrcmd);
        mapfirst.put("bool", join(arrnot, arridvar, arrvalor, arrabreparentese));
        mapfirst.put("operador_relacional", arroprel);
        mapfirst.put("and_or", arrandor);
        mapfirst.put("operador_aritmetico", arropari);
        mapfirst.put("operacao_aritmetica", join(arridvar, arrvalor));
        //mapfirst.put("", );

        // inicializando map follow
        //mapfollow.put("", );
    }

    private String[] join(String[]... a) {
        ArrayList<String> list = new ArrayList<>(a.length);
        for (String[] arr : a) {
            for (String st : arr) {
                list.add(st);
            }
        }
        String[] arr = new String[list.size()];
        for (int i = 0; i < list.size(); ++i) {
            arr[i] = list.get(i);
        }
        return arr;
    }

    private void next() {
        tkprevius = tk;
        tk = tknext;
        anterior++;
        proximo++;
        tknext = proximo < arrtokens.size() ? arrtokens.get(proximo) : null;
    }

    private void previus() {
        tknext = tk;
        proximo--;
        tk = tkprevius;
        anterior--;
        tkprevius = anterior >= 0 ? arrtokens.get(anterior) : null;

    }

    private void pularComando() {
        if (!(first("tipo_var", tk.getName()) || first("valor", tk.getName()) || first("cmd", tk.getName()))) {
            next();
        }
    }

    private boolean erroLexico() {
        return tk.getName().equals(ERRO_LEXICO.name());
    }

    private ArrayList<Erro> join(ArrayList<Erro> a, ArrayList<Erro> b) {
        ArrayList<Erro> c = new ArrayList<>(a.size() + b.size());
        int i = 0, j = 0;
        while (i < a.size() && j < b.size()) {
            if (a.get(i).getLine() <= b.get(j).getLine()) {
                c.add(a.get(i++));
            } else {
                c.add(b.get(j++));
            }
        }
        if (j == b.size()) {
            while (i < a.size()) {
                c.add(a.get(i++));
            }

        } else {
            while (j < b.size()) {
                c.add(b.get(j++));
            }
        }
        return c;
    }

    public ArrayList<Erro> start() {
        next();
        begin();
        cmdVar();
        cmd();
        end();
        return join(analex.getErros(), erros);
    }

    private boolean first(String tkparent, String tkchild) {
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

    /*private boolean follow(String tkparent, String tkchild){
        String[] tks = mapfollow.get(tkparent);
        for (String ts : tks) {
            if(ts.equals(tkchild)){
                return true;
            }
        }
        return false;
    }*/
    private boolean idVar() {
        if (tk.getName().equals(ID_VAR.name()) || erroLexico()) {
            next();
        } else {
            erros.add(new Erro(tk.getLinha(), "'" + tk.getLexema() + "', não é um id de variavel"));
            return false;
        }
        return true;

    }

    private boolean atribuidor() {
        if (tk.getName().equals(ATRIBUIR.name()) || erroLexico()) {
            next();
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_ATRIBUIR: Esperado = '=', Obtido = '" + tk.getLexema() + "'"));

            return false;
        }
        return true;
    }

    private boolean bool() {
        if (first("bool", tk.getName()) || erroLexico()) {
            if (tk.getName().equals(NOT.name()) || erroLexico()) {
                next();
                if (!bool()) {
                    return false;
                }
            } else if (tk.getName().equals(ABRE_PARENTESE.name()) || erroLexico()) {
                next();
                if (bool()) {
                    if (tk.getName().equals(FECHA_PARENTESE.name()) || erroLexico()) {
                        next();
                    } else {
                        erros.add(new Erro(tk.getLinha(), "ERRO_BOOL: Esperado = ')', Obtido = " + tk.getLexema()));

                    }
                } else {
                    return false;
                }
            } else { // id_var ou valor
                if (first("operador_relacional", tknext.getName())) {
                    next();
                    if (first("operador_relacional", tk.getName()) || erroLexico()) {
                        next();
                    } else {
                        erros.add(new Erro(tk.getLinha(),
                                "ERRO_OPERADOR_RELACIONAL: "
                                + "Esperado = {'<', '>', '==', '<=', '>=', '!='}, Obtido = " + tk.getLexema()));

                        return false;
                    }
                } else if (first("operador_aritmetico", tknext.getName())) {
                    operacaoAritmetica();
                }
                if (first("valor", tk.getName()) || tk.getName().equals(ID_VAR.name()) || erroLexico()) {
                    next();
                } else {
                    erros.add(new Erro(tk.getLinha(), "ERRO_BOOL: Esperado = {ID_VALOR, VALOR}, Obtido = " + tk.getLexema()));

                    return false;
                }
            }
            if(first("and_or", tk.getName())){
                next();
                bool();
            }
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_BOOL: Esperado = {ID_VAR, VALOR, '!', '('}, Obtido = " + tk.getLexema()));

            return false;
        }
        return true;
    }

    private boolean atribuicao() {
        next();
        if (atribuidor()) {
            if (first("operador_aritmetico", tknext.getName())) {
                if (!operacaoAritmetica()) {
                    return false;
                }
            } else if (first("valor", tk.getName()) || ID_VAR.name().equals(tk.getName())) {
                next();
            } else {
                erros.add(new Erro(tk.getLinha(), "ERRO_ATRIBUICAO: Esperado = {ID_VAR, VALOR}, Obtido = " + tk.getLexema()));
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    private void cmdFor() {
        if (FOR.name().equals(tk.getName())) {
            next();
            boolean errocabecalho = false;
            if (ABRE_PARENTESE.name().equals(tk.getName()) || erroLexico()) {
                next();
                if (ID_VAR.name().equals(tk.getName())) { // opcional
                    if (!atribuicao()) {
                        errocabecalho = true;
                    }
                }
                if (!errocabecalho) {
                    if (!pontoVirgula()) {
                        errocabecalho = true;
                    } else if (!bool()) {
                        errocabecalho = true;
                    } else if (!pontoVirgula()) {
                        errocabecalho = true;
                    } else {
                        if (ID_VAR.name().equals(tk.getName())) { // opcional
                            if (!atribuicao()) {
                                errocabecalho = true;
                            }
                        }
                        if (!errocabecalho) {
                            if (FECHA_PARENTESE.name().equals(tk.getName()) || erroLexico()) {
                                next();
                            } else {
                                erros.add(new Erro(tk.getLinha(), "ERRO_FOR: Esperado = ')' , Obtido = " + tk.getLexema()));
                                errocabecalho = true;
                            }
                        }
                    }
                }

            } else {
                erros.add(new Erro(tk.getLinha(), "ERRO_FOR: Esperado = '(' , Obtido = " + tk.getLexema()));
                errocabecalho = true;
            }
            if (errocabecalho) {
                procurarBlocoComando();
            }
            blocoComando();
        }
    }

    private void procurarBlocoComando() {
        while (!tk.getName().equals(ABRE_CHAVE.name())) {
            next();
        }
    }

    private boolean blocoComando() {
        if (tk.getName().equals(ABRE_CHAVE.name()) || erroLexico()) {
            next();
            cmd();
            if (tk.getName().equals(FECHA_CHAVE.name()) || erroLexico()) {
                next();
            } else {
                erros.add(new Erro(tk.getLinha(), "ERRO_BLOCO_COMANDO: Esperado = '}', Obtido = " + tk.getLexema()));
                //
                return false;
            }
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_BLOCO_COMANDO: Esperado = '{', Obtido = " + tk.getLexema()));

            return false;
        }
        return true;
    }

    private boolean condicao() {
        if (tk.getName().equals(ABRE_PARENTESE.name()) || erroLexico()) {
            next();
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_CONDICAO: Esperado = '(', Obtido = " + tk.getLexema()));

            return false;
        }
        if (bool()) {
            if (tk.getName().equals(FECHA_PARENTESE.name()) || erroLexico()) {
                next();
            } else {
                erros.add(new Erro(tk.getLinha(), "ERRO_CONDICAO: Esperado = ')', Obtido = " + tk.getLexema()));

                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    private boolean pontoVirgula() {
        if (tk.getName().equals(PONTO_VIRGULA.name()) || erroLexico()) {
            next();
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_PONTO_VIRGULA: Esperado = ';', Obtido = " + tk.getLexema()));

            return false;
        }
        return true;
    }

    private void cmdIf() {
        next();
        if (!condicao()) {
            procurarBlocoComando();
        }
        blocoComando();
        if (tk.getName().equals(ELSE.name())) { // opcional
            next();
            if (tk.getName().equals(IF.name())) { // opcional
                cmdIf();
            } else {
                blocoComando();
            }
        }
    }

    private boolean cmd() {
        String name = tk.getName();
        do {
            if (erroLexico() || tk.getName().equals(PONTO_VIRGULA.name())) {
                while (erroLexico()) {
                    next();
                }
            } else if (name.equals(ID_VAR.name())) { // atribuicao
                boolean erro = false;
                if (!atribuicao()) {
                    erro = true;
                } else if (!pontoVirgula()) {
                    erro = true;
                }
                if (erro) {
                    while (!tk.getName().equals(PONTO_VIRGULA.name()) // para pegar algum outro erro como 2 = 2;
                            && !(first("cmd", tk.getName()) && !tk.getName().equals(ID_VAR.name()))) {
                        next();
                    }
                    if (tk.getName().equals(PONTO_VIRGULA.name())) {
                        next();
                    }
                }
            } else if (name.equals(FOR.name())) {
                cmdFor();
            } else if (name.equals(IF.name())) {
                cmdIf();
            } else if (name.equals(WHILE.name())) { // while
                next();
                if (!condicao()) {
                    procurarBlocoComando();
                }
                blocoComando();
            } else { // erro
                erros.add(new Erro(tk.getLinha(), "ERRO_COMANDO: Esperado = {'if','while','for',ID_VAR}, Obtido = '" + tk.getLexema() + "'"));
                while (tk.getName().equals(END.name()) || tk.getName().equals(FECHA_CHAVE.name())
                        || first("cmd", tk.getName())) {
                    next();
                }
            }
            name = tk.getName();
        } while (first("cmd", name));
        return true;
    }

    private void pontoConfianca() {
        do {
            if (tk.getName().equals(ID_VAR.name()) && !tknext.getName().equals(ATRIBUIR.name())) {
                next();
            }
            while (!(first("cmd", tk.getName()) || END.name().equals(tk.getName())
                    || ABRE_CHAVE.name().equals(tk.getName()) || FECHA_CHAVE.name().equals(tk.getName()))) {
                next();
            }
            System.out.println("pontoConfianca = " + tk.getLexema() + ", " + tk.getName());
        } while (!END.name().equals(tk.getName()) && tk.getName().equals(ID_VAR.name()) && !tknext.getName().equals(ATRIBUIR.name()));

    }

    private void operadorAritmetico() {
        if (first("operador_aritmetico", tk.getName())) {
            next();
        } else {

            erros.add(new Erro(tk.getLinha(), "ERRO_OPERADOR_ARITMETIC0: Esperado: {'+', '-', '*', '/'}, Obtido = " + tk.getLexema()));

        }
    }

    private boolean operacaoAritmetica() {
        if (first("operacao_aritmetica", tk.getName()) || erroLexico()) {

            if (ABRE_PARENTESE.name().equals(tk.getName())) {
                next();
                if (!operacaoAritmetica()) {
                    return false;
                }
                if (FECHA_PARENTESE.name().equals(tk.getName()) || erroLexico()) {
                    next();
                } else {
                    erros.add(new Erro(tk.getLinha(), "ERRO_OPERACAO_ARITMETICA: Esperado = ')', Obtido = " + tk.getLexema()));

                    return false;
                }
            } else if (first("valor", tk.getName()) || ID_VAR.name().equals(tk.getName())) {
                next();
            } else {
                erros.add(new Erro(tk.getLinha(), "ERRO_OPERACAO_ARITMETICA: Esperado = {'(', ID_VAR, VALOR}, Obtido = " + tk.getLexema()));

                return false;
            }
            operadorAritmetico();
            if (ABRE_PARENTESE.name().equals(tk.getName())) {
                next();
                if (!operacaoAritmetica()) {
                    return false;
                }
                if (FECHA_PARENTESE.name().equals(tk.getName()) || erroLexico()) {
                    next();
                } else {
                    erros.add(new Erro(tk.getLinha(), "ERRO_OPERACAO_ARITMETICA: Esperado = ')', Obtido = " + tk.getLexema()));

                    return false;
                }
            } else if (first("valor", tk.getName()) || ID_VAR.name().equals(tk.getName())) {
                next();
            } else {
                erros.add(new Erro(tk.getLinha(), "ERRO_OPERACAO_ARITMETICA: Esperado = {'(', ID_VAR, VALOR}, Obtido = " + tk.getLexema()));

                return false;
            }
            // parte opcional
            if (first("operador_aritmetico", tk.getName())) {
                next();
                if (first("operador_aritmetico", tknext.getName()) || ABRE_PARENTESE.name().equals(tknext.getName())) {
                    if (!operacaoAritmetica()) {
                        return false;
                    }
                } else if (first("valor", tk.getName()) || ID_VAR.name().equals(tk.getName())) {
                    next();
                } else {
                    erros.add(new Erro(tk.getLinha(), "ERRO_OPERACAO_ARITMETICA: Esperado = {'(', ID_VAR, VALOR}, Obtido = " + tk.getLexema()));

                    return false;
                }
            }
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_OPERACAO_ARITMETICA: Esperado = {ID_VAR, VALOR, ABRE_PARENTES}, Obtido = " + tk.getLexema()));

            return false;
        }
        return true;
    }

    private boolean valor() {
        if (first("valor", tk.getName()) || erroLexico()) {
            // valor int, double, ou exp
            next();
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_VALOR: Esperado = {VALOR_INT, VALOR_DOUBLE, VALOR_EXP, ABRE_PARENTESE}, Obtido = " + tk.getLexema()));

            return false;
        }
        return true;
    }

    private void definirVar() {
        if (idVar()) {
            // parte opcional inicializacao
            if (tk.getName().equals(ATRIBUIR.name()) || erroLexico()) {
                next();
                valor();
            }
            // opcional, novo id
            if (tk.getName().equals(VIRGULA.name()) || erroLexico()) {
                next();
                definirVar();
            }
        }
    }

    private void cmdVar() {
        if (first("cmd_var", tk.getName())) {
            next();
            definirVar();
            pontoVirgula();
            if (first("cmd_var", tk.getName())) {
                cmdVar();
            }
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_DECLARAÇÃO_VAR: Esperado ={'int', 'double', 'exp'}\nObtido = '" + tk.getLexema() + "'"));

        }
    }

    private void begin() {
        if (tk.getName().equals(BEGIN.name())) {
            next();
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_BEGIN: Esperado = 'begin'\nObtido = \'" + tk.getLexema() + "'"));

        }
    }

    private void end() {
        if (tk.getName().equals(END.name()) || erroLexico()) {
            next();
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_END: Esperado = 'end'\nObtido = " + tk.getLexema()));

        }
    }

}
