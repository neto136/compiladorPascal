/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiladores;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * Compilador Semântico para compilação de programas escritos na linguagem de
 * programação PASCAL. É a terceira etapa da compilação.
 * <p> Construído sobre o Analisador Sintático, incrementando condições a mais.
 * <p> Projeto da cadeira de Construção de Compiladores na UFPB, período 2015.1
 * 
 * @author Neto
 * @see LexicoPascal
 * @see SintaticoPascal
 * @see TrataArquivos
 * @see String
 * @see java.util.regex
 */
public class SemanticoPascal extends TrataArquivos{
    private List<DadoLinha> linha;      // Lista com estrutura de Strings (Token,Identificador,Linha)
    private Stack<DadoPilha> pilhaEscopos; // Pilha com escopos e suas variáveis.
                                           // Cada grupo de variável tem seu escopo separado por "txSep"
                                           // Cada item corresponde à estrutura (Identificador, TipoDeDado).
    private Stack<String> pilhaControleTipo;    // Pilha auxiliar para controle de verificação de tipos em expressões e atribuições 
    private String tabela, mensagem, nomePrograma;
    private Iterator<DadoLinha> iterator;
    private int nivelEscopo;            // Auxiliar para indicar se já terminou declarações e está iniciando uso das variáveis (begin-end)
    private DadoLinha dados;
    private boolean auxIdentificadores; // Auxiliar para marcar identificadores de variáveis com "+" na pilha para definir tipo
    
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
    private static final String txSep = "$";    // Caractere separador de escopos na pilha
    // Tabela de tipos para operações aritméticas. Apenas as válidas aqui. Ordem {V1,V2,V3} no array para V3 := V1 op V2
    private static final String[][] tabelaOperacoes = {{"integer","integer","integer","real"   ,"real"},
                                                       {"integer","integer","real"   ,"integer","real"},
                                                       {"integer","real"   ,"real"   ,"real"   ,"real"}};
    private static final String[] txTipo = {"program", "integer", "real", "boolean", "procedure"};  // Tipos de dados para Pilha de Controle de Tipos
    
    
    /**
    * Construtor da classe. Obriga a passagem da tabela vinda do analisador Léxico. 
    * @param tabela  String com tabela gerada pelo analisador Léxico
    */ 
    public SemanticoPascal(String tabela) {
        linha = new ArrayList<>();
        iterator = linha.listIterator();
        nivelEscopo = 0;
        this.tabela = tabela;
    }
    
    /**
    * Construtor da classe. Obriga a passagem do arquivo com a tabela gerada pelo analisador Léxico.
    * @param arquivo String com nome do arquivo a ser usado para leitura
    * @param isFile  Parâmetro auxiliar para dizer se o parâmetro é o arquivo, invés da tabela.
    *                Serve apenas para permitir a sobrecarga do construtor.
    */ 
    public SemanticoPascal(String arquivo, boolean isFile) {
        linha = new ArrayList<>();
        iterator = linha.listIterator();
        this.tabela = "";
        nivelEscopo = 0;
        LeTabela(arquivo);
    }
    
