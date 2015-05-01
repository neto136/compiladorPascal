/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiladores;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Classe TrataArquivos para realizar a interface de tratamento de arquivos.
 * Seus métodos servem para ler um arquivo passado como argumento, retornando a String
 * e para salvar em arquivo uma String passada como parâmetro (ambos tratando os possíveis erros).
 * <p>Adaptada para uso com classe de Analisador Léxico de programas em Pascal.
 * 
 * @see FileReader
 * @see FileWriter
 * @see BufferedReader
 * @see BufferedWriter
 * @author Neto
 */
public class TrataArquivos{
    
    /**
    * Lê arquivo texto com programa escrito em PASCAL e passa-o para uma string
    * lendo linha por linha e retorna esta string.
    * <p>Como deve ser usado por um analisador léxico, as quebras de linha  que
    * são removidas, são manualmente inseridas novamente para que o compilador
    * possa manter a contagem das linhas.
    * 
    * @param arquivo    String com nome do arquivo a ser lido
    * @return   String com programa Pascal lido no parâmetro, mantendo as quebras de linha para contá-las
    */
    public String LeArquivo(String arquivo) {
        String programa = "";
        
        try {
            FileReader filereader = new FileReader(arquivo);
            BufferedReader buffreader = new BufferedReader(filereader);
            
            String linha = null;
            while ((linha = buffreader.readLine()) != null) {
                programa = programa.concat(linha);
                programa = programa.concat(System.lineSeparator()); // Readiciona o separador de linha pois o readLine os remove
            }
            
            filereader.close();
            buffreader.close();
        } catch (IOException e) {
            System.out.println("Erro de leitura do arquivo!");
        }

        return programa;
    }
    
    /**
    * Escreve em arquivo texto uma tabela de tokens da etapa de compilação
    * léxica de um programa em PASCAL, informando os tokens, suas classificações
    * e as linhas em que se encontram no programa.
    * <p>Esta saída deve ser usada pelo analisador Sintático na próxima etapa
    * de compilação.
    * 
    * @param tabela String com tabela léxica de programa Pascal compilado
    *               seguindo o padrão "TOKEN CLASSIFICAÇÃO LINHA"
    */
    public void SalvaArquivo(String tabela){
        File arquivo = new File("TabelaLexico.txt");
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date data = new Date();
        
        try {
            FileWriter filewriter = new FileWriter(arquivo, true); // Parâmetro true permite escrever em arquivo existente sem recriá-lo
            BufferedWriter bufwriter = new BufferedWriter(filewriter);
            
            bufwriter.write(dateFormat.format(data) + System.lineSeparator());
            bufwriter.write("===============================================" + System.lineSeparator());
            bufwriter.write("TOKEN\t\tCLASSIFICAÇÃO\t\tLINHA" + System.lineSeparator());
            bufwriter.write("===============================================" + System.lineSeparator());
            bufwriter.write(tabela);
            bufwriter.write(System.lineSeparator());
            
            bufwriter.flush();
            filewriter.close();
            bufwriter.close();
        } catch (IOException e) {
            System.out.println("Não foi possível salvar o arquivo");
        }
    }
}
