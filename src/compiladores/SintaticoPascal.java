/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiladores;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Compilador Sintático para compilação de programas escritos na linguagem de
 * programação PASCAL. É a segunda etapa da compilação.
 * <p> Projeto da cadeira de Construção de Compiladores na UFPB, período 2015.1
 * 
 * @author Neto
 * @see LexicoPascal
 * @see TrataArquivos
 * @see String
 * @see java.util.regex
 */
public class SintaticoPascal extends TrataArquivos{
    private List<DadoLinha> linha;      // Lista com estrutura de Strings (Token,Identificador,Linha)
    private String tabela, mensagem;
    private Iterator<DadoLinha> iterator;
    DadoLinha dados;
    
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
    
    
    /**
    * Construtor da classe. Obriga a passagem da tabela vinda do analisador Léxico. 
    * @param tabela  String com tabela gerada pelo analisador Léxico
    */ 
    public SintaticoPascal(String tabela) {
        linha = new ArrayList<>();
        iterator = linha.listIterator();
        this.tabela = tabela;
    }
    
    /**
    * Construtor da classe. Obriga a passagem do arquivo com a tabela gerada pelo analisador Léxico.
    * @param arquivo String com nome do arquivo a ser usado para leitura
    * @param isFile  Parâmetro auxiliar para dizer se o parâmetro é o arquivo, invés da tabela.
    *                Serve apenas para permitir a sobrecarga do construtor.
    */ 
    public SintaticoPascal(String arquivo, boolean isFile) {
        linha = new ArrayList<>();
        iterator = linha.listIterator();
        this.tabela = "";
        LeTabela(arquivo);
    }
    
    /**
    * Método para executar a leitura do arquivo através da classe TrataArquivos
    * e salvar a String de retorno na propriedade "tabela". Como o arquivo gerado
    * pelo analisador léxico tem cabeçalhos adicionados, aqui eles são removidos.
    * <p>Usado apenas quando é passado o arquivo a ser lido, invés de String com a tabela.
    * @param arquivo Nome do arquivo a ser lido.
    */ 
    private void LeTabela(String arquivo) {
        // Verifica se o arquivo é vazio
        if (arquivo == null || arquivo.equals(""))
            return;
        
        // Divide a string da tabela em sub-strings linha-a-linha
        String tabelaCabecalho = LeArquivo(arquivo);
        String[] linhas = tabelaCabecalho.split(System.lineSeparator());
        
        // Ignora as linhas de cabeçalho do documento e salva as demais linhas 
        for (String linhaAtual : linhas) {
            if ( linhaAtual.startsWith("=========") || (linhaAtual.contains("TOKEN") && linhaAtual.contains("CLASSIFICAÇÃO") && linhaAtual.contains("LINHA")) )
                continue;
            tabela = tabela + linhaAtual + System.lineSeparator();
        }
    }
    
    /**
    * Método para preencher estrutura de lista com os tokens das linhas.
    * <p>Cada elemento da lista é um dado do tipo DadoLinha, com 3 strings.
    * @return Retorna um valor booleano confirmando se foi possível gerar a lista.
    */ 
    private boolean geraLista() {
        // Se a tabela está vazia, retorna erro
        if (tabela == null || tabela.equals("")) {
            System.out.println("Erro: A tabela de tokens do compilador Léxico está vazia");
            return false;
        }
        
        // Divide a tabela em strings com cada linha
        String[] linhas = tabela.split(System.lineSeparator());
        
        // Para cada linha, lê os 3 tokens (Token, Tipo, Linha) e insere na lista
        for (String linhaAtual : linhas) {
            String[] tokens = linhaAtual.split("[\\t]+");   // Os tokens são separados por 1 ou mais tabulações
            DadoLinha dado = new DadoLinha(tokens[0], tokens[1], tokens[2]);
            linha.add(dado);
        }
        
        return true;
    }
    
