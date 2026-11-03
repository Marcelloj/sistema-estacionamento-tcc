package com.tcc.sistema_estacionamento.excecao;

public class ValorInsuficienteException extends RuntimeException {
    public ValorInsuficienteException(String mensagem) {
        super(mensagem);
    }
}
