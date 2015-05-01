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
        System.out.println(args[0]);
        LexicoPascal lexico = new LexicoPascal(args[0]);

        lexico.Compila();
    }
    
}