    /**
    * Principal método da classe, que realiza a compilação da tabela léxica, de fato.
    * <p>Segue a gramática do analisador especificada na disciplina.
    * @return 'True' se compilou corretamente e 'False' se encontrou algum erro..
    */ 
    public boolean Compila() {
        // Se não for possível 
        if (!geraLista())
            return false;
        
        mensagem = "Analisador Sintático: Programa compilado com sucesso.";
        iterator = linha.listIterator();    // Atualiza o iterador da lista
        
        /*while (iterator.hasNext()) {
            DadoLinha dado = iterator.next();
            System.out.println(dado.getToken() + "  " + dado.getIdent() + " " + dado.getLinha());
        }*/
        
        
        
        // Concebido inicialmente para tratar apenas palavras-reservadas minúsculas
        // Processa a primeira parte da gramática. As produções de 'PROGRAMA'
        if (iterator.hasNext())
            dados = iterator.next();
        else {
             mensagem = "ERRO: programa vazio";
            return false;
        }
        
        if (dados.getToken().equals("program")) {
            if (iterator.hasNext()) dados = iterator.next(); else return false;
            if (dados.getIdent().equals(txIdent)) {
                if (iterator.hasNext()) dados = iterator.next(); else return false;
                if (dados.getToken().equals(";")) {
                    if (iterator.hasNext()) dados = iterator.next(); else return false;
                    if (DeclaracoesVariaveis()) {
                        if (DeclaracoesSubprogramas()) {
                            if (ComandoComposto()) {
                                if (!dados.getToken().equals("."))
                                    mensagem = "ERRO: delimitador final '.' esperado. Linha: " + dados.getLinha();
                                else
                                    mensagem = "Analisador Sintático: Programa compilado com sucesso.";
                            }
                        }
                    }
                } else {
                    mensagem = "ERRO: delimitador ';' esperado. Linha: " + dados.getLinha();
                }
            } else {
                mensagem = "ERRO: identificador de programa errado. Erro de tipo. Linha: " + dados.getLinha();
            }
        } else {
            mensagem = "ERRO: identificador inicial 'program' não definido. Linha: " + dados.getLinha();
        }
        
        
        
        System.out.println(mensagem);   // Imprime no console a mensagem final
        return !mensagem.startsWith("ERRO");    // Caso a mensagem inicie com "ERRO", retorna falso
    }
    
    private boolean DeclaracoesVariaveis() {
        if (dados.getToken().equals("var")) {
            if (iterator.hasNext()) dados = iterator.next(); else return false;
            return ListaDeclaracoesVariaveis();
        } else {
            return true;
        }
    }
    
