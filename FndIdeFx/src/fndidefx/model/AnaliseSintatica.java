/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fndidefx.model;

import static fndidefx.model.Token.TokenType.*;
import java.util.ArrayList;
import java.util.Stack;
import javafx.util.Pair;

/**
 *
 * @author fndcaique
 */
public class AnaliseSintatica {

    /* quando ocorrer um erro deve ser buscado quem tem como follow o proximo token*/
    public static String ERROR = "########T_ERRO_SINTATICO########";
    private AnaliseLexica analex;
    private ArrayList<Erro> erros;
    private Simbolo tk, tkprevius, tknext;
    private ArrayList<Simbolo> arrtokens;
    private int idxprox, idxant, idxatual;
    private Linguagem ling;
    private TabelaSimbolos tablesimb, tablevars;
    private Simbolo varRec;

    private String tipoAtual, codemed;
    private boolean finalizado = false;

    public AnaliseSintatica(String code) {
        codemed = "";
        tablesimb = new TabelaSimbolos();
        tablevars = new TabelaSimbolos();
        idxant = -1;
        idxatual = idxprox = 0;
        arrtokens = new ArrayList<>();
        ling = Linguagem.getInstace();
        analex = new AnaliseLexica(code);

//
//        Token t = analex.nextSimbolo();
//        while (t != null) {
//            arrtokens.add(t);
//            t = analex.nextSimbolo();
//        }
//
        arrtokens.add(analex.nextSimbolo());
        tknext = arrtokens.get(idxprox);

        erros = new ArrayList<>();

    }

    public TabelaSimbolos getTabelaSimbolos() {
        return this.tablesimb;
    }

    private void next() {
        Simbolo s = analex.nextSimbolo();
        //if (!s.getToken().equals(END.name())) {
        arrtokens.add(s);
        tkprevius = tk;
        tk = tknext;
        idxant++;
        idxatual = idxprox++;
        tknext = arrtokens.get(idxprox);
        tablesimb.add(tk);

        s = tablevars.search(tk);
        if (s != null) {
            // existe na tabela de variaveis
            s.setLinha(tk.getLinha());
            tk = s;
        } else if (ID_VAR.name().equals(tk.getToken())) {
            tablevars.add(tk);
        }
        //} else {
        //  tk = arrtokens.get(idxprox);
        //}
    }

    private void previus() {
        tknext = tk;
        idxprox--;
        tk = tkprevius;
        idxant--;
        tkprevius = idxant >= 0 ? arrtokens.get(idxant) : null;

    }

    private boolean erroLexico() {
        return tk.getToken().equals(AnaliseLexica.ERROR);
    }

