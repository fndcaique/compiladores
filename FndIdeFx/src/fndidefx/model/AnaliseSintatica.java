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

    public AnaliseSintatica(String code) {
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

    public TabelaSimbolos getTabelaSimbolos(){
        return ling.getTabelaSimbolos();
    }

    private void next() {
        arrtokens.add(analex.nextSimbolo());
        tkprevius = tk;
        tk = tknext;
        anterior++;
        proximo++;
        tknext = proximo < arrtokens.size() ? arrtokens.get(proximo) : null;
        
        ling.getTabelaSimbolos().add(tk);
    }

    private void previus() {
        tknext = tk;
        proximo--;
        tk = tkprevius;
        anterior--;
        tkprevius = anterior >= 0 ? arrtokens.get(anterior) : null;

    }

    private void pularComando() {
        if (!(ling.isFirst("tipo_var", tk.getTipo()) || ling.isFirst("valor", tk.getTipo()) || ling.isFirst("cmd", tk.getTipo()))) {
            next();
        }
    }

    private boolean erroLexico() {
        return tk.getTipo().equals(ERRO_LEXICO.name());
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
        if (tk.getTipo().equals(ID_VAR.name()) || erroLexico()) {
            next();
        } else {
            erros.add(new Erro(tk.getLinha(), "'" + tk.getId() + "', não é um id de variavel"));
            return false;
        }
        return true;

    }

    private boolean atribuidor() {
        if (tk.getTipo().equals(ATRIBUIR.name()) || erroLexico()) {
            next();
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_ATRIBUIR: Esperado = '=', Obtido = '" + tk.getId() + "'"));

            return false;
        }
        return true;
    }

    private boolean bool() {
        if (ling.isFirst("bool", tk.getTipo()) || erroLexico()) {
            if (tk.getTipo().equals(NOT.name()) || erroLexico()) {
                next();
                if (!bool()) {
                    return false;
                }
            } else if (tk.getTipo().equals(ABRE_PARENTESE.name()) || erroLexico()) {
                next();
                if (bool()) {
                    if (tk.getTipo().equals(FECHA_PARENTESE.name()) || erroLexico()) {
                        next();
                    } else {
                        erros.add(new Erro(tk.getLinha(), "ERRO_BOOL: Esperado = ')', Obtido = " + tk.getId()));

                    }
                } else {
                    return false;
                }
            } else { // id_var ou valor
                if (ling.isFirst("operador_relacional", tknext.getTipo())) {
                    next();
                    if (ling.isFirst("operador_relacional", tk.getTipo()) || erroLexico()) {
                        next();
                    } else {
                        erros.add(new Erro(tk.getLinha(),
                                "ERRO_OPERADOR_RELACIONAL: "
                                + "Esperado = {'<', '>', '==', '<=', '>=', '!='}, Obtido = " + tk.getId()));

                        return false;
                    }
                } else if (ling.isFirst("operador_aritmetico", tknext.getTipo())) {
                    operacaoAritmetica();
                }
                if (ling.isFirst("valor", tk.getTipo()) || tk.getTipo().equals(ID_VAR.name()) || erroLexico()) {
                    next();
                } else {
                    erros.add(new Erro(tk.getLinha(), "ERRO_BOOL: Esperado = {ID_VALOR, VALOR}, Obtido = " + tk.getId()));

                    return false;
                }
            }
            if (ling.isFirst("and_or", tk.getTipo())) {
                next();
                bool();
            }
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_BOOL: Esperado = {ID_VAR, VALOR, '!', '('}, Obtido = " + tk.getId()));

            return false;
        }
        return true;
    }

    private boolean atribuicao() {
        next();
        if (atribuidor()) {
            if (ling.isFirst("operador_aritmetico", tknext.getTipo())) {
                if (!operacaoAritmetica()) {
                    return false;
                }
            } else if (ling.isFirst("valor", tk.getTipo()) || ID_VAR.name().equals(tk.getTipo())) {
                next();
            } else {
                erros.add(new Erro(tk.getLinha(), "ERRO_ATRIBUICAO: Esperado = {ID_VAR, VALOR}, Obtido = " + tk.getId()));
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    private void cmdFor() {
        if (FOR.name().equals(tk.getTipo())) {
            next();
            boolean errocabecalho = false;
            if (ABRE_PARENTESE.name().equals(tk.getTipo()) || erroLexico()) {
                next();
                if (ID_VAR.name().equals(tk.getTipo())) { // opcional
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
                        if (ID_VAR.name().equals(tk.getTipo())) { // opcional
                            if (!atribuicao()) {
                                errocabecalho = true;
                            }
                        }
                        if (!errocabecalho) {
                            if (FECHA_PARENTESE.name().equals(tk.getTipo()) || erroLexico()) {
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
        while (!tk.getTipo().equals(ABRE_CHAVE.name())) {
            next();
        }
    }

    private boolean blocoComando() {
        if (tk.getTipo().equals(ABRE_CHAVE.name()) || erroLexico()) {
            next();
            cmd();
            if (tk.getTipo().equals(FECHA_CHAVE.name()) || erroLexico()) {
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
        if (tk.getTipo().equals(ABRE_PARENTESE.name()) || erroLexico()) {
            next();
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_CONDICAO: Esperado = '(', Obtido = " + tk.getId()));

            return false;
        }
        if (bool()) {
            if (tk.getTipo().equals(FECHA_PARENTESE.name()) || erroLexico()) {
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
        if (tk.getTipo().equals(PONTO_VIRGULA.name()) || erroLexico()) {
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
        if (tk.getTipo().equals(ELSE.name())) { // opcional
            next();
            if (tk.getTipo().equals(IF.name())) { // opcional
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
        String name = tk.getTipo();
        do {
            if (erroLexico() || tk.getTipo().equals(PONTO_VIRGULA.name())) {
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
                    while (!tk.getTipo().equals(PONTO_VIRGULA.name()) // para pegar algum outro erro como 2 = 2;
                            && !(ling.isFirst("cmd", tk.getTipo()) && !tk.getTipo().equals(ID_VAR.name()))) {
                        next();
                    }
                    if (tk.getTipo().equals(PONTO_VIRGULA.name())) {
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
                while (!ling.isFirst("cmd", tk.getTipo()) && !tk.getTipo().equals(END.name()) &&
                        !tk.getTipo().equals(FECHA_CHAVE.name())){
                    next();
                }
            }
            name = tk.getTipo();
        } while (ling.isFirst("cmd", name));
        return true;
    }

    private void pontoConfianca() {
        do {
            if (tk.getTipo().equals(ID_VAR.name()) && !tknext.getTipo().equals(ATRIBUIR.name())) {
                next();
            }
            while (!(ling.isFirst("cmd", tk.getTipo()) || END.name().equals(tk.getTipo())
                    || ABRE_CHAVE.name().equals(tk.getTipo()) || FECHA_CHAVE.name().equals(tk.getTipo()))) {
                next();
            }
            System.out.println("pontoConfianca = " + tk.getId() + ", " + tk.getTipo());
        } while (!END.name().equals(tk.getTipo()) && tk.getTipo().equals(ID_VAR.name()) && !tknext.getTipo().equals(ATRIBUIR.name()));

    }

    private void operadorAritmetico() {
        if (ling.isFirst("operador_aritmetico", tk.getTipo())) {
            next();
        } else {

            erros.add(new Erro(tk.getLinha(), "ERRO_OPERADOR_ARITMETIC0: Esperado: {'+', '-', '*', '/'}, Obtido = " + tk.getId()));

        }
    }

    private boolean operacaoAritmetica() {
        if (ling.isFirst("operacao_aritmetica", tk.getTipo()) || erroLexico()) {

            if (ABRE_PARENTESE.name().equals(tk.getTipo())) {
                next();
                if (!operacaoAritmetica()) {
                    return false;
                }
                if (FECHA_PARENTESE.name().equals(tk.getTipo()) || erroLexico()) {
                    next();
                } else {
                    erros.add(new Erro(tk.getLinha(), "ERRO_OPERACAO_ARITMETICA: Esperado = ')', Obtido = " + tk.getId()));

                    return false;
                }
            } else if (ling.isFirst("valor", tk.getTipo()) || ID_VAR.name().equals(tk.getTipo())) {
                next();
            } else {
                erros.add(new Erro(tk.getLinha(), "ERRO_OPERACAO_ARITMETICA: Esperado = {'(', ID_VAR, VALOR}, Obtido = " + tk.getId()));

                return false;
            }
            operadorAritmetico();
            if (ABRE_PARENTESE.name().equals(tk.getTipo())) {
                next();
                if (!operacaoAritmetica()) {
                    return false;
                }
                if (FECHA_PARENTESE.name().equals(tk.getTipo()) || erroLexico()) {
                    next();
                } else {
                    erros.add(new Erro(tk.getLinha(), "ERRO_OPERACAO_ARITMETICA: Esperado = ')', Obtido = " + tk.getId()));

                    return false;
                }
            } else if (ling.isFirst("valor", tk.getTipo()) || ID_VAR.name().equals(tk.getTipo())) {
                next();
            } else {
                erros.add(new Erro(tk.getLinha(), "ERRO_OPERACAO_ARITMETICA: Esperado = {'(', ID_VAR, VALOR}, Obtido = " + tk.getId()));

                return false;
            }
            // parte opcional
            if (ling.isFirst("operador_aritmetico", tk.getTipo())) {
                next();
                if (ling.isFirst("operador_aritmetico", tknext.getTipo()) || ABRE_PARENTESE.name().equals(tknext.getTipo())) {
                    if (!operacaoAritmetica()) {
                        return false;
                    }
                } else if (ling.isFirst("valor", tk.getTipo()) || ID_VAR.name().equals(tk.getTipo())) {
                    next();
                } else {
                    erros.add(new Erro(tk.getLinha(), "ERRO_OPERACAO_ARITMETICA: Esperado = {'(', ID_VAR, VALOR}, Obtido = " + tk.getId()));

                    return false;
                }
            }
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_OPERACAO_ARITMETICA: Esperado = {ID_VAR, VALOR, ABRE_PARENTES}, Obtido = " + tk.getId()));

            return false;
        }
        return true;
    }

    private boolean valor() {
        if (ling.isFirst("valor", tk.getTipo()) || erroLexico()) {
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
            // parte opcional inicializacao
            if (tk.getTipo().equals(ATRIBUIR.name()) || erroLexico()) {
                next();
                valor();
            }
            // opcional, novo id
            if (tk.getTipo().equals(VIRGULA.name()) || erroLexico()) {
                next();
                definirVar();
            }
        }
    }

    private void cmdVar() {
        if (ling.isFirst("cmd_var", tk.getTipo())) {
            next();
            definirVar();
            pontoVirgula();
            if (ling.isFirst("cmd_var", tk.getTipo())) {
                cmdVar();
            }
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_DECLARAÇÃO_VAR: Esperado ={'int', 'double', 'exp'}\nObtido = '" + tk.getId() + "'"));

        }
    }

    private void begin() {
        if (tk.getTipo().equals(BEGIN.name())) {
            next();
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_BEGIN: Esperado = 'begin'\nObtido = \'" + tk.getId() + "'"));

        }
    }

    private void end() {
        if (tk.getTipo().equals(END.name()) || erroLexico()) {
            next();
        } else {
            erros.add(new Erro(tk.getLinha(), "ERRO_END: Esperado = 'end'\nObtido = " + tk.getId()));

        }
    }

}
