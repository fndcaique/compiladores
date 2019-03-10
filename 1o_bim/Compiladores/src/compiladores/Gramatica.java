package compiladores;

/**
 *
 * @author Bruna Kushikawa
 */
public class Gramatica
{
    private int p, p_erro;
    private int linha, col, colAux;
    private String[] token;
    private String erro, ignored;
    private String programa;
    private boolean fim;
    
    public Gramatica()
    {
        token = new String[2];
    }
    
    public String getIgnored()
    {
        return ignored;
    }
    
    private void init(){
        token[0] = "";
        token[1] = "";
        erro="";
        ignored="";
        linha = 1;
        col = 0;
        p = 0;
    }
    
    private void lixo(){
        while(programa.charAt(p) == ' ' || programa.charAt(p) == '\t' || programa.charAt(p) == '\n')
        {
            if(programa.charAt(p) == ' ')
                col++;
            else
            if(programa.charAt(p) == '\t')
                col+=8;
            else
            if(programa.charAt(p) == '\n')
            {
                colAux = col;
                col = 0;
                linha++;
            }
            p++;
        }
    }
    
    private void prox_token(){
        token[1] = "INVALID";
        p_erro = 0;
        String string, tipo = "";
        
        char get, get2;  
        boolean aux = true;
    
        lixo();
        get = programa.charAt(p++); col++;
        get2 = ' ';
        string = get+"";
        
        if(get != '#')
            get2 = programa.charAt(p);
        
        p_erro = 0;
        switch(get)
        {
            case '+':   switch(get2)
                        {
                            case '=':   token[0] = "+=";
                                        token[1] = "INCREMENTO1";
                                        p++; col++; p_erro++;
                                        break;
                            case '+':   token[0] = "++";
                                        token[1] = "INCREMENTO2";
                                        p++; col++; p_erro++;
                                        break;
                            default:    token[0] = "=";
                                        token[1] = "OPERADOR_MAT";
                                        break;
                        }
                        break;
                        
            case '-':   switch(get2)
                        {
                            case '=':   token[0] = "-=";
                                        token[1] = "INCREMENTO1";
                                        p++; col++; p_erro++;
                                        break;
                            case '-':   token[0] = "--";
                                        token[1] = "INCREMENTO2";
                                        p++; col++; p_erro++;
                                        break; 
                            default:    token[0] = "-";
                                        token[1] = "OPERADOR_MAT";
                                        break;
                        }
                        break;
            case '*':   switch(get2)
                        {
                            case '=':   token[0] = "*=";
                                        token[1] = "INCREMENTO1";
                                        p++; col++; p_erro++;
                                        break;
                            default:    token[0] = "*";
                                        token[1] = "OPERADOR_MAT";
                                        break;
                        }
                        break;
            case '/':   switch(get2)
                        {
                            case '=':   token[0] = "/=";
                                        token[1] = "INCREMENTO1";
                                        p++; col++; p_erro++;
                                        break;
                            default:    token[0] = "/";
                                        token[1] = "OPERADOR_MAT";
                                        break;
                        }
                        break;
                        
            case '=':   switch(get2)
                        {
                            case '=':   token[0] = "==";
                                        token[1] = "OPERADOR_REL";
                                        p++; col++; p_erro++;
                                        break;
                            default:    token[0] = "=";
                                        token[1] = "IGUAL";
                                        break;
                        }
                        break;
                        
            case '!':   switch(get2)
                        {
                            case '=':   token[0] = "!=";
                                        token[1] = "OPERADOR_REL";
                                        p++; col++; p_erro++;
                                        break;
                            default:    token[0] = "!";
                                        token[1] = "NOT";
                                        break;
                        }
                        break;
                        
            case '>':   if(get2 == '=') p++; col++; p_erro++;
                        token[0] = ">";
                        token[1] = "OPERADOR_REL";
                        break;
                        
            case '<':   if(get2 == '=') p++; col++; p_erro++;
                        token[0] = "<";
                        token[1] = "OPERADOR_REL";
                        break;
            
            case ';':   token[0] = ";"; token[1] = "PONTO_VIRGULA"; break;
            case '%':   token[0] = "%"; token[1] = "OPERADOR_MAT"; break;
            case '^':   token[0] = "^"; token[1] = "OPERADOR_MAT"; break;
            case ',':   token[0] = ","; token[1] = "VIRGULA"; break;
            case '(':   token[0] = "("; token[1] = "ABRE_P"; break;
            case ')':   token[0] = ")"; token[1] = "FECHA_P"; break;
            case '{':   token[0] = "("; token[1] = "ABRE_C"; break;
            case '}':   token[0] = ")"; token[1] = "FECHA_C"; break;
            case '#':   token[0] = "#"; token[1] = "#"; break;
        }

        if(isLetra(get))
        {
            p--;
            col--;
            string = "";
            while(isLetra(programa.charAt(p)) || isNumero(programa.charAt(p)) || programa.charAt(p) == '_')
            {
                string += programa.charAt(p);
                p++;
                col++;
                p_erro++;
            }
            if(isSimbolo(programa.charAt(p)))
            {
                while(isLetra(programa.charAt(p)) || isNumero(programa.charAt(p)) || programa.charAt(p) == '_' || isSimbolo(programa.charAt(p)))
                {
                    string += programa.charAt(p);
                    p++;
                    col++;
                    p_erro++;
                }
            }
            else
                busca(string);
            if(string.length() > 30)
            {
                token[1]="INVALID";
                tipo="ID muito extenso!";
                aux=false;
            }
        }
        else
        if(isNumero(get))
        {
            p--;
            col--;
            string = "";
            while(isNumero(programa.charAt(p)))
            {
                string += programa.charAt(p);
                p++;
                col++;
                p_erro++;
            }
            if(programa.charAt(p) == '.' && isNumero(programa.charAt(p+1)))
            {
                string += programa.charAt(p++); col++; p_erro++;
                while(isNumero(programa.charAt(p)))
                {
                    string += programa.charAt(p);
                    p++;
                    col++;
                    p_erro++;
                }
            }
            
            if(programa.charAt(p) == 'E')
            {
                string += "E";
                p++;
                if((programa.charAt(p) == '+' || programa.charAt(p) == '-') && isNumero(programa.charAt(p+1)))
                    string += programa.charAt(p++)+"";
                
                if(isNumero(programa.charAt(p)))
                {
                    while(isNumero(programa.charAt(p)))
                    {
                        string += programa.charAt(p);
                        p++;
                        col++;
                        p_erro++;
                    }
                    token[1] = "NUMERO";
                }
                else
                if(isLetra(programa.charAt(p)) || isSimbolo(programa.charAt(p)))
                {
                    while(isLetra(programa.charAt(p)) || isNumero(programa.charAt(p)) || isSimbolo(programa.charAt(p)))
                    {
                        string += programa.charAt(p);
                        p++;
                        col++;
                        p_erro++;
                    }
                }
            }
            else
            if(isLetra(programa.charAt(p)) || isSimbolo(programa.charAt(p)))
            {
                while(isLetra(programa.charAt(p)) || isNumero(programa.charAt(p)) || isSimbolo(programa.charAt(p)))
                {
                    string += programa.charAt(p);
                    p++;
                    col++;
                    p_erro++;
                }
            }
            else
                token[1] = "NUMERO";
            
            if(string.length() > 30)
            {
                token[1]="INVALID";
                tipo="Número muito extenso!";
                aux=false;
            }
        }
        else
        if(get == '"')
        {
            string = "\"";
            while(programa.charAt(p) != '"' && programa.charAt(p) != '\n' && programa.charAt(p) != '#')
            {
                string += programa.charAt(p);
                p++;
                col++;
                p_erro++;
            }
            if(programa.charAt(p) != '"')
            {
                aux = false;
                token[1] = "INVALID";
                tipo="String sem fim!";
            }
            else
            {
                token[1] = "CADEIA";
                string += "\"";
                p++;
                col++;
                p_erro++;
            }
        }
        
        token[0] = string;
        
        if(token[1].equals("INVALID") && aux)
            erro("Lexema inválido: "+string+"! "+tipo,"ERRO LÉXICO");
    }
    
