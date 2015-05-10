/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiladores;

/**
 *
 * @author Neto
 */
public class Compiladores {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String tabelaLexico = null;
        
        LexicoPascal lexico = new LexicoPascal(args[0]);

        tabelaLexico = lexico.Compila();
        
        SintaticoPascal sintatico = new SintaticoPascal("TabelaLexico.txt", true);
        
        sintatico.Compila();
    }
    
}
