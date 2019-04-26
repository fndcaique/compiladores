/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fndidefx.model;

import static fndidefx.model.Token.TokenType.*;
import java.util.ArrayList;

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
    private int proximo, anterior;
    private Linguagem ling;
    private TabelaSimbolos tablesimb;
    private Simbolo rec;

    public AnaliseSintatica(String code) {
        tablesimb = new TabelaSimbolos();
        anterior = -1;
        proximo = 0;
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
        tknext = arrtokens.get(proximo);

        erros = new ArrayList<>();

    }

    private void next() {
        arrtokens.add(analex.nextSimbolo());
        tkprevius = tk;
        tk = tknext;
        anterior++;
        proximo++;
        tknext = proximo < arrtokens.size() ? arrtokens.get(proximo) : null;
        if (tablesimb.search(tk) != null) { // já utilizada
            if (tk.getToken().equals(ID_VAR.name())) {
                //tk.setUtilizada(tk.getUtilizada() + 1);
            }
        } else {
            tablesimb.add(tk);
        }
    }

    private void previus() {
        tknext = tk;
        proximo--;
        tk = tkprevius;
        anterior--;
        tkprevius = anterior >= 0 ? arrtokens.get(anterior) : null;

    }

    private void pularComando() {
        if (!(ling.isFirst("tipo_var", tk.getToken()) || ling.isFirst("valor", tk.getToken()) || ling.isFirst("cmd", tk.getToken()))) {
            next();
        }
    }

    private boolean erroLexico() {
        return tk.getToken().equals(ERRO_LEXICO.name());
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

    public ArrayList<Erro> start() {
        next();
        begin();
        cmdVar();
        cmd();
        end();
        return join(analex.getErros(), erros);
    }

    private boolean idVar() {
        if (tk.getToken().equals(ID_VAR.name()) || erroLexico()) {
            next();
        } else {
            erros.add(new Erro(tk.getLinha(), "'" + tk.getId() + "', não é um id de variavel"));
            return false;
        }
        return true;

    }

    private boolean atribuidor() {
        if (tk.getToken().equals(ATRIBUIR.name()) || erroLexico()) {
            next();
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_ATRIBUIR: Esperado = '=', Obtido = '" + tk.getId() + "'"));

            return false;
        }
        return true;
    }

    private boolean bool() {
        if (ling.isFirst("bool", tk.getToken()) || erroLexico()) {
            if (tk.getToken().equals(NOT.name()) || erroLexico()) {
                next();
                if (!bool()) {
                    return false;
                }
            } else if (tk.getToken().equals(ABRE_PARENTESE.name()) || erroLexico()) {
                next();
                if (bool()) {
                    if (tk.getToken().equals(FECHA_PARENTESE.name()) || erroLexico()) {
                        next();
                    } else {
                        erros.add(new Erro(tk.getLinha(), "ERRO_BOOL: Esperado = ')', Obtido = " + tk.getId()));

                    }
                } else {
                    return false;
                }
            } else { // id_var ou valor
                if (tk.getToken().equals(ID_VAR)) {
                    //tk.setUtilizada(tk.getUtilizada() + 1);
                }
                if (ling.isFirst("operador_relacional", tknext.getToken())) {
                    next();
                    if (ling.isFirst("operador_relacional", tk.getToken()) || erroLexico()) {
                        next();
                    } else {
                        erros.add(new Erro(tk.getLinha(),
                                "ERRO_OPERADOR_RELACIONAL: "
                                + "Esperado = {'<', '>', '==', '<=', '>=', '!='}, Obtido = " + tk.getId()));

                        return false;
                    }
                } else if (ling.isFirst("operador_aritmetico", tknext.getToken())) {
                    operacaoAritmetica();
                }
                if (ling.isFirst("valor", tk.getToken()) || tk.getToken().equals(ID_VAR.name()) || erroLexico()) {
                    next();
                } else {
                    erros.add(new Erro(tk.getLinha(), "ERRO_BOOL: Esperado = {ID_VALOR, VALOR}, Obtido = " + tk.getId()));

                    return false;
                }
            }
            if (ling.isFirst("and_or", tk.getToken())) {
                next();
                bool();
            }
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_BOOL: Esperado = {ID_VAR, VALOR, '!', '('}, Obtido = " + tk.getId()));

            return false;
        }
        return true;
    }

    private String atribuicao() {
        String saida = "";
        rec = tk;
        next();
        if (atribuidor()) {
            if (ling.isFirst("operador_aritmetico", tknext.getToken())) {
                saida = operacaoAritmetica();
            } else if (ling.isFirst("valor", tk.getToken()) || ID_VAR.name().equals(tk.getToken())) {
                next();
            } else {
                erros.add(new Erro(tk.getLinha(), "ERRO_ATRIBUICAO: Esperado = {ID_VAR, VALOR}, Obtido = " + tk.getId()));
            }
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_ATRIBUICAO: Esperado = '=', Obtido = " + tk.getId()));
        }
        return saida;
    }

    private void cmdFor() {
        if (FOR.name().equals(tk.getToken())) {
            next();
            boolean errocabecalho = false;
            if (ABRE_PARENTESE.name().equals(tk.getToken()) || erroLexico()) {
                next();
                if (ID_VAR.name().equals(tk.getToken())) { // opcional
                    if (atribuicao().isEmpty()) {
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
                            if (atribuicao().isEmpty()) {
                                errocabecalho = true;
                            }
                        }
                        if (!errocabecalho) {
                            if (FECHA_PARENTESE.name().equals(tk.getToken()) || erroLexico()) {
                                next();
                            } else {
                                erros.add(new Erro(tk.getLinha(), "ERRO_FOR: Esperado = ')' , Obtido = " + tk.getId()));
                                errocabecalho = true;
                            }
                        }
                    }
                }

            } else {
                erros.add(new Erro(tk.getLinha(), "ERRO_FOR: Esperado = '(' , Obtido = " + tk.getId()));
                errocabecalho = true;
            }
            if (errocabecalho) {
                procurarBlocoComando();
            }
            blocoComando();
        }
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
                erros.add(new Erro(tk.getLinha(), "ERRO_BLOCO_COMANDO: Esperado = '}', Obtido = " + tk.getId()));
                //
                return false;
            }
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_BLOCO_COMANDO: Esperado = '{', Obtido = " + tk.getId()));

            return false;
        }
        return true;
    }

    private boolean condicao() {
        if (tk.getToken().equals(ABRE_PARENTESE.name()) || erroLexico()) {
            next();
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_CONDICAO: Esperado = '(', Obtido = " + tk.getId()));

            return false;
        }
        if (bool()) {
            if (tk.getToken().equals(FECHA_PARENTESE.name()) || erroLexico()) {
                next();
            } else {
                erros.add(new Erro(tk.getLinha(), "ERRO_CONDICAO: Esperado = ')', Obtido = " + tk.getId()));

                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    private boolean pontoVirgula() {
        if (tk.getToken().equals(PONTO_VIRGULA.name()) || erroLexico()) {
            next();
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_PONTO_VIRGULA: Esperado = ';', Obtido = " + tk.getId()));

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
            if (erroLexico() || tk.getToken().equals(PONTO_VIRGULA.name())) {
                while (erroLexico()) {
                    next();
                }
            } else if (name.equals(ID_VAR.name())) { // atribuicao
                boolean erro = false;
                if (atribuicao().isEmpty()) {
                    erro = true;
                } else if (!pontoVirgula()) {
                    erro = true;
                }
                if (erro) {
                    while (!tk.getToken().equals(PONTO_VIRGULA.name()) // para pegar algum outro erro como 2 = 2;
                            && !(ling.isFirst("cmd", tk.getToken()) && !tk.getToken().equals(ID_VAR.name()))) {
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
                next();
                if (!condicao()) {
                    procurarBlocoComando();
                }
                blocoComando();
            } else { // erro
                erros.add(new Erro(tk.getLinha(), "ERRO_COMANDO: Esperado = {'if','while','for',ID_VAR}, Obtido = '" + tk.getId() + "'"));
                verificaProximoToken();
                while (!ling.isFirst("cmd", tk.getToken()) && !tk.getToken().equals(END.name())
                        && !tk.getToken().equals(FECHA_CHAVE.name())) {
                    next();
                }
            }
            name = tk.getToken();
        } while (ling.isFirst("cmd", name));
        return true;
    }

    private void pontoConfianca() {
        do {
            if (tk.getToken().equals(ID_VAR.name()) && !tknext.getToken().equals(ATRIBUIR.name())) {
                next();
            }
            while (!(ling.isFirst("cmd", tk.getToken()) || END.name().equals(tk.getToken())
                    || ABRE_CHAVE.name().equals(tk.getToken()) || FECHA_CHAVE.name().equals(tk.getToken()))) {
                next();
            }
            System.out.println("pontoConfianca = " + tk.getId() + ", " + tk.getToken());
        } while (!END.name().equals(tk.getToken()) && tk.getToken().equals(ID_VAR.name()) && !tknext.getToken().equals(ATRIBUIR.name()));

    }

    private void operadorAritmetico() {
        if (ling.isFirst("operador_aritmetico", tk.getToken())) {
            next();
        } else {

            erros.add(new Erro(tk.getLinha(), "ERRO_OPERADOR_ARITMETIC0: Esperado: {'+', '-', '*', '/'}, Obtido = " + tk.getId()));

        }
    }

    private String operacaoAritmetica() {
        String saida = "";
        if (ling.isFirst("operacao_aritmetica", tk.getToken()) || erroLexico()) {

            if (ABRE_PARENTESE.name().equals(tk.getToken())) {
                next();
                saida = operacaoAritmetica();
                if (FECHA_PARENTESE.name().equals(tk.getToken()) || erroLexico()) {
                    next();
                } else {
                    erros.add(new Erro(tk.getLinha(), "ERRO_OPERACAO_ARITMETICA: Esperado = ')', Obtido = " + tk.getId()));
                }
            } else if (ling.isFirst("valor", tk.getToken())) {
                next();
            } else if (ID_VAR.name().equals(tk.getToken())) {
                //if () {

                //}
                next();
            } else {
                erros.add(new Erro(tk.getLinha(), "ERRO_OPERACAO_ARITMETICA: Esperado = {'(', ID_VAR, VALOR}, Obtido = " + tk.getId()));
            }
            operadorAritmetico();
            if (ABRE_PARENTESE.name().equals(tk.getToken())) {
                next();
                saida = operacaoAritmetica();
                if (FECHA_PARENTESE.name().equals(tk.getToken()) || erroLexico()) {
                    next();
                } else {
                    erros.add(new Erro(tk.getLinha(), "ERRO_OPERACAO_ARITMETICA: Esperado = ')', Obtido = " + tk.getId()));
                }
            } else if (ling.isFirst("valor", tk.getToken()) || ID_VAR.name().equals(tk.getToken())) {
                next();
            } else {
                erros.add(new Erro(tk.getLinha(), "ERRO_OPERACAO_ARITMETICA: Esperado = {'(', ID_VAR, VALOR}, Obtido = " + tk.getId()));
            }
            // parte opcional
            if (ling.isFirst("operador_aritmetico", tk.getToken())) {
                next();
                if (ling.isFirst("operador_aritmetico", tknext.getToken()) || ABRE_PARENTESE.name().equals(tknext.getToken())) {
                    saida = operacaoAritmetica();
                } else if (ling.isFirst("valor", tk.getToken()) || ID_VAR.name().equals(tk.getToken())) {
                    next();
                } else {
                    erros.add(new Erro(tk.getLinha(), "ERRO_OPERACAO_ARITMETICA: Esperado = {'(', ID_VAR, VALOR}, Obtido = " + tk.getId()));
                }
            }
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_OPERACAO_ARITMETICA: Esperado = {ID_VAR, VALOR, ABRE_PARENTES}, Obtido = " + tk.getId()));
        }
        return saida;
    }

    private boolean valor() {
        if (ling.isFirst("valor", tk.getToken()) || erroLexico()) {
            // valor int, double, ou exp
            next();
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_VALOR: Esperado = {VALOR_INT, VALOR_DOUBLE, VALOR_EXP, ABRE_PARENTESE}, Obtido = " + tk.getId()));
            return false;
        }
        return true;
    }

    private void definirVar() {
        if (idVar()) {
            Simbolo s = tk;
            //s.setDeclarada(true);
            next();
            // opcional: inicializacao
            if (tk.getToken().equals(ATRIBUIR.name()) || erroLexico()) {
                next();
                if (valor()) {
                    //s.setInicializada(true);
                }
            }
            // opcional: novo id
            if (tk.getToken().equals(VIRGULA.name()) || erroLexico()) {
                next();
                definirVar();
            }
        }
    }

    private void cmdVar() {
        if (ling.isFirst("cmd_var", tk.getToken())) {
            next();
            definirVar();
            pontoVirgula();
            if (ling.isFirst("cmd_var", tk.getToken())) {
                cmdVar();
            }
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_DECLARAÇÃO_VAR: Esperado ={'int', 'double', 'exp'}\nObtido = '" + tk.getId() + "'"));

        }
    }

    private void begin() {
        if (tk.getToken().equals(BEGIN.name())) {
            next();
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_BEGIN: Esperado = 'begin'\nObtido = \'" + tk.getId() + "'"));

        }
    }

    private void end() {
        if (tk.getToken().equals(END.name()) || erroLexico()) {
            next();
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_END: Esperado = 'end'\nObtido = " + tk.getId()));

        }
    }

}