    private ArrayList<Erro> join(ArrayList<Erro> a, ArrayList<Erro> b) {
        ArrayList<Erro> c = new ArrayList<>(a.size() + b.size());
        int i = 0, j = 0;
        while (i < a.size() && j < b.size()) {
            if (a.get(i).getLinha() <= b.get(j).getLinha()) {
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

    private void pularErrosLexicos() {
        while (tk.getToken().equals(AnaliseLexica.ERROR)) {
            next();
        }
    }

    public ArrayList<Erro> start() {

        next();
        pularErrosLexicos();
        begin();
        cmdVar();
        cmd();
        end();
        next();
        while (!tk.getToken().equals(FIM.name())) {
            erros.add(new Erro(tk.getLinha(), "ERRO: Simbolo '" + tk.getId() + "' não esperado após o fim do programa"));
            next();
        }
        // verificar erros semanticos
        tablevars.getTable().stream().filter((var) -> (var.getUtilizada() < 1)).forEach((var) -> {
            erros.add(new Erro(-1, "ATENÇÃO: Var '" + var.getId() + "' não utilizada"));
        });
        erros = join(analex.getErros(), erros);
        erros.sort((Erro o1, Erro o2) -> o1.getLinha() - o2.getLinha());
        return erros;
    }

    /**
     * pula o token se ele não for first de ninguém
     */
    private boolean pularToken() {
        if (!ling.isFirst(tk.getId())) {
            next();
            return true;
        }
        return false;
    }

    private boolean atribuidor() {
        if (tk.getToken().equals(ATRIBUIR.name()) || erroLexico()) {
            next();
            return true;
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_ATRIBUIR: Esperado = '=', Obtido = '" + tk.getId() + "'"));
        }
        return false;
    }

    private boolean bool() {
        boolean r = true, not = false;
        Simbolo va = null;
        pularErrosLexicos();
        if (ling.isFirst("bool", tk.getId())) {
            // inicio primeiro operando
            if (tk.getToken().equals(NOT.name())) {
                next();
                bool();
                not = true;
            }
            if (tk.getToken().equals(ABRE_PARENTESE.name()) || erroLexico()) { // opcional
                next();
                if (bool()) {
                    if (tk.getToken().equals(FECHA_PARENTESE.name()) || erroLexico()) {
                        next();
                    } else {
                        erros.add(new Erro(tk.getLinha(), "ERRO_BOOL: Esperado = ')', Obtido = '" + tk.getId() + "'"));
                        //pularToken();
                    }
                }
            } else// id_var ou valor
            {
                if (ling.isFirst("operador_aritmetico", tk.getId())
                        || ling.isFirst("operador_aritmetico", tknext.getId())) { // opcional

                    va = tk;
                    Pair<String, String> saida = operacaoAritmetica();
                    if (va.getToken().equals(ID_VAR.name())) {
                        va.setUtilizada(va.getUtilizada() + 1);
                    } else if (!not && saida == null) {
                        erros.add(new Erro(tk.getLinha(), "ERRO_BOOL: Esperado = {ID_VALOR, VALOR}, Obtido = '" + tk.getId() + "'"));
                        pularToken();
                    }
                } else if (tk.getToken().equals(ID_VAR.name())) {
                    tk.setUtilizada(tk.getUtilizada() + 1);
                    next();
                } else if (ling.isFirst("valor", tk.getId())) {
                    next();
                } else if (!not) {
                    erros.add(new Erro(tk.getLinha(), "ERRO_BOOL: Esperado = {ID_VALOR, VALOR}, Obtido = '" + tk.getId() + "'"));
                    pularToken();
                }
            }
            // fim primeiro operando

            //operador
            pularErrosLexicos();
            if (ling.isFirst("operador_relacional", tk.getId())) { // obrigatório se não for not
                next();
            } else if (!not) {
                erros.add(new Erro(tk.getLinha(),
                        "ERRO_OPERADOR_RELACIONAL: "
                        + "Esperado = {'<', '>', '==', '<=', '>=', '!='}, Obtido = '" + tk.getId() + "'"));
                pularToken();
            }
            pularErrosLexicos();
            // fim operador

            //segundo operando
            if (tk.getToken().equals(NOT.name())) {
                next();
                bool();
                not = true;
            }
            if (tk.getToken().equals(ABRE_PARENTESE.name()) || erroLexico()) { // opcional
                next();
                if (bool()) {
                    if (tk.getToken().equals(FECHA_PARENTESE.name()) || erroLexico()) {
                        next();
                    } else {
                        erros.add(new Erro(tk.getLinha(), "ERRO_BOOL: Esperado = ')', Obtido = '" + tk.getId() + "'"));
                        //pularToken();
                    }
                }
            } else// id_var ou valor
            {
                if (ling.isFirst("operador_aritmetico", tknext.getId())) { // opcional

                    va = tk;
                    Pair<String, String> saida = operacaoAritmetica();
                    if (va.getToken().equals(ID_VAR.name())) {
                        va.setUtilizada(va.getUtilizada() + 1);
                    } else if (!not) {
                        erros.add(new Erro(tk.getLinha(), "ERRO_BOOL: Esperado = {ID_VALOR, VALOR}, Obtido = '" + tk.getId() + "'"));
                        pularToken();
                    }
                } else if (tk.getToken().equals(ID_VAR.name())) {
                    tk.setUtilizada(tk.getUtilizada() + 1);
                    next();
                } else if (ling.isFirst("valor", tk.getId())) {
                    next();
                } else if (!not) {
                    erros.add(new Erro(tk.getLinha(), "ERRO_BOOL: Esperado = {ID_VALOR, VALOR}, Obtido = '" + tk.getId() + "'"));
                    pularToken();
                }
            }
            // fim segundo operando

            if (ling.isFirst("and_or", tk.getId())) { // opcional
                next();
                r = bool();
            }
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_BOOL: Esperado = "
                    + "{ID_VAR, VALOR, '!', '('}, Obtido = '" + tk.getId() + "'"));
            pularToken();
            r = false;
        }
        return r;
    }

    private Pair<String, String> atribuicao() {
        Simbolo var = tk;
        next();
        Pair<String, String> saida = null;
        pularErrosLexicos();
        boolean erro = false;
        String valor = "", cast = "";
        if (atribuidor()) {
            if (tk.getToken().equals(ABRE_PARENTESE.name())) { // casting
                if (ling.isFirst("tipo_var", tknext.getId())) {
                    next();
                    cast = tk.getId();
                    next();
                    if (tk.getToken().equals(FECHA_PARENTESE.name())) {
                        next();
                    } else {
                        erros.add(new Erro(tk.getLinha(), "ERRO_CASTING: Esperado = ')',"
                                + "obtido = '" + tk.getId() + "'"));
                    }
                } else {
                    return operacaoAritmetica();
                }
            }
            if (tk.getToken().equals(MENOS.name())) {
                valor += "-";
                next();
            }
            if ((ling.isFirst("valor", tk.getId()) || ID_VAR.name().equals(tk.getToken()))
                    && !ling.isFirst("operador_aritmetico", tknext.getId())) {
                saida = new Pair<>(tk.getTipo(), tk.getId());
                tk.setUtilizada(tk.getUtilizada() + 1);
                next();
            } else if (ling.isFirst("operacao_aritmetica", tk.getId())) { // se a = -1;
                saida = operacaoAritmetica();
            } else {
                erros.add(new Erro(tk.getLinha(), "ERRO_ATRIBUICAO: Esperado = "
                        + "{ID_VAR, VALOR, OPERACAO_ARITMETICA}, Obtido = '" + tk.getId() + "'"));
                //pularToken();
                erro = true;
            }
            if (!erro && saida != null) {
                var.setValor(valor + saida.getValue());
                var.setInitialized(true);
                if (!cast.isEmpty()) {
                    saida = new Pair<>(cast, saida.getValue());
                }
                if (var.getTipo() != null && !var.getTipo().isEmpty()
                        && !var.getTipo().equals(prioritType(var.getTipo(), saida.getKey()))) {
                    addWarnningPrecisao(var, saida.getKey());
                }
            }
        }

        return saida != null ? new Pair<>(saida.getKey(), valor + saida.getValue()) : null;
    }

    private void addWarnningPrecisao(Simbolo var, String tipodif) {
        erros.add(new Erro(var.getLinha(), "ATENÇÃO: Possível perda de precisão,"
                + " var '" + var.getId() + "' é do tipo '" + var.getTipo()
                + "' e o resultado é '" + tipodif + "'"));
    }

    private void cmdWhile() {
        next();
        if (!condicao()) {
            procurarBlocoComando();
        }
        blocoComando();
    }

    private void cmdFor() {
        next();
        boolean errocabecalho = false;
        if (ABRE_PARENTESE.name().equals(tk.getToken())) {
            next();
            if (ID_VAR.name().equals(tk.getToken())) { // opcional
                Simbolo v = tk;
                //next();
                Pair<String, String> res = atribuicao();
                if (res == null) {
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
                    if (ID_VAR.name().equals(tk.getToken())) { // opcional
                        //next();
                        if (atribuicao() == null) {
                            errocabecalho = true;
                        }
                    }
                    if (!errocabecalho) {
                        if (FECHA_PARENTESE.name().equals(tk.getToken()) || erroLexico()) {
                            next();
                        } else {
                            erros.add(new Erro(tk.getLinha(), "ERRO_FOR: Esperado = ')' , Obtido = '" + tk.getId() + "'"));
                            errocabecalho = true;
                            pularToken();
                        }
                    }
                }
            }

        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_FOR: Esperado = '(' , Obtido = '" + tk.getId() + "'"));
            errocabecalho = true;
            pularToken();
        }
        if (errocabecalho) {
            procurarBlocoComando();
        }
        blocoComando();

    }

    private void procurarBlocoComando() {
        while (!tk.getToken().equals(ABRE_CHAVE.name())) {
            next();
        }
    }

    private boolean blocoComando() {
        if (tk.getToken().equals(ABRE_CHAVE.name()) || erroLexico()) {
            next();
            cmd();
            if (tk.getToken().equals(FECHA_CHAVE.name()) || erroLexico()) {
                next();
            } else {
                erros.add(new Erro(tk.getLinha(), "ERRO_BLOCO_COMANDO: Esperado = '}', Obtido = '" + tk.getId() + "'"));
                pularToken();
                //
                return false;
            }
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_BLOCO_COMANDO: Esperado = '{', Obtido = '" + tk.getId() + "'"));
            pularToken();

            return false;
        }
        return true;
    }

    private boolean condicao() {
        boolean s = true;
        pularErrosLexicos();
        if (tk.getToken().equals(ABRE_PARENTESE.name())) {
            next();
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_CONDICAO: Esperado = '(', Obtido = '" + tk.getId() + "'"));
            pularToken();
            s = false;
        }
        s &= bool();
        pularErrosLexicos();
        if (tk.getToken().equals(FECHA_PARENTESE.name())) {
            next();
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_CONDICAO: Esperado = ')', Obtido = '" + tk.getId() + "'"));
            pularToken();
            s = false;
        }
        return s;
    }

    private boolean pontoVirgula() {
        if (tk.getToken().equals(PONTO_VIRGULA.name()) || erroLexico()) {
            next();
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_PONTO_VIRGULA: Esperado = ';', Obtido = '" + tk.getId() + "'"));
            pularToken();

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
        if (tk.getToken().equals(ELSE.name())) { // opcional
            next();
            if (tk.getToken().equals(IF.name())) { // opcional
                cmdIf();
            } else {
                blocoComando();
            }
        }
    }

    private void verificaProximoToken() {
        // verificar qual para qual automato ir, de acordo com quem tiver o proximo token como follow
    }

    private boolean cmd() {
        String name = tk.getToken();
        do {
            pularErrosLexicos();
            if (name.equals(ID_VAR.name())) { // atribuicao
                testeDeclarada(tk);
                //next();
                boolean erro = false;
                if (atribuicao() == null) {
                    erro = true;
                } else if (!pontoVirgula()) {
                    erro = true;
                }
                if (erro) {
                    while (!tk.getToken().equals(PONTO_VIRGULA.name()) // para pegar algum outro erro como 2 = 2;
                            && !(ling.isFirst("cmd", tk.getId()) && !tk.getToken().equals(ID_VAR.name()))) {
                        next();
                    }
                    if (tk.getToken().equals(PONTO_VIRGULA.name())) {
                        next();
                    }
                }
            } else if (name.equals(FOR.name())) {
                cmdFor();
            } else if (name.equals(IF.name())) {
                cmdIf();
            } else if (name.equals(WHILE.name())) { // while
                cmdWhile();
            } else { // erro
                erros.add(new Erro(tk.getLinha(), "ERRO_COMANDO: Esperado = "
                        + "{'if','while','for',ID_VAR}, Obtido = '" + tk.getId() + "'"));
                pularToken();
                /*verificaProximoToken();
                while (!ling.isFirst("cmd", tk.getToken()) && !tk.getToken().equals(END.name())
                        && !tk.getToken().equals(FECHA_CHAVE.name())) {
                    next();
                }*/
            }
            name = tk.getToken();
        } while (ling.isFirst("cmd", tk.getId()) && !END.name().equals(name));
        return true;
    }

    private String operadorAritmetico() {
        String op = null + "";
        if (ling.isFirst("operador_aritmetico", tk.getId())) {
            op = tk.getId();
            next();
        } else {

            erros.add(new Erro(tk.getLinha(), "ERRO_OPERADOR_ARITMETICO: Esperado: "
                    + "{'+', '-', '*', '/'}, Obtido = '" + tk.getId() + "'"));
            pularToken();

        }
        return op;
    }

    private String prioritType(String t1, String t2) {
        t1 = t1.toUpperCase();
        t2 = t2.toUpperCase();
        if (t1.equals(EXP.name()) || t2.equals(EXP.name())) {
            return EXP.name().toLowerCase();
        }
        if (t1.equals(DOUBLE.name()) || t2.equals(DOUBLE.name())) {
            return DOUBLE.name().toLowerCase();
        }
        return INT.name().toLowerCase();
    }

    private void testeDeclarada(Simbolo s) {
        if (!s.isDeclared()) {
            erros.add(new Erro(s.getLinha(), "ERRO_OPERACAO_ARITMETICA: '"
                    + s.getId() + "' não declarada"));
            //pularToken();
        }
    }

    private void testeInicializada(Simbolo s) {
        if (!s.isInitialized()) {
            erros.add(new Erro(s.getLinha(), "ERRO_OPERACAO_ARITMETICA: '"
                    + s.getId() + "' não inicializada"));
            //pularToken();
        }
    }

    private Pair<String, String> operacaoAritmetica() {
        Pair<String, String> saida = null;
        String valor = "", tipo = INT.name();
        boolean usarop = true;
        if (ling.isFirst("operacao_aritmetica", tk.getId())) {

            if (tk.getToken().equals(MENOS.name())) {
                saida = operacaoAritmetica();
                return saida == null ? null : new Pair<>(saida.getKey(), "-" + saida.getValue());
            }
            // inicio operando 
            if (ABRE_PARENTESE.name().equals(tk.getToken())) {
                valor += "(";
                next();
                saida = operacaoAritmetica();
                valor += saida.getValue();
                tipo = prioritType(tipo, saida.getKey());
                if (FECHA_PARENTESE.name().equals(tk.getToken()) || erroLexico()) {
                    valor += ")";
                    next();
                } else {
                    erros.add(new Erro(tk.getLinha(), "ERRO_OPERACAO_ARITMETICA: "
                            + "Esperado = ')', Obtido = '" + tk.getId() + "'"));
                    //pularToken();
                }
            } else if (ling.isFirst("valor", tk.getId())) {
                valor += tk.getId();
                tipo = prioritType(tipo, tk.getTipo());
                next();
            } else if (ID_VAR.name().equals(tk.getToken())) {
                testeDeclarada(tk);
                testeInicializada(tk);
                tk.setUtilizada(tk.getUtilizada() + 1);
                valor += tk.getId();
                tipo = prioritType(tipo, tk.getTipo());
                next();
            } else {
                erros.add(new Erro(tk.getLinha(), "ERRO_OPERACAO_ARITMETICA: Esperado = "
                        + "{'(', ID_VAR, VALOR}, Obtido = '" + tk.getId() + "'"));
                //pularToken();
            }
            // fim primeiro

            // parte opcional
            if (ling.isFirst("operador_aritmetico", tk.getId())) {
                valor += tk.getId();
                next();
                saida = operacaoAritmetica();
                tipo = prioritType(tipo, saida.getKey());
                valor += saida.getValue();
            } //else {
            //System.out.println(tk);
            //}
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_OPERACAO_ARITMETICA: Esperado = "
                    + "{ID_VAR, VALOR, ABRE_PARENTES}, obtido = '" + tk.getId() + "'"));
            //pularToken();
        }
        return new Pair<>(tipo, valor);
    }

