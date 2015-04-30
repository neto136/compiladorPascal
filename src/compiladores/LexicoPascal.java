/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiladores;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final String erIgnore = "[ \\t\\v\\r]";
    private static final String erEnter = "[\\n]";
    private static final String erComentAb = "[{]";
    private static final String erComentFe = "[}]";
    private static final String erReserv = "(?i:program|var|integer|real|boolean|procedure|begin|end|if|then|while|do|not)";    // Case-insensitive (?i:)
    
    // Padrão de identificação dos tokens para a saída
    private static final String txReserv = "Palavra reservada";
    private static final String txIdent = "Identificador";
    private static final String txDelimit = "Delimitador";
    private static final String txAtrib = "Atribuição";
    private static final String txNumInt = "Número inteiro";
    private static final String txNumReal = "Número real";
    private static final String txOpMul = "Operador multiplicativo";
    private static final String txOpAdit = "Operador aditivo";
    private static final String txOpRel = "Operador relacional";
    
    
    
   public LexicoPascal() {
       arquivo = null;
       programa = null;
       tabela = "";
       qtdeLinhas = 1;
   }
   
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
   
   private void lePrograma(){
       programa = LeArquivo(arquivo);
   }
   
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
       for (int index = 0; index < programa.length()-1; index++) {
           
           //==========================================================================
           // Caso encontre quebra de linha, incrementar a contagem e ir para próximo caractere
           if ( Character.toString(programa.charAt(index)).matches(erEnter) ) {
               qtdeLinhas++;
               continue;
           }
           
           //==========================================================================
           // Caso encontre caracteres de formatação ou espaço, ir para próximo caractere
           else if ( Character.toString(programa.charAt(index)).matches(erIgnore) ) {
               continue;
           }
           
           //==========================================================================
           // Caso encontre abertura de comentário "{" deve procurar pelo seu fechamento "}" e prosseguir, alertando caso não feche.
           else if ( Character.toString(programa.charAt(index)).matches(erComentAb) ) {
               boolean fechou = false;
               for (int i = index+1; index < programa.length()-1; i++) {
                   if ( Character.toString(programa.charAt(index)).matches(erComentFe) ) {
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
               continue;
           }
           
           //==========================================================================
           // Caso encontre números, deve verificar se é inteiro, real e com exponencial
           else if ( Character.toString(programa.charAt(index)).matches(erNums) ) {
               String tipo = txNumInt;
               String dado = Character.toString(programa.charAt(index));
               boolean exponential = false;
               boolean erroExponential = false;
               
               for (int i = index+1; index < programa.length()-1; i++) {
                   if ( Character.toString(programa.charAt(index)).matches(erNums) )
                       continue;
                   else if ( Character.toString(programa.charAt(index)).matches("[.]") ) {
                       if (tipo.equals(txNumReal)) {    // Se encontrar dois pontos, para
                           dado = programa.substring(index, i);
                           index = i-1;     // Compensa o i++ do laço externo
                           break;
                       }
                       tipo = txNumReal;
                       continue;
                   } else if ( Character.toString(programa.charAt(index)).matches("[eE]") ) {
                       if (exponential) {       // Se encontrar dois expoenciais, para
                           dado = programa.substring(index, i);
                           index = i-1;     // Compensa o i++ do laço externo
                           break;
                       }
                       
                       if ( (Character.toString(programa.charAt(index+1)).matches(erNums)) ) {
                           tipo = txNumReal;
                           exponential = true;
                       } else if ( (Character.toString(programa.charAt(index+1)).matches("[+-]")) && (Character.toString(programa.charAt(index+2)).matches(erNums)) ) {
                           i++;
                           tipo = txNumReal;
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
               if (erroExponential) {
                   System.out.println("Erro de exponencial. Linha: " + qtdeLinhas);
                   break;
               }
               
               tabela = tabela.concat(dado + "\t\t" + tipo + "\t\t" + qtdeLinhas + System.lineSeparator());
               continue;
           }
           
           //==========================================================================
           // Caso encontre um ponto, verifica se é o ponto final ou se se trata de um número real
           else if ( Character.toString(programa.charAt(index)).matches("[.]") ) {
               String dado = Character.toString(programa.charAt(index));
               boolean exponential = false;
               boolean erroExponential = false;
               
               if ( (Character.toString(programa.charAt(index+1)).matches(erNums)) ) {
                   
                   for (int i = index+1; index < programa.length()-1; i++) {
                        if ( Character.toString(programa.charAt(index)).matches(erNums) )
                            continue;
                        else if ( Character.toString(programa.charAt(index)).matches("[.]") ) {  // Se encontrar dois pontos, para
                            dado = programa.substring(index, i);
                            index = i-1;     // Compensa o i++ do laço externo
                            break;
                        } else if ( Character.toString(programa.charAt(index)).matches("[eE]") ) {
                            if (exponential) {       // Se encontrar dois expoenciais, para
                                dado = programa.substring(index, i);
                                index = i-1;     // Compensa o i++ do laço externo
                                break;
                            }

                            if ( (Character.toString(programa.charAt(index+1)).matches(erNums)) ) {
                                exponential = true;
                            } else if ( (Character.toString(programa.charAt(index+1)).matches("[+-]")) && (Character.toString(programa.charAt(index+2)).matches(erNums)) ) {
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
               continue;
           }
           
           //==========================================================================
           // Se encontrar letras, verifica até onde é válido e considera como identificador.
           // Se estiver dentro da lista de palavras-reservadas, salva-o como tal.
           else if ( Character.toString(programa.charAt(index)).matches(erLetras) ) {
               String tipo = txIdent;
               String dado = Character.toString(programa.charAt(index));

               for (int i = index+1; index < programa.length()-1; i++) {
                   if ( !Character.toString(programa.charAt(index)).matches(erIdent) ) {
                       dado = programa.substring(index, i);
                       index = i-1;     // Compensa o i++ do laço externo
                       break;
                   }
               }
               if (dado.matches(erReserv))
                   tipo = txReserv;
               
               tabela = tabela.concat(dado + "\t\t" + tipo + "\t\t" + qtdeLinhas + System.lineSeparator());
               continue;
           }
           
           //==========================================================================
           // Se encontrar um delimitador, salva na tabela e passa para o próximo caractere
           else if ( Character.toString(programa.charAt(index)).matches(erDelim) ) {
               if ( Character.toString(programa.charAt(index)).matches("[:]") && Character.toString(programa.charAt(index+1)).matches("[=]") )
                   tabela = tabela.concat(":=" + "\t\t" + txAtrib + "\t\t" + qtdeLinhas + System.lineSeparator());
               else
                   tabela = tabela.concat(Character.toString(programa.charAt(index)) + "\t\t" + txDelimit + "\t\t" + qtdeLinhas + System.lineSeparator());
               continue;
           }
           
           //==========================================================================
           // Se encontrar um operador aditivo, salva na tabela e passa para o próximo caractere
           else if ( Character.toString(programa.charAt(index)).matches(erOpAdit) ) {
               tabela = tabela.concat(":=" + "\t\t" + txOpAdit + "\t\t" + qtdeLinhas + System.lineSeparator());
               continue;
           }
           
           //==========================================================================
           // Se encontrar um operador multiplicativo, salva na tabela e passa para o próximo caractere
           else if ( Character.toString(programa.charAt(index)).matches(erOpMult) ) {
               tabela = tabela.concat(Character.toString(programa.charAt(index)) + "\t\t" + txOpMul + "\t\t" + qtdeLinhas + System.lineSeparator());
               continue;
           }
           
           //==========================================================================
           // Se encontrar um operador relacional, salva na tabela e passa para o próximo caractere
           else if ( Character.toString(programa.charAt(index)).matches(erOpRel) ) {
               if ( Character.toString(programa.charAt(index)).matches("[=]") )
                   tabela = tabela.concat(Character.toString(programa.charAt(index)) + "\t\t" + txOpRel + "\t\t" + qtdeLinhas + System.lineSeparator());
               else if ( Character.toString(programa.charAt(index+1)).matches("[=]") )
                   tabela = tabela.concat(Character.toString(programa.charAt(index)) + Character.toString(programa.charAt(index+1)) + "\t\t" + txOpRel + "\t\t" + qtdeLinhas + System.lineSeparator());
               else if ( Character.toString(programa.charAt(index)).matches("[<]") && Character.toString(programa.charAt(index+1)).matches("[>]") )
                   tabela = tabela.concat("<>" + "\t\t" + txOpRel + "\t\t" + qtdeLinhas + System.lineSeparator());
               else
                   tabela = tabela.concat(Character.toString(programa.charAt(index)) + "\t\t" + txOpRel + "\t\t" + qtdeLinhas + System.lineSeparator());
               continue;
           }
           
           //==========================================================================
           else {
               System.out.println("Caractere não pertencente a linguagem. Linha: " + qtdeLinhas);
           }
           
       }
       
       
       
       return tabela;
   }
    
}