    private void busca(String string){
        switch(string)
        {
            case "begin":   token[1] = "INICIO";    break;
            case "end":     token[1] = "FIM";       break;
            case "number":  token[1] = "TIPO";      break;
            case "string":  token[1] = "TIPO";      break;
            case "if":      token[1] = "IF";        break;
            case "else":    token[1] = "ELSE";      break;
            case "for":     token[1] = "FOR";       break;
            case "while":   token[1] = "WHILE";     break;
            default:        token[1] = "ID";        break;
        }
    }
    
    public String compilar(String program){
        boolean ini = false;
        
        programa = program.trim();
        programa = program+'#';
        init();
        
        prox_token();
        
        if(!token[1].equals("INICIO"))
            erro("'begin' não definido!","");
        else
        {
            ini = true;
            prox_token();
        }
        
        while(!token[1].equals("#"))
        {
            if(FirstFollow.firstCOMANDOS(token[1]))
                comandos();
            else
            if(token[1].equals("FIM"))
            {
                prox_token();
                if(token[1].equals("#"))
                    fim = true;
                else
                    erro("'end' deve ser definido apenas no final do programa!", "");
            }
            else
            if(token[1].equals("INICIO"))
            {
                if(ini)
                    erro("'begin' deve ser definido apenas no inicio do programa!","");
            }
            else
                erro("Lexema \""+token[0]+"\" perdido!","out");
        }
        
        if(!fim)
            erro("'end' não definido!","");
        
        erro+="\n ** Compilado com sucesso! ** \n";
        
        return erro;
    }
    