    private boolean valor() {
        pularErrosLexicos();
        if (ling.isFirst("valor", tk.getId()) || erroLexico()) {
            // valor int, double, ou exp
            next();
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_VALOR: Esperado = "
                    + "{VALOR_INT, VALOR_DOUBLE, VALOR_EXP, OPERACAO_ARITMETICA}, "
                    + "Obtido = '" + tk.getId() + "'"));
            pularToken();
            return false;
        }
        return true;
    }

    private void definirVar() {
        if (tk.getToken().equals(ID_VAR.name())) {
            varRec = tk;
            varRec.setDeclared(true);
            varRec.setTipo(tipoAtual);
            if (tknext.getToken().equals(ATRIBUIR.name())) { // opcional
                Pair<String, String> res = atribuicao();
                varRec.setInitialized(res != null);
            } else { // não inicializada
                next();
            }
            // opcional: novo id
            if (tk.getToken().equals(VIRGULA.name())) {
                next();
                definirVar();
            }
        } else {
            erros.add(new Erro(tk.getLinha(), "'" + tk.getId() + "', não é um id de variavel"));
            pularToken();
        }
    }

    private void cmdVar() {
        pularErrosLexicos();
        if (ling.isFirst("cmd_var", tk.getId())) {
            tipoAtual = tk.getId(); // tipoAtual = int | double | exp
            next();
            definirVar();
            pontoVirgula();
            if (ling.isFirst("cmd_var", tk.getId())) {
                cmdVar();
            }
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_DECLARAÇÃO_VAR: Esperado = "
                    + "{'int', 'double', 'exp'}, Obtido = '" + tk.getId() + "'"));
            pularToken();

        }
    }

    private void begin() {
        if (tk.getToken().equals(BEGIN.name()) || erroLexico()) {
            next();
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_BEGIN: Esperado = 'begin', "
                    + "obtido = '" + tk.getId() + "'"));
            pularToken();

        }
    }

    private void end() {
        if (tk.getToken().equals(END.name()) || erroLexico()) {
            next();
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_END: Esperado = 'end', "
                    + "obtido = '" + tk.getId() + "'"));
            pularToken();

        }
    }

}
