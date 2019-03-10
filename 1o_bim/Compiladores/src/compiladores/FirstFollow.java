package compiladores;

public class FirstFollow
{
    public static boolean firstCOMANDOS(String lexema)
    {
        return lexema.equals("FOR") || lexema.equals("WHILE") || lexema.equals("ID") || lexema.equals("IF") || lexema.equals("TIPO");
    }
    public static boolean firstEXPRESSAO_MAT(String[] lexema)
    {
        return lexema[0].equals("-") || lexema[1].equals("ABRE_P") || lexema[1].equals("ID") || lexema[1].equals("NUMERO");
    }
    
    
    
    public static boolean followVALOR(String lexema)
    {
        return lexema.equals("VIRGULA") || lexema.equals("PONTO_VIRGULA") || lexema.equals("FECHA_P") || lexema.equals("OPERADOR_REL")|| lexema.equals("OPERADOR_MAT");
    }
    public static boolean followENTRE(String lexema)
    {
        return lexema.equals("ELSE") || lexema.equals("NUMERO") || lexema.equals("CADEIA") || lexema.equals("FECHA_C");
    }
    public static boolean followVERIFICA(String lexema)
    {
        return lexema.equals("FECHA_P");
    }
}