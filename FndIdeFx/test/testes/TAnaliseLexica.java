/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testes;

import fndidefx.model.AnaliseLexica;
import fndidefx.model.Simbolo;
import fndidefx.model.Token;
import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author fndcaique
 */
public class TAnaliseLexica {

    public TAnaliseLexica() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void testeProgramTeste() {

        String code = "begin\n"
                + "\n"
                + "int a = 3\n"
                + "\n"
                + "while(a < 10){a = a+a}\n"
                + "\n"
                + "\n"
                + "end";
        
        AnaliseLexica al = new AnaliseLexica((code));
        Assert.assertEquals("t_begin",al.nextSimbolo().getTipo());
        Assert.assertEquals("t_int",al.nextSimbolo().getTipo());
        Assert.assertEquals("t_id_var",al.nextSimbolo().getTipo());
        Assert.assertEquals("t_atribuidor",al.nextSimbolo().getTipo());
        Assert.assertEquals("t_valor_int",al.nextSimbolo().getTipo());
        Assert.assertEquals("t_while",al.nextSimbolo().getTipo());
        Assert.assertEquals("t_abre_parenteses",al.nextSimbolo().getTipo());
        Assert.assertEquals("t_id_var",al.nextSimbolo().getTipo());
        Assert.assertEquals("t_menor",al.nextSimbolo().getTipo());
        Assert.assertEquals("t_valor_int",al.nextSimbolo().getTipo());
        Assert.assertEquals("t_fecha_parenteses",al.nextSimbolo().getTipo());
        Assert.assertEquals("t_abre_chaves",al.nextSimbolo().getTipo());
        Assert.assertEquals("t_id_var",al.nextSimbolo().getTipo());
        Assert.assertEquals("t_atribuidor",al.nextSimbolo().getTipo());
        Assert.assertEquals("t_id_var",al.nextSimbolo().getTipo());
        Assert.assertEquals("t_mais",al.nextSimbolo().getTipo());
        Assert.assertEquals("t_id_var",al.nextSimbolo().getTipo());
        Assert.assertEquals("t_fecha_chaves",al.nextSimbolo().getTipo());
        Assert.assertEquals("t_end",al.nextSimbolo().getTipo());
    }
    
    @Test
    public void testeComment(){
        String code = "/* ekryfb71234!@#$%*() */";
        AnaliseLexica ana = new AnaliseLexica(code);
        Simbolo tk = ana.nextSimbolo();
        Assert.assertNotNull(tk);
        Assert.assertEquals("t_abre_comment", tk.getTipo());
    }
}
