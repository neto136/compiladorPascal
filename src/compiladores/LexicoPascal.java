/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiladores;

/**
 * Compilador Léxico para compilação de programas escritos na linguagem de
 * programação PASCAL.
 * <p> Projeto da cadeira de Construção de Compiladores na UFPB, período 2015.1
 * 
 * @author Neto
 */
public class LexicoPascal extends TrataArquivos{
    private String arquivo, programa, tabela;
    private int qtdeLinhas;
    // Expressões regulares
    // Variáveis de classe (static) e que não serão modificadas (final)
    private static final String erLetras = "[a-zA-Z]";
    private static final String erNums = "[0-9]";
    private static final String erIdent = "[\\w]"; //[a-zA-Z_0-9]
    private static final String erDelim = "[.,:;()]";
    private static final String erAtrib = ":=";
    private static final String erOpRel = "[=><]|<=|>=|<>";
    private static final String erOpAdit = "[+-]|or|OR";
    private static final String erOpMult = "[*/]|and|AND";
    // Lembrar de usar: Pattern.compile(erReserv, Pattern.CASE_INSENSITIVE);
    private static final String erReserv = "program|var|integer|real|boolean|procedure|begin|end|if|then|while|do|not";
    
    
    
   public LexicoPascal() {
       arquivo = null;
       programa = null;
       tabela = null;
       qtdeLinhas = 1;
   }
   
   public LexicoPascal(String arquivo) {
       this.arquivo = arquivo;
       programa = null;
       tabela = null;
       qtdeLinhas = 1;
   }
   
   public String getPrograma() {
       return programa;
   }
   
   public String getArquivo() {
       return arquivo;
   }
   
   public String getTabela() {
       return tabela;
   }
   
   public void setArquivo(String arquivo) {
       this.arquivo = arquivo;
   }
   
   private void lePrograma(){
       programa = LeArquivo(arquivo);
   }
   
   public String Compila() {
       
       lePrograma();
       
       for (int index = 0; index < programa.length()-1; index++) {
           
           switch (programa.charAt(index)) {
                case 'a':
                    break;
                default:
                    break;
           }
       }
       
       return tabela;
   }
    
}
