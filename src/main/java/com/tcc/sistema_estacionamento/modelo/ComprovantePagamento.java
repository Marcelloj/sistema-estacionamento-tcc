package com.tcc.sistema_estacionamento.modelo;

import java.time.LocalDateTime;

public class ComprovantePagamento {

    private final String codigoTransacao;
    private final Long idRegistro;
    private final String placa;
    private final MetodoPagamento metodoPagamento;
    private final double valorTarifa;
    private final double valorPago;
    private final double troco;
    private final LocalDateTime horarioPagamento;
    private final String mensagem;

    public ComprovantePagamento(String codigoTransacao,
                                Long idRegistro,
                                String placa,
                                MetodoPagamento metodoPagamento,
                                double valorTarifa,
                                double valorPago,
                                double troco,
                                LocalDateTime horarioPagamento,
                                String mensagem) {
        this.codigoTransacao = codigoTransacao;
        this.idRegistro = idRegistro;
        this.placa = placa;
        this.metodoPagamento = metodoPagamento;
        this.valorTarifa = valorTarifa;
        this.valorPago = valorPago;
        this.troco = troco;
        this.horarioPagamento = horarioPagamento;
        this.mensagem = mensagem;
    }

    public String getCodigoTransacao() {
        return codigoTransacao;
    }

    public Long getIdRegistro() {
        return idRegistro;
    }

    public String getPlaca() {
        return placa;
    }

    public MetodoPagamento getMetodoPagamento() {
        return metodoPagamento;
    }

    public double getValorTarifa() {
        return valorTarifa;
    }

    public double getValorPago() {
        return valorPago;
    }

    public double getTroco() {
        return troco;
    }

    public LocalDateTime getHorarioPagamento() {
        return horarioPagamento;
    }

    public String getMensagem() {
        return mensagem;
    }
}