    /**
     * Método exclusivo para centralizar o tratamento de tokens de identificadores que devem ser
     * inseridos na pilha de escopos ou que já estão declarados e apenas necessitam ser buscados.
     * <p>A variável "nivelEscopo" determina se há escopos abertos (> 0) e busca se o identificador
     * passado foi declarado na pilha. Caso esteja nas declarações (== 0), apenas busca na pilha se 
     * o identificador já foi declarado com esse nome.
     * @param token String com token do identificador a ser inserido ou buscado na pilha de escopos.
     * @param tipo String com o tipo do token no momento. Requerido como parâmetro para push() na pilha.
     * @return Boolean confirmando se foi possível declarar variável ou encontrá-la na pilha, em caso de busca.
     */
    private boolean analisaPilha(String token, String tipo) {
        Stack<DadoPilha> copia;
        copia = (Stack<DadoPilha>) pilhaEscopos.clone();
        
        // Verifica se está no nível de declaração (nível == 0) ou entrou em comandos compostos (nível > 0)
        if (nivelEscopo == 0) {
            // Analisa pilha até primeiro separador, ou seja, apenas o escopo atual.
            // Se nele já existir tal identificador, retorna erro. Senão, adiciona na pilha.
            while (!copia.peek().getIdentificador().equals(txSep))
                if (copia.pop().getIdentificador().equals(token)) {
                    mensagem = "ERRO: identificador já foi declarado neste escopo. Linha: " + dados.getLinha();
                    return false;
                }
            pilhaEscopos.push(new DadoPilha(token, tipo));
            return true;
        } else {
            // Se não for declaração de variáveis, então varre toda a pilha (do escopo atual até o máximo,
            // ou seja, até a pilha esvaziar) procurando pelo token. Se não existir é porque o identificador
            // não foi declarado. Se existir, ainda deve verificar se é o identificador do programa e não aceitar.
            while (!copia.isEmpty())
                if (copia.pop().getIdentificador().equals(token)) {     // Achou o token na pilha.
                    if (token.equals(nomePrograma)) {     // Vê se o nome do programa é igual ao token
                        mensagem = "ERRO: identificador não pode ser o nome do programa. Linha: " + dados.getLinha();
                        return false;
                    } else                                      // Se não for o identificador do programa, aceita
                        return true;
                }
            mensagem = "ERRO: identificador não declarado no programa. Linha: " + dados.getLinha();
            return false;
        }
            
    }
    
    /**
     * Método auxilar para tratar fechamento de escopos. Quando um END reduz o contador
     * de escopos abertos e chega a zero (nenhum escopo aberto), esta função é chamada.
     * Ela retira da pilha todas variáveis existentes do escopo atual, só parando ao chegar
     * no caractere separador de escopos, depois também o remove.
     */
    private void fechaEscopo() {
        // Enquanto não chegar no separador do escopo atual, continua removendo os dados
        while (!pilhaEscopos.peek().getIdentificador().equals(txSep)) {
            System.out.println(pilhaEscopos.peek().getIdentificador() +"\t"+ pilhaEscopos.peek().getTipo());
            
            pilhaEscopos.pop();
        }
            
        // Ao terminar, chegou no separador, então remove-o
        pilhaEscopos.pop();
        System.out.println("$");
    }
    
