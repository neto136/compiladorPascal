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
public class DadoLinha {
    private String token;
    private String identificador;
    private String linha;
    
    public DadoLinha () {
        token = "";
        identificador = "";
        linha = "";
    }
    
    public DadoLinha (String tok, String ident, String lin) {
        token = tok;
        identificador = ident;
        linha = lin;
    }
    
    public String getToken() {
        return token;
    }
    
    public String getIdent() {
        return identificador;
    }
    
    public String getLinha() {
        return linha;
    }
    
    public void setToken(String tok) {
        token = tok;
    }
    
    public void setIdent(String ident) {
        identificador = ident;
    }
    
    public void setLinha(String lin) {
        linha = lin;
    }
    
}