    private void variaveis()
    {
        if(token[1].equals("TIPO"))
        {
            prox_token();
            if(token[1].equals("ID"))
            {
                prox_token();
                igual_valor();
                continuacao();
                
                if(token[1].equals("PONTO_VIRGULA"))
                    prox_token();
                else
                    erro("Finalize declaração com \";\"!","COMANDOS");
            }
            else
                erro("Declaração incompleta (apenas o tipo foi definido)!", "COMANDOS");
        }
    }
    
    private void comandos()
    {
        if(token[1].equals("TIPO"))
        {
            variaveis();
            comandos();
        }
        else
        if(token[1].equals("ID"))//ATRIBUICAO
        {
            atribuicao();
            comandos();
        }
        else
        if(token[1].equals("IF"))
        {
            condicao();
            comandos();
        }
        else
        if(token[1].equals("FOR") || token[1].equals("WHILE"))
        {
            repeticao();
            comandos();
        }
    }
    
    private void continuacao()
    {
        if(token[1].equals("VIRGULA"))
        {
            prox_token();
            if(token[1].equals("ID"))
            {
                prox_token();
                igual_valor();
                continuacao();
            }
            else
               erro("Vírgulas devem ser utilizadas para continuar declarando variáveis de mesmo tipo!","COMANDOS");
        }
    }
    
    private void igual_valor()
    {
        if(token[1].equals("IGUAL"))
        {
            prox_token();
            valor();
        }
    }
    
    private void atribuicao()
    {
        String oi;
        if(token[1].equals("ID"))
        {
            oi = token[0];
            prox_token();
            if(token[1].equals("IGUAL"))
            {
                prox_token();
                valor();
                
                if(token[1].equals("PONTO_VIRGULA"))
                    prox_token();
                else
                    erro("Atribuições devem ser finalizadas com \";\"!","COMANDOS");
            }
            else
                erro("ID \""+oi+"\"sem comando associado!","COMANDOS");
        }
        else
            erro("Atribuições seguem a estrutura \"X = 2;\". Informe um ID!","COMANDOS");
    }
    
    private void valor()
    {
        if(token[1].equals("CADEIA"))
            prox_token();
        else
        if(FirstFollow.firstEXPRESSAO_MAT(token))
            expressao_mat();
        else
            erro("Valor não definido","VALOR");
    }
    
    private void expressao_mat()
    {
        if(token[1].equals("ABRE_P"))
        {
            prox_token();
            exp_mat();
            if(token[1].equals("FECHA_P"))
                prox_token();
            else
                erro("Má formação de expressão! Feche Parênteses!","VALOR");
        }
        else
            exp_mat();
    }
    
    private void exp_mat()
    {
        if(token[0].equals("-"))
            prox_token();
        var();
        conta();
    }
    
    private void var()
    {
        if(token[1].equals("ID") || token[1].equals("NUMERO"))
            prox_token();
        else
            erro("Valor incorreto! Defina um ID ou um número!","VALOR");
    }
    
    private void conta()
    {
        if(token[1].equals("OPERADOR_MAT"))
        {
            prox_token();
            expressao_mat();
        }
    }
    
    private void condicao()
    {
        if(token[1].equals("IF"))
        {
            prox_token();
            if(token[1].equals("ABRE_P"))
            {
                prox_token();
                verifica();
                
                if(token[1].equals("FECHA_P"))
                {
                    prox_token();
                    entre();
                    ELSE();
                }
                else
                    erro("Necessário fechar parenteses!","");
            }
            else
                erro("Necessário abrir parenteses!","VALOR");
        }
    }
    
    private void ELSE()
    {
        if(token[1].equals("ELSE"))
        {
            prox_token();
            entre();
        }
    }
    
    private void entre()
    {
        if(token[1].equals("ABRE_C"))
        {
            prox_token();
            comandos();
            
            if(token[1].equals("FECHA_C"))
                prox_token();
            else
                erro("Necessário fechar chave!","ENTRE");
        }
        else
            erro("Necessário abrir chave!","ENTRE");
    }
    
    private void verifica()
    {
        if(token[1].equals("NOT"))
            prox_token();
        
        valor();
        if(token[1].equals("OPERADOR_REL"))
        {
            prox_token();
            valor();
        }
        else
            erro("É necessário um operador relacional para realizar a verificação!","VALOR");
    }
    
    private void repeticao()
    {
        if(token[1].equals("FOR"))
            FOR();

        if(token[1].equals("WHILE"))
            WHILE();
    }
    
