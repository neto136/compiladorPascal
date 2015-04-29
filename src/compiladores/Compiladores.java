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
        String arquivo, programa;
        TrataArquivos tratador = new TrataArquivos();
        
        programa = tratador.LeArquivo(args[0]);
        tratador.SalvaArquivo(programa);
        
        System.out.print(programa);
    }
    
}