    /**
     * Após o método Tipo() identificar qual o tipo atual a ser passado para a lista
     * de identificadores, chama este método. Ele busca na pilhaEscopos todas as variáveis
     * marcadas com tipo "+" para terem seu tipo atualizado pelo parâmetro deste método.
     * A marcação é feita pelos métodos ListaIdentificadores() e ListaIdentificadores2().
     * @param tipo String com tipo de variável identificado que deve ser usado para atualizar
     *             tipos das variáveis marcadas na pilha de escopos.
     */
    private void atualizaTipos(String tipo) {
        // OBS: ESTE MÉTODO NÃO É A MELHOR SOLUÇÃO MAS FOI O NECESSÁRIO PARA TERMINAR LOGO
        Stack<DadoPilha> aux = new Stack();
        
        // Remove todos itens da pilha de escopo, armazenando em outra auxiliar (invertendo a ordem, claro).
        // Cada dado com tipo marcado para substituição ("+"), o tem trocado pelo parâmetro "tipo".
        while (!pilhaEscopos.isEmpty()) {
            DadoPilha dado = pilhaEscopos.pop();
            if (dado.getTipo().equals("+"))
                dado.setTipo(tipo);
            aux.push(dado);
        }
        
        // Se a pilha não esvaziou, algo está errado
        if (!pilhaEscopos.isEmpty()) {
            mensagem = "ERRO: Falha interna. Pilha não está vazia para reinserção";
            return;
        }
        
        // Reinsere tudo na pilha original, com atualização dos tipos marcados. A ordem volta ao normal.
        while (!aux.isEmpty()) {
            pilhaEscopos.push(aux.pop());
        }
    }
    
    
    private boolean analisaPCT() {
        if  (pilhaControleTipo.isEmpty()) {
            System.out.println("ERRO: Faha interna. Pilha de Controle de Tipos está vazia");
            return false;
        }
        
        Stack<String> copia = (Stack<String>) pilhaControleTipo.clone();
        String topo = copia.pop();
        
        if  (copia.isEmpty())
            return true;
        
        String subtopo = copia.pop();
        
        for (int i = 0; i < tabelaOperacoes[0].length; i++) {
            if (topo.equals(tabelaOperacoes[0][i]) && subtopo.equals(tabelaOperacoes[1][i])) {
                atualizaPCT(tabelaOperacoes[2][i]);
                return true;
            }
        }
        
        mensagem = "ERRO: Incompatibilidade de tipos na expressão. Linha " + dados.getLinha();
        return false;
    }
    
    
    private void atualizaPCT(String tipoResultante) {
        pilhaControleTipo.pop();    // Remover topo
        pilhaControleTipo.pop();    // Remover subtopo
        pilhaControleTipo.push(tipoResultante);  // Adicionar tipo resultante no topo
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
            
            if (tokens.length != 3) {
                System.out.println("Erro: A tabela de tokens do compilador Léxico não possui 3 tokens por linha");
                return false;
            }
            
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
        
        mensagem = "Analisador Sintático/Semântico: Programa compilado com sucesso.";
        iterator = linha.listIterator();    // Atualiza o iterador da lista
        pilhaEscopos = new Stack<>();       // Reinicia a pilha para nova compilação
        pilhaControleTipo = new Stack<>();       // Reinicia a pilha para nova compilação
        
        // Concebido inicialmente para tratar apenas palavras-reservadas minúsculas
        // Processa a primeira parte da gramática. As produções de 'PROGRAMA'
        if (iterator.hasNext())
            dados = iterator.next();
        else {
             mensagem = "ERRO: programa vazio";
            return false;
        }
        
        if (dados.getToken().equals("program")) {
            pilhaEscopos.push(new DadoPilha(txSep));   // Inicia empilhamento pelo programa
            
            if (iterator.hasNext()) dados = iterator.next(); else return false;
            if (dados.getIdent().equals(txIdent)) {
                pilhaEscopos.push(new DadoPilha(dados.getToken(), "program"));    // Empilha nome do programa e seu tipo
                nomePrograma = dados.getToken();        // e salva para consulta futura
                
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
                            
                            
                            //while(!pilhaControleTipo.isEmpty())
                                System.out.println("\nPilha de Controle de Tipos:\n" + pilhaControleTipo.toString() + "\n");
                            
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
        auxIdentificadores = true;      // Identificadores neste momento devem ser marcados como tipo "+"
        if (ListaIdentificadores()) {
            if (dados.getToken().equals(":")) {
                auxIdentificadores = false; // Libera marcação de identificadores
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
                auxIdentificadores = false; // Libera marcação de identificadores
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
            auxIdentificadores = true;      // Identificadores neste momento devem ser marcados como tipo "+"
            if (ListaIdentificadores()) {
                if (dados.getToken().equals(":")) {
                    auxIdentificadores = false; // Libera marcação de identificadores
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
            String aux = auxIdentificadores ? "+" : "";  // Se auxiliar é true, marque tipo como "+" para definir depois
            if (!analisaPilha(dados.getToken(), aux))    // Se não adicionar o identificador ou não estiver declarado, retorna a falha
                return false;
            
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
                String aux = auxIdentificadores ? "+" : "";  // Se auxiliar é true, marque tipo como "+" para definir depois
                if (!analisaPilha(dados.getToken(), aux))    // Se não adicionar o identificador ou não estiver declarado, retorna a falha
                    return false;
                
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
            atualizaTipos(dados.getToken());
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
                if (!analisaPilha(dados.getToken(), "procedure"))    // Se não adicionar o identificador ou não estiver declarado, retorna a falha
                    return false;
                pilhaEscopos.push(new DadoPilha(txSep));   // Ao adicionar o identificador de uma procedure, adiciona separador
                
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
        auxIdentificadores = true;      // Identificadores neste momento devem ser marcados como tipo "+"
        if (ListaIdentificadores()) {
            if (dados.getToken().equals(":")) {
                auxIdentificadores = false; // Libera marcação de identificadores
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
            auxIdentificadores = true;      // Identificadores neste momento devem ser marcados como tipo "+"
            if (ListaIdentificadores()) {
                if (dados.getToken().equals(":")) {
                    auxIdentificadores = false; // Libera marcação de identificadores
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
            nivelEscopo++;
            if (iterator.hasNext()) dados = iterator.next(); else return false;
            if (ComandosOpcionais()) {
                if (dados.getToken().equals("end")) {
                    nivelEscopo--;
                    if (nivelEscopo == 0)
                        fechaEscopo();
                    
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
        if (dados.getToken().equals("if") || dados.getToken().equals("begin") || dados.getToken().equals("repeat") || dados.getToken().equals("while") || dados.getIdent().equals(txIdent)) {
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
            if (!analisaPilha(dados.getToken(), ""))    // Se não adicionar o identificador ou não estiver declarado, retorna a falha
                return false;
            
            // TAMBÉM CORRIGIR DEPOIS ESTE TRECHO DE CÓDIGO
            Stack<DadoPilha> copia = (Stack<DadoPilha>) pilhaEscopos.clone();
            String temp;
            while(!copia.isEmpty())
                if (!copia.peek().getIdentificador().equals(dados.getToken()))
                    copia.pop();
                else
                    break;
            temp = copia.pop().getTipo();     // Registra o tipo do identificador para inserir na PCT e comparar com resultante da Expressão
            
            if (iterator.hasNext()) dados = iterator.next(); else return false;
            if (dados.getToken().equals(":=")) {
                if (iterator.hasNext()) dados = iterator.next(); else return false;
                
                //return Expressao();
                if (Expressao()) {
                    pilhaControleTipo.push(temp);
                    return analisaPCT();
                } else
                    return false;                
                    
            } else if (dados.getToken().equals("(")) {  // Deve mandar o próprio parênteses, não o próximo token
                return AtivacaoProcedimento();
            } else {
                return true;
            }
        } else if (dados.getToken().equals("repeat")) {
            if (iterator.hasNext()) dados = iterator.next(); else return false;
            if (Comando()) {
                if (dados.getToken().equals("until")) {
                    if (iterator.hasNext()) dados = iterator.next(); else return false;
                        return Expressao();
                } else {
                    mensagem = "ERRO: esperado 'until' para comando 'repeat'. Linha: " + dados.getLinha();
                    return false;
                }
            } else {
                return false;
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
            String temp = pilhaControleTipo.peek();     // Salva tipo resultante da primeira expressão para comparar
            if (OpRelacional()) {
                //return ExpressaoSimples();
                if (ExpressaoSimples()) {
                    pilhaControleTipo.push(temp);       // Empilha tipo resultante anterior para commparar
                    return analisaPCT();
                } else
                    return false;
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
            if (!analisaPCT())
                return false;
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
            if (!analisaPCT())
                return false;
            return true;
        }
    }
    
    private boolean Fator() {
        if (dados.getIdent().equals(txNumInt)) {
            pilhaControleTipo.push("integer");  // Adicionar na PcT o tipo do fator para verificação da expressão
            if (iterator.hasNext()) dados = iterator.next(); else return true;
            return true;
        } else if (dados.getIdent().equals(txNumReal)) {
            pilhaControleTipo.push("real");  // Adicionar na PcT o tipo do fator para verificação da expressão
            if (iterator.hasNext()) dados = iterator.next(); else return true;
            return true;
        } else if (dados.getIdent().equals(txNumCompx)) {
            if (iterator.hasNext()) dados = iterator.next(); else return true;
            return true;
        } else if (dados.getToken().equals("true")) {
            pilhaControleTipo.push("boolean");  // Adicionar na PcT o tipo do fator para verificação da expressão
            if (iterator.hasNext()) dados = iterator.next(); else return true;
            return true;
        } else if (dados.getToken().equals("false")) {
            pilhaControleTipo.push("boolean");  // Adicionar na PcT o tipo do fator para verificação da expressão
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
            if (!analisaPilha(dados.getToken(), ""))    // Se não adicionar o identificador ou não estiver declarado, retorna a falha
                return false;
            
            // MELHORAR TAMBÉM ESTA SOLUÇÃO. FOI O NECESSÁRIO PARA RESOLVER LOGO
            // Adicionar na PcT o tipo do fator (identificador) para verificação da expressão
            Stack<DadoPilha> temp = (Stack<DadoPilha>) pilhaEscopos.clone();
            while(!temp.isEmpty()) {
                DadoPilha aux = temp.pop();
                if (aux.getIdentificador().equals( dados.getToken() )) {
                    pilhaControleTipo.push(aux.getTipo());
                    break;
                }
            }
            
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
