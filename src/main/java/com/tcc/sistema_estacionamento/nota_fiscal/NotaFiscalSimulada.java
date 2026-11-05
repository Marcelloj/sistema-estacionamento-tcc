
package com.tcc.sistema_estacionamento.nota_fiscal;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "NOTA_FISCAL_SIMULADA")
public class NotaFiscalSimulada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Código único utilizado para identificar a nota no QR Code
    @Column(nullable = false, unique = true, length = 36)
    private String codigoNota;

    // Valor da compra fictícia
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorCompra;

    // Data e horário em que a nota foi criada
    @Column(nullable = false)
    private LocalDateTime horarioEmissao;

    // Indica se a nota já foi utilizada para obter um benefício
    @Column(nullable = false)
    private boolean utilizada = false;

    // Registro do estacionamento beneficiado, se houver
    private Long idRegistroUtilizacao;

    public NotaFiscalSimulada() {
    }

    public Long getId() {
        return id;
    }

    public String getCodigoNota() {
        return codigoNota;
    }

    public void setCodigoNota(String codigoNota) {
        this.codigoNota = codigoNota;
    }

    public BigDecimal getValorCompra() {
        return valorCompra;
    }

    public void setValorCompra(BigDecimal valorCompra) {
        this.valorCompra = valorCompra;
    }

    public LocalDateTime getHorarioEmissao() {
        return horarioEmissao;
    }

    public void setHorarioEmissao(LocalDateTime horarioEmissao) {
        this.horarioEmissao = horarioEmissao;
    }

    public boolean isUtilizada() {
        return utilizada;
    }

    public void setUtilizada(boolean utilizada) {
        this.utilizada = utilizada;
    }

    public Long getIdRegistroUtilizacao() {
        return idRegistroUtilizacao;
    }

    public void setIdRegistroUtilizacao(Long idRegistroUtilizacao) {
        this.idRegistroUtilizacao = idRegistroUtilizacao;
    }
}
