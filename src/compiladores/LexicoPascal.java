/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiladores;

/**
 * Compilador Léxico para compilação de programas escritos na linguagem de
 * programação PASCAL. É a primeira etapa da compilação.
 * <p> Projeto da cadeira de Construção de Compiladores na UFPB, período 2015.1
 * 
 * @author Neto
 * @see TrataArquivos
 * @see String
 * @see java.util.regex
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
    private static final String erIgnore = "[ \\t\\v\\r]";
    private static final String erEnter = "[\\n]";
    private static final String erComentAb = "[{]";
    private static final String erComentFe = "[}]";
    private static final String erReserv = "(?i:program|var|integer|real|boolean|procedure|begin|end|if|then|while|do|not|repeat|until)";    // Case-insensitive (?i:)
    
    // Padrão de identificação dos tokens para a saída
    private static final String txReserv = "Palavra reservada";
    private static final String txIdent = "Identificador";
    private static final String txDelimit = "Delimitador";
    private static final String txAtrib = "Atribuição";
    private static final String txNumInt = "Número inteiro";
    private static final String txNumReal = "Número real";
    private static final String txNumCompx = "Número complexo";
    private static final String txOpMul = "Operador multiplicativo";
    private static final String txOpAdit = "Operador aditivo";
    private static final String txOpRel = "Operador relacional";
    
    
   /**
    * Construtor da classe. Obriga a passagem do parâmetro para evitar problemas. 
    * @param arquivo String com nome do arquivo a ser usado para leitura
    */ 
   public LexicoPascal(String arquivo) {
       this.arquivo = arquivo;
       programa = null;
       tabela = "";
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
   
    /**
    * Método para executar a leitura do arquivo através da classe TrataArquivos
    * e salvar a String de retorno na propriedade "programa".
    */ 
   private void lePrograma(){
       if (arquivo == null) {
           System.out.println("Não foi especificado um programa para compilação.");
           return;
       }
       programa = LeArquivo(arquivo);
   }
   
    /**
    * Principal método da classe, que realiza a compilação do programa lido do arquivo, de fato.
    * <p>Salva o programa no diretório padrão com método da classe TrataArquivos.
    * @return String com programa ou Null caso o programa ainda não tenha sido carregado.
    */ 
   public String Compila() {
       
       lePrograma();
       
       if (programa == null)
           return null;
       
       
       /**==========================================================================
          Laço para simulação de autômato do analisador léxico, varrendo cada caractere e, através de um 
          look-ahead para ver quais são os próximos caracteres, classificar os tokens inserindo-os na tabela.
          Não foi usada a classe Tokenizer intencinoalmente para que ainda seja possível realizar alguma operação
          sobre espaços em branco e caracteres especiais, caso necessário futuramente.
       */
       for (int index = 0; index < programa.length(); index++) {
           String charAtual = Character.toString(programa.charAt(index));
           
           //==========================================================================
           // Caso encontre quebra de linha, incrementar a contagem e ir para próximo caractere
           if ( charAtual.matches(erEnter) ) {
               qtdeLinhas++;
           }
           
           //==========================================================================
           // Caso encontre caracteres de formatação ou espaço, ir para próximo caractere
           else if ( charAtual.matches(erIgnore) ) {
           }
           
           //==========================================================================
           // Caso encontre abertura de comentário "{" deve procurar pelo seu fechamento "}" e prosseguir, alertando caso não feche.
           else if ( charAtual.matches(erComentAb) ) {
               boolean fechou = false;
               for (int i = index+1; i < programa.length(); i++) {
                   if ( Character.toString(programa.charAt(i)).matches(erComentFe) ) {
                       fechou = true;
                       index = i;     // Índice de varredura do programa deve ir para caractere seguinte ao fecha-comentário
                       break;
                       //tabela = tabela.concat(programa.substring(index,index+i+1));
                       //tabela = tabela.concat("\t"+ "COMENTÁRIO" +"\t"+qtdeLinhas);
                   }
               }
               if (!fechou) {
                   System.out.println("Erro de comentário não fechado. Linha: " + qtdeLinhas);
                   break;
               }
           }
           
           //==========================================================================
           // Caso encontre números, deve verificar se é inteiro, real e com exponencial (e se é complexo)
           else if ( charAtual.matches(erNums) ) {
               String tipo = txNumInt;
               String dado = charAtual;
               boolean exponential = false;
               boolean erroExponential = false;
               boolean erroComplexo = false;
               boolean complexoCompleto = false;
               
               for (int i = index+1; i < programa.length(); i++) {
                   if ( Character.toString(programa.charAt(i)).matches(erNums) )
                       ;
                   else if ( Character.toString(programa.charAt(i)).matches("[.]") ) {
                       if (tipo.equals(txNumReal) && !tipo.equals(txNumCompx)) {    // Se encontrar dois pontos, para (não sendo complexo)
                           dado = programa.substring(index, i);
                           index = i-1;     // Compensa o i++ do laço externo
                           break;
                       }
                       if (!tipo.equals(txNumCompx))
                           tipo = txNumReal;
                       else {
                           if ( (Character.toString(programa.charAt(i+1)).matches(erNums)) )
                               complexoCompleto = true;
                       }
                   } else if ( Character.toString(programa.charAt(i)).matches("[eE]") ) {
                       if (exponential) {       // Se encontrar dois expoenciais, para
                           dado = programa.substring(index, i);
                           index = i-1;     // Compensa o i++ do laço externo
                           break;
                       }
                       
                       if ( (Character.toString(programa.charAt(i+1)).matches(erNums)) ) {
                           tipo = txNumReal;
                           exponential = true;
                       } else if ( (Character.toString(programa.charAt(i+1)).matches("[+-]")) && (Character.toString(programa.charAt(i+2)).matches(erNums)) ) {
                           i++;
                           tipo = txNumReal;
                           exponential = true;
                       } else {
                           dado = programa.substring(index, i);
                           index = i-1;     // Compensa o i++ do laço externo
                           erroExponential = true;
                           break;
                       }
                   } else if ( Character.toString(programa.charAt(i)).matches("[+-]")) {
                       if (!tipo.equals(txNumReal)) {    // Se ainda não for real, para
                           dado = programa.substring(index, i);
                           index = i-1;     // Compensa o i++ do laço externo
                           break;
                       }
                       
                       if ( (Character.toString(programa.charAt(i+1)).equals("i")) ) {
                           tipo = txNumCompx;
                           exponential = true;
                           i++;
                       } else {
                           dado = programa.substring(index, i);
                           index = i-1;      // Compensa o i++ do laço externo
                           erroComplexo = true;
                           break;
                       }
                   }else {
                       dado = programa.substring(index, i);
                       index = i-1;      // Compensa o i++ do laço externo
                       break;
                   }
               }
               if (erroExponential) {
                   System.out.println("Erro de notação ciêntífica no número real. Linha: " + qtdeLinhas);
                   break;
               }
               if (erroComplexo || (tipo.equals(txNumCompx) && !complexoCompleto)) {
                   System.out.println("Erro de notação no número complexo. Linha: " + qtdeLinhas);
                   break;
               }
               
               tabela = tabela.concat(dado + "\t\t" + tipo + "\t\t" + qtdeLinhas + System.lineSeparator());
           }
           
           //==========================================================================
           // Caso encontre um ponto, verifica se é o ponto final ou se se trata de um número real
           else if ( charAtual.matches("[.]") ) {
               String dado = charAtual;
               boolean exponential = false;
               boolean erroExponential = false;
               
               if ( (Character.toString(programa.charAt(index+1)).matches(erNums)) ) {
                   
                   for (int i = index+1; i < programa.length(); i++) {
                        if ( Character.toString(programa.charAt(i)).matches(erNums) )
                            ;
                        else if ( Character.toString(programa.charAt(i)).matches("[.]") ) {  // Se encontrar dois pontos, para
                            dado = programa.substring(index, i);
                            index = i-1;     // Compensa o i++ do laço externo
                            break;
                        } else if ( Character.toString(programa.charAt(i)).matches("[eE]") ) {
                            if (exponential) {       // Se encontrar dois expoenciais, para
                                dado = programa.substring(index, i);
                                index = i-1;     // Compensa o i++ do laço externo
                                break;
                            }

                            if ( (Character.toString(programa.charAt(i+1)).matches(erNums)) ) {
                                exponential = true;
                            } else if ( (Character.toString(programa.charAt(i+1)).matches("[+-]")) && (Character.toString(programa.charAt(i+2)).matches(erNums)) ) {
                                i++;
                                exponential = true;
                            } else {
                                dado = programa.substring(index, i);
                                index = i-1;     // Compensa o i++ do laço externo
                                erroExponential = true;
                                break;
                            }
                        } else {
                            dado = programa.substring(index, i);
                            index = i-1;      // Compensa o i++ do laço externo
                            break;
                        }
                   }
                   
               } else {
                   tabela = tabela.concat("." + "\t\t" + txDelimit + "\t\t" + qtdeLinhas + System.lineSeparator());
                   continue;
               }
               
               if (erroExponential) {
                   System.out.println("Erro de exponencial. Linha: " + qtdeLinhas);
                   break;
               }
               
               tabela = tabela.concat(dado + "\t\t" + txNumReal + "\t\t" + qtdeLinhas + System.lineSeparator());
           }
           
           //==========================================================================
           // Se encontrar letras, verifica até onde é válido e considera como identificador.
           // Se estiver dentro da lista de palavras-reservadas, salva-o como tal.
           else if ( charAtual.matches(erLetras) ) {
               String tipo = txIdent + "\t\t";  // Nivelamento de tabs
               String dado = charAtual;

               for (int i = index+1; i < programa.length(); i++) {
                   if ( !Character.toString(programa.charAt(i)).matches(erIdent) ) {
                       dado = programa.substring(index, i);
                       index = i-1;     // Compensa o i++ do laço externo
                       break;
                   }
               }
               if (dado.matches(erReserv))
                   tipo = txReserv + "\t";      // Nivelamento de tabs
               
               tabela = tabela.concat(dado + "\t\t" + tipo + qtdeLinhas + System.lineSeparator());
           }
           
           //==========================================================================
           // Se encontrar um delimitador, salva na tabela e passa para o próximo caractere
           else if ( charAtual.matches(erDelim) ) {
               if ( charAtual.matches("[:]") && Character.toString(programa.charAt(index+1)).matches("[=]") ) {
                   tabela = tabela.concat(":=" + "\t\t" + txAtrib + "\t\t" + qtdeLinhas + System.lineSeparator());
                   index++;     // É ':=' então próxima iteração deve ir para caractere seguinte ao '='
               } else {
                   tabela = tabela.concat(charAtual + "\t\t" + txDelimit + "\t\t" + qtdeLinhas + System.lineSeparator());
               }
           }
           
           //==========================================================================
           // Se encontrar um operador aditivo, salva na tabela e passa para o próximo caractere
           else if ( charAtual.matches(erOpAdit) ) {
               tabela = tabela.concat(charAtual + "\t\t" + txOpAdit + "\t" + qtdeLinhas + System.lineSeparator());
           }
           
           //==========================================================================
           // Se encontrar um operador multiplicativo, salva na tabela e passa para o próximo caractere
           else if ( charAtual.matches(erOpMult) ) {
               tabela = tabela.concat(charAtual + "\t\t" + txOpMul + "\t" + qtdeLinhas + System.lineSeparator());
           }
           
           //==========================================================================
           // Se encontrar um operador relacional, salva na tabela e passa para o próximo caractere
           else if ( charAtual.matches(erOpRel) ) {
               if ( charAtual.matches("[=]") ) {
                   tabela = tabela.concat(charAtual + "\t\t" + txOpRel + "\t" + qtdeLinhas + System.lineSeparator());
               } else if ( Character.toString(programa.charAt(index+1)).matches("[=]") ) {
                   tabela = tabela.concat(charAtual + Character.toString(programa.charAt(index+1)) + "\t\t" + txOpRel + "\t" + qtdeLinhas + System.lineSeparator());
                   index++;     // É '<=' ou '<=' então próxima iteração deve ir para caractere seguinte ao '='
               } else if ( charAtual.matches("[<]") && Character.toString(programa.charAt(index+1)).matches("[>]") ) {
                   tabela = tabela.concat("<>" + "\t\t" + txOpRel + "\t" + qtdeLinhas + System.lineSeparator());
                   index++;     // É '<>' então próxima iteração deve ir para caractere seguinte ao '>'
               } else
                   tabela = tabela.concat(charAtual + "\t\t" + txOpRel + "\t" + qtdeLinhas + System.lineSeparator());
           }
           
           //==========================================================================
           else {
               System.out.println("Caractere não pertencente a linguagem. Caractere: \"" + charAtual + "\". " + "Linha: " + qtdeLinhas);
           }
           
       }
       
       
       SalvaArquivo(tabela);
       return tabela;
   }
    
}
