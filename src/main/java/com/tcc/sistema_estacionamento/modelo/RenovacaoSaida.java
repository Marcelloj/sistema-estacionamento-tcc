
package com.tcc.sistema_estacionamento.modelo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "RENOVACAO_SAIDA")
public class RenovacaoSaida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long idRegistro;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDateTime horarioPagamento;

    @Column(nullable = false)
    private LocalDateTime liberacaoAnterior;

    @Column(nullable = false)
    private LocalDateTime validadeAte;

    @Column(nullable = false, unique = true)
    private String codigoTransacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetodoPagamento metodoPagamento;

    protected RenovacaoSaida() {
    }

    public RenovacaoSaida(
            Long idRegistro,
            BigDecimal valor,
            LocalDateTime horarioPagamento,
            LocalDateTime liberacaoAnterior,
            LocalDateTime validadeAte,
            String codigoTransacao) {

        this.idRegistro = idRegistro;
        this.valor = valor;
        this.horarioPagamento = horarioPagamento;
        this.liberacaoAnterior = liberacaoAnterior;
        this.validadeAte = validadeAte;
        this.codigoTransacao = codigoTransacao;
        this.metodoPagamento = MetodoPagamento.PIX;
    }

    public Long getId() {
        return id;
    }

    public Long getIdRegistro() {
        return idRegistro;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public LocalDateTime getHorarioPagamento() {
        return horarioPagamento;
    }

    public LocalDateTime getLiberacaoAnterior() {
        return liberacaoAnterior;
    }

    public LocalDateTime getValidadeAte() {
        return validadeAte;
    }

    public String getCodigoTransacao() {
        return codigoTransacao;
    }

    public MetodoPagamento getMetodoPagamento() {
        return metodoPagamento;
    }
}