    private boolean ListaDeclaracoesVariaveis() {
        if (ListaIdentificadores()) {
            if (dados.getToken().equals(":")) {
                if (iterator.hasNext()) dados = iterator.next(); else return false;
                if (Tipo()) {
                    if (dados.getToken().equals(";")) {
                        if (iterator.hasNext()) dados = iterator.next(); else return false;
                        return ListaDeclaracoesVariaveis2();
                    } else {
                        mensagem = "ERRO: delimitador ';' esperado para encerrar declaração de tipo de variável. Linha: " + dados.getLinha();
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                mensagem = "ERRO: delimitador ':' esperado para declaração de tipo. Linha: " + dados.getLinha();
                return false;
            }
        } else {
            return false;
        }
    }
    
    private boolean ListaDeclaracoesVariaveis2() {
        // Se tipo do token é Identificador então deve seguir para processamento de lista
        // de identificadores. Caso contrário, então está iniciando com algo que não é identificador
        // e se encontra no caso "Épsilon/Vazio" da produção da regra gramatical, ou seja, declarações encerradas.
        if (dados.getIdent().equals(txIdent)) {
            if (ListaIdentificadores()) {
                if (dados.getToken().equals(":")) {
                    if (iterator.hasNext()) dados = iterator.next(); else return false;
                    if (Tipo()) {
                        if (dados.getToken().equals(";")) {
                            if (iterator.hasNext()) dados = iterator.next(); else return false;
                            return ListaDeclaracoesVariaveis2();
                        } else {
                            mensagem = "ERRO: delimitador ';' esperado para encerrar declaração de tipo de variável. Linha: " + dados.getLinha();
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    mensagem = "ERRO: delimitador ':' esperado para declaração de tipo. Linha: " + dados.getLinha();
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
    
    private boolean ListaIdentificadores() {
        if (dados.getIdent().equals(txIdent)) {
            if (iterator.hasNext()) dados = iterator.next(); else return false;
            return ListaIdentificadores2();
        } else {
            mensagem = "ERRO: lista de identificadores possui elemento com tipo não-identificador. Linha: " + dados.getLinha();
            return false;
        }
    }
    
    private boolean ListaIdentificadores2() {
        if (dados.getToken().equals(",")) {
            if (iterator.hasNext()) dados = iterator.next(); else return false;
            if (dados.getIdent().equals(txIdent)) {
                if (iterator.hasNext()) dados = iterator.next(); else return false;
                return ListaIdentificadores2();
            } else {
                mensagem = "ERRO: lista de identificadores possui elemento com tipo não-identificador. Linha: " + dados.getLinha();
                return false;
            }
        } else {
            return true;
        }
    }
    
    private boolean Tipo() {
        if (dados.getToken().equals("integer") || dados.getToken().equals("real") || dados.getToken().equals("boolean")) {
            if (iterator.hasNext()) dados = iterator.next(); else return true;
            return true;
        } else {
            mensagem = "ERRO: tipo de variável não permitido. Linha: " + dados.getLinha();
            return false;
        }
    }
    
    
    private boolean DeclaracoesSubprogramas() {
        // Caso token seja "procedure" (que é primeiro token a ser analisado na recursão
        // de subprogramas, deve continuar o processamento. Caso contrário, então deve aceitar
        // e sair pois caiu no caso "Épsilon/Vazio" das produções, ou seja, não há subprogramas.
        if (dados.getToken().equals("procedure")) {
            if (DeclaracaoSubprograma()) {
                if (dados.getToken().equals(";")) {
                    if (iterator.hasNext()) dados = iterator.next(); else return false;
                    return DeclaracoesSubprogramas2();
                } else {
                    mensagem = "ERRO: delimitador ';' esperado no fim do subprograma. Linha: " + dados.getLinha();
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
    
    private boolean DeclaracoesSubprogramas2() {
        // Caso token seja "procedure" (que é primeiro token a ser analisado na recursão
        // de subprogramas, deve continuar o processamento. Caso contrário, então deve aceitar
        // e sair pois caiu no caso "Épsilon/Vazio" das produções, ou seja, não há subprogramas.
        if (dados.getToken().equals("procedure")) {
            if (DeclaracaoSubprograma()) {
                if (dados.getToken().equals(";")) {
                    if (iterator.hasNext()) dados = iterator.next(); else return false;
                    return DeclaracoesSubprogramas2();
                } else {
                    mensagem = "ERRO: delimitador ';' esperado no fim do subprograma. Linha: " + dados.getLinha();
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
    
    private boolean DeclaracaoSubprograma() {
        if (dados.getToken().equals("procedure")) {
            if (iterator.hasNext()) dados = iterator.next(); else return false;
            if (dados.getIdent().equals(txIdent)) {
                if (iterator.hasNext()) dados = iterator.next(); else return false;
                if (Argumentos()) {
                    if (dados.getToken().equals(";")) {
                        dados = iterator.next();
                        if (DeclaracoesVariaveis()) {
                            if (DeclaracoesSubprogramas()) {
                                return (ComandoComposto());
                            } else {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    } else {
                        mensagem = "ERRO: delimitador ';' esperado no fim do subprograma. Linha: " + dados.getLinha();
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                mensagem = "ERRO: subprograma identificado com nome do tipo não-identificador. Linha: " + dados.getLinha();
                return false;
            }
        } else {
            mensagem = "ERRO: declaração de subprograma deveria iniciar com 'procedure'. Linha: " + dados.getLinha();
            return false;
        }
    }
    
    private boolean Argumentos() {
        // Caso token seja "(", deve continuar o processamento. Caso contrário, então deve aceitar
        // e sair pois caiu no caso "Épsilon/Vazio" das produções, ou seja, não há argumentos.
        if (dados.getToken().equals("(")) {
            if (iterator.hasNext()) dados = iterator.next(); else return false;
            if (ListaParametros()) {
                if (dados.getToken().equals(")")) {
                    if (iterator.hasNext()) dados = iterator.next(); else return false;
                    return true;
                } else {
                    mensagem = "ERRO: delimitador ')' esperado para fechar lista de parâmetros. Linha: " + dados.getLinha();
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
    
    private boolean ListaParametros() {
        if (ListaIdentificadores()) {
            if (dados.getToken().equals(":")) {
                if (iterator.hasNext()) dados = iterator.next(); else return false;
                if (Tipo()) {
                    return ListaParametros2();
                } else {
                    return false;
                }
            } else {
                mensagem = "ERRO: delimitador ':' esperado após lista de identificadores. Linha: " + dados.getLinha();
                return false;
            }
        } else {
            return false;
        }
    }
    
    private boolean ListaParametros2() {
        // Caso token seja ";", deve continuar o processamento. Caso contrário, então deve aceitar
        // e sair pois caiu no caso "Épsilon/Vazio" das produções, ou seja, não há mais parametros.
        if (dados.getToken().equals(";")) {
            if (iterator.hasNext()) dados = iterator.next(); else return false;
            if (ListaIdentificadores()) {
                if (dados.getToken().equals(":")) {
                    if (iterator.hasNext()) dados = iterator.next(); else return false;
                    if (Tipo()) {
                        return ListaParametros2();
                    } else {
                        return false;
                    }
                } else {
                    mensagem = "ERRO: delimitador ':' esperado após lista de identificadores. Linha: " + dados.getLinha();
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
    
    
    private boolean ComandoComposto() {
        if (dados.getToken().equals("begin")) {
            if (iterator.hasNext()) dados = iterator.next(); else return false;
            if (ComandosOpcionais()) {
                if (dados.getToken().equals("end")) {
                    if (iterator.hasNext()) dados = iterator.next(); else return true;
                    return true;
                } else {
                    mensagem = "ERRO: comando composto deve finalizar com 'end'. Linha: " + dados.getLinha();
                    return false;
                }
            } else {
                return false;
            }
        } else {
            mensagem = "ERRO: comando composto deve iniciar com 'begin'. Linha: " + dados.getLinha();
            return false;
        }
    }
    
    private boolean ComandosOpcionais() {
        // Para continuar processando, o token deve ser algo em ListaComandos (que resumindo, inicia com tipo
        // Identificador ou com Begin ou com IF ou WHILE). Caso não seja nenhum deles, então caiu na produção
        // "Épsilon/Vazia", ou seja, não há lista de comandos
        if (dados.getToken().equals("if") || dados.getToken().equals("begin") || dados.getToken().equals("while") || dados.getIdent().equals(txIdent)) {
            return ListaComandos();
        } else {
            return true;
        }
    }
    
    private boolean ListaComandos() {
        if (Comando()) {
            return ListaComandos2();
        } else {
            return false;
        }
    }
    
    private boolean ListaComandos2() {
        // Caso token seja ";", deve continuar o processamento. Caso contrário, então deve aceitar
        // e sair pois caiu no caso "Épsilon/Vazio" das produções, ou seja, não há mais comandos.
        if (dados.getToken().equals(";")) {
            if (iterator.hasNext()) dados = iterator.next(); else return false;
            if (Comando()) {
                return ListaComandos2();
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
    
    private boolean Comando() {
        if (dados.getToken().equals("if")) {
            if (iterator.hasNext()) dados = iterator.next(); else return false;
            if (Expressao()) {
                if (dados.getToken().equals("then")) {
                    if (iterator.hasNext()) dados = iterator.next(); else return false;
                    if (Comando()) {
                        return ParteElse();
                    } else {
                        return false;
                    }
                } else {
                    mensagem = "ERRO: esperado 'then' para o comando 'if'. Linha: " + dados.getLinha();
                    return false;
                }
            } else {
                return false;
            }
        } else if (dados.getToken().equals("while")) {
            if (iterator.hasNext()) dados = iterator.next(); else return false;
            if (Expressao()) {
                if (dados.getToken().equals("do")) {
                    return Comando();
                } else {
                    mensagem = "ERRO: esperado 'do' para o comando 'while'. Linha: " + dados.getLinha();
                    return false;
                }
            } else {
                return false;
            }
        } else if (dados.getToken().equals("begin")) {
            return ComandoComposto();
        } else if (dados.getIdent().equals(txIdent)) {
            if (iterator.hasNext()) dados = iterator.next(); else return false;
            if (dados.getToken().equals(":=")) {
                if (iterator.hasNext()) dados = iterator.next(); else return false;
                    return Expressao();
            } else if (dados.getToken().equals("(")) {  // Deve mandar o próprio parênteses, não o próximo token
                return AtivacaoProcedimento();
            } else {
                return true;
            }
        } else {
            mensagem = "ERRO: tipo de comando não definido. Linha: " + dados.getLinha();
            return false;
        }
    }
    
    private boolean ParteElse() {
        // Caso token seja "else", deve continuar o processamento. Caso contrário, então deve aceitar
        // e sair pois caiu no caso "Épsilon/Vazio" das produções, ou seja, não há else.
        if (dados.getToken().equals("else")) {
            if (iterator.hasNext()) dados = iterator.next(); else return false;
            return Comando();
        } else {
            return true;
        }
    }
    
    private boolean AtivacaoProcedimento() {
        if (dados.getToken().equals("(")) {
            if (iterator.hasNext()) dados = iterator.next(); else return false;
            if (ListaExpressoes()) {
                if (dados.getToken().equals(")")) {
                    if (iterator.hasNext()) dados = iterator.next(); else return false;
                    return true;
                } else {
                    mensagem = "ERRO: esperado ')' para lista de expressões. Linha: " + dados.getLinha();
                    return false;
                }
            } else {
                return false;
            }
        } else {
            mensagem = "ERRO: esperado '(' para lista de expressões. Linha: " + dados.getLinha();
            return false;
        }
    }
    
    private boolean ListaExpressoes() {
        if (Expressao()) {
            return ListaExpressoes2();
        } else {
            return false;
        }
    }
    
    private boolean ListaExpressoes2() {
        if (dados.getToken().equals(",")) {
            if (iterator.hasNext()) dados = iterator.next(); else return false;
            if (Expressao()) {
                return ListaExpressoes2();
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
    
    private boolean Expressao() {
        if (ExpressaoSimples()) {
            if (OpRelacional()) {
                return ExpressaoSimples();
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
    
    private boolean ExpressaoSimples() {
        if (Sinal()) {
            if (Termo()) {
                return ExpressaoSimples2();
            } else {
                return false;
            }
        } else if (Termo()) {
            return ExpressaoSimples2();
        } else {
            mensagem = "ERRO: tipo de expressão simples não definida. Linha: " + dados.getLinha();
            return false;
        }
    }
    
    private boolean ExpressaoSimples2() {
        if (OpAditivo()) {
            if (Termo()) {
                return ExpressaoSimples2();
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
    
    private boolean Termo() {
        if (Fator()) {
            return Termo2();
        } else {
            return false;
        }
    }
    
    private boolean Termo2() {
        if (OpMultiplicativo()) {
            if (Fator()) {
                return Termo2();
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
    
    private boolean Fator() {
        if (dados.getIdent().equals(txNumInt)) {
            if (iterator.hasNext()) dados = iterator.next(); else return true;
            return true;
        } else if (dados.getIdent().equals(txNumReal)) {
            if (iterator.hasNext()) dados = iterator.next(); else return true;
            return true;
        } else if (dados.getToken().equals("true")) {
            if (iterator.hasNext()) dados = iterator.next(); else return true;
            return true;
        } else if (dados.getToken().equals("false")) {
            if (iterator.hasNext()) dados = iterator.next(); else return true;
            return true;
        } else if (dados.getToken().equals("not")) {
            if (iterator.hasNext()) dados = iterator.next(); else return true;
            return Fator();
        } else if (dados.getToken().equals("(")) {
            if (iterator.hasNext()) dados = iterator.next(); else return true;
            if (Expressao()) {
                if (dados.getToken().equals(")")) {
                    return true;
                } else {
                    mensagem = "ERRO: delimitador ')' esperado no fim da lista de expressões. Linha: " + dados.getLinha();
                    return false;
                }
            } else {
                return false;
            }
        } else if (dados.getIdent().equals(txIdent)) {
            if (iterator.hasNext()) dados = iterator.next(); else return false;
            if (dados.getToken().equals("(")) {
                if (iterator.hasNext()) dados = iterator.next(); else return false;
                if (ListaExpressoes()) {
                    if (dados.getToken().equals(")")) {
                        return true;
                    } else {
                        mensagem = "ERRO: delimitador ')' esperado no fim da expressão. Linha: " + dados.getLinha();
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return true;
            }
        } else {
            mensagem = "ERRO: dado não definido na lista de fatores. Linha: " + dados.getLinha();
            return false;
        }
    }
    
    private boolean Sinal() {
        if (dados.getToken().matches("[+-]")) {
            if (iterator.hasNext()) dados = iterator.next(); else return true;
            return true;
        } else {
            mensagem = "ERRO: sinal não definido. Linha: " + dados.getLinha();
            return false;
        }
    }
    
    private boolean OpRelacional() {
        if (dados.getToken().matches("[=><]|<=|>=|<>")) {
            if (iterator.hasNext()) dados = iterator.next(); else return true;
            return true;
        } else {
            mensagem = "ERRO: operador relacional não definido. Linha: " + dados.getLinha();
            return false;
        }
    }
    
    private boolean OpAditivo() {
        if (dados.getToken().matches("[+-]|or|OR")) {
            if (iterator.hasNext()) dados = iterator.next(); else return true;
            return true;
        } else {
            mensagem = "ERRO: operador aditivo não definido. Linha: " + dados.getLinha();
            return false;
        }
    }
    
    private boolean OpMultiplicativo() {
        if (dados.getToken().matches("[*/]|and|AND")) {
            if (iterator.hasNext()) dados = iterator.next(); else return true;
            return true;
        } else {
            mensagem = "ERRO: operador multiplicativo não definido. Linha: " + dados.getLinha();
            return false;
        }
    }
}
