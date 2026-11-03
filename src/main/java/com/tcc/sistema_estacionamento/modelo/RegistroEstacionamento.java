
package com.tcc.sistema_estacionamento.modelo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "REGISTRO_ESTACIONAMENTO")
public class RegistroEstacionamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String placa;

    @Column(nullable = false)
    private LocalDateTime horarioEntrada;

    private LocalDateTime horarioSaida;
    private LocalDateTime horarioPagamento;
    private LocalDateTime horarioLiberacaoCancela;

    @Column(precision = 10, scale = 2)
    private BigDecimal valorTarifaOriginal;

    @Column(precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(precision = 10, scale = 2)
    private BigDecimal valorPago;

    @Column(precision = 10, scale = 2)
    private BigDecimal troco;  // Não precisa 

    @Enumerated(EnumType.STRING)
    private StatusPagamento statusPagamento = StatusPagamento.PENDENTE;

    @Enumerated(EnumType.STRING)
    private StatusVeiculo statusVeiculo = StatusVeiculo.ESTACIONADO;

    @Enumerated(EnumType.STRING)
    private MetodoPagamento metodoPagamento;

    private String codigoTransacao;

    public RegistroEstacionamento() {
    }

    public Long getId() {
        return id;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public LocalDateTime getHorarioEntrada() {
        return horarioEntrada;
    }

    public void setHorarioEntrada(LocalDateTime horarioEntrada) {
        this.horarioEntrada = horarioEntrada;
    }

    public LocalDateTime getHorarioSaida() {
        return horarioSaida;
    }

    public void setHorarioSaida(LocalDateTime horarioSaida) {
        this.horarioSaida = horarioSaida;
    }

    public LocalDateTime getHorarioPagamento() {
        return horarioPagamento;
    }

    public void setHorarioPagamento(LocalDateTime horarioPagamento) {
        this.horarioPagamento = horarioPagamento;
    }

    public LocalDateTime getHorarioLiberacaoCancela() {
        return horarioLiberacaoCancela;
    }

    public void setHorarioLiberacaoCancela(LocalDateTime horarioLiberacaoCancela) {
        this.horarioLiberacaoCancela = horarioLiberacaoCancela;
    }

    public BigDecimal getValorTarifaOriginal() {
        return valorTarifaOriginal;
    }

    public void setValorTarifaOriginal(BigDecimal valorTarifaOriginal) {
        this.valorTarifaOriginal = valorTarifaOriginal;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public BigDecimal getValorPago() {
        return valorPago;
    }

    public void setValorPago(BigDecimal valorPago) {
        this.valorPago = valorPago;
    }

    public BigDecimal getTroco() {
        return troco;
    }

    public void setTroco(BigDecimal troco) {
        this.troco = troco;
    }

    public StatusPagamento getStatusPagamento() {
        return statusPagamento;
    }

    public void setStatusPagamento(StatusPagamento statusPagamento) {
        this.statusPagamento = statusPagamento;
    }

    public StatusVeiculo getStatusVeiculo() {
        return statusVeiculo;
    }

    public void setStatusVeiculo(StatusVeiculo statusVeiculo) {
        this.statusVeiculo = statusVeiculo;
    }

    public MetodoPagamento getMetodoPagamento() {
        return metodoPagamento;
    }

    public void setMetodoPagamento(MetodoPagamento metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }

    public String getCodigoTransacao() {
        return codigoTransacao;
    }

    public void setCodigoTransacao(String codigoTransacao) {
        this.codigoTransacao = codigoTransacao;
    }
}