    private void FOR()
    {
        if(token[1].equals("FOR"))
        {
            prox_token();
            if(token[1].equals("ABRE_P"))
            {
                prox_token();
                if(token[1].equals("ID"))
                {
                    prox_token();
                    if(token[1].equals("IGUAL"))
                    {
                        prox_token();
                        expressao_mat();
                        if(token[1].equals("PONTO_VIRGULA"))
                        {
                            prox_token();
                            verifica();
                            if(token[1].equals("PONTO_VIRGULA"))
                            {
                                prox_token();
                                passo();
                                if(token[1].equals("FECHA_P"))
                                {
                                    prox_token();
                                    entre();
                                }
                                else
                                    erro("Necessário fechar parenteses!","");
                            }
                            else
                                erro("Necessário \";\"!","");
                        }
                        else
                            erro("Necessário \";\"!","");
                    }
                    else
                        erro("Necessário atribuição!","");
                }
                else
                    erro("Necessário informar id!","");
            }
            else
                erro("Necessário abrir parenteses!","");
        }
    }
    
    private void passo()
    {
        if(token[1].equals("ID"))
        {
            prox_token();
            if(token[1].equals("INCREMENTO1"))
            {
                prox_token();
                expressao_mat();
            }
            else
            if(token[1].equals("INCREMENTO2"))
                prox_token();
            else
                erro("Necessário informar incremento!","");
        }
        else
            erro("Necessário informar id","");
    }
    
    private void WHILE()
    {
        if(token[1].equals("WHILE"))
        {
            prox_token();
            if(token[1].equals("ABRE_P"))
            {
                prox_token();
                verifica();
                if(token[1].equals("FECHA_P"))
                {
                    prox_token();
                    entre();
                }
                else
                    erro("Necessário fechar parenteses!","COMANDOS");
            }
            else
                erro("Necessario abrir parenteses!","");
        }
    }
    
    private boolean isSimbolo(char i) {
        return i == '.' || i == 34 || i == 36 || (i > 37 && i < 40) || i == 58 || (i > 62 && i < 65) || (i > 90 && i < 94)  || (i > 94 && i < 97) || (i == 124) || (i == 126);
    }
        
    private boolean isNumero(char i){
        return i > 47 && i < 58;
    }
    
    private boolean isLetra(char i){
        return (i > 64 && i < 91) || (i > 96 && i < 123);
    }

    private void erro(String e, String tipo)
    {
        String follow = tipo, aux, aux2;
        if(tipo.equals("ERRO LÉXICO"))
        {
            erro += "ERRO LÉXICO [linha "+linha+", coluna "+(col-p_erro)+"]: "+e+"\n";
            return;
        }
        
        aux = "ERRO SINTÁTICO [linha "+linha+", coluna "+(col-p_erro)+"]: "+e+"\n";
        erro+=aux;
        
        if(tipo.equals("out"))
        {
            prox_token();
            return;
        }
        
        while(!token[1].equals("#"))
        {
            if(follow.equals("VALOR") && FirstFollow.followVALOR(token[1]))
            {
                return;
            }
            else
            if(follow.equals("ENTRE") && FirstFollow.followENTRE(token[1]))
            {
                return;
            }
            else
            if(follow.equals("VERIFICA") && FirstFollow.followVERIFICA(token[1]))
            {
                return;
            }
            else
            if(FirstFollow.firstCOMANDOS(token[1]))
            {
                comandos();
                return;
            }
            else
            if(token[1].equals("INICIO"))
            {
                aux2="ERRO SINTÁTICO [linha "+linha+", coluna "+(col-p_erro)+"]: 'begin' deve ser definido apenas no inicio do programa!\n";
                if(!aux.equals(aux2))
                    erro+=aux2;
            }
            else
            if(token[1].equals("FIM"))
            {
                prox_token();
                if(!token[1].equals("#"))
                {
                    aux2="ERRO SINTÁTICO [linha "+linha+", coluna "+(col-p_erro)+"]: 'end' deve ser definido apenas no final do programa!\n";
                    if(!aux.equals(aux2))
                        erro+=aux2;
                }
                else
                    fim = true;
            }
            else
            {
                //if(token[1].equals("ABRE_C") || token[1].equals("ABRE_P") || token[1].equals("FECHA_C") || token[1].equals("FECHA_P") || token[1].equals("PONTO_VIRGULA"))
                {
                    //aux2="ERRO SINTÁTICO [linha "+linha+", coluna "+(col-p_erro)+"]: Lexema \""+token[0]+"\" perdido!\n";
                    ignored+="ERRO SINTÁTICO [linha "+linha+", coluna "+(col-p_erro)+"]: Lexema \""+token[0]+"\" perdido!\n";
                    //if(!aux.equals(aux2))
                        //erro+=aux2;
                }
            }
            
            if(!fim)
                prox_token();
        }
    }
}