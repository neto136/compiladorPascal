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
public class DadoPilha {
    private String identificador;
    private String tipo;
    
    public DadoPilha() {
        identificador = "";
        tipo = "";
    }
    
    public DadoPilha(String id) {
        identificador = id;
        tipo = "";
    }
    
    public DadoPilha(String id, String tipo) {
        identificador = id;
        this.tipo = tipo;
    }

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
          
}
