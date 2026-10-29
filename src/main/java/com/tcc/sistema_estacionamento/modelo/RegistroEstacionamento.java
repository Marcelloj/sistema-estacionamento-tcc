/*
 * RESPONSÁVEL: Marcello
 *
 * OBJETIVO:
 * Representar o registro de permanência de
 * um veículo no estacionamento.
 *
 * TAREFAS:
 * 1. Definir os atributos do registro.
 * 2. Configurar a entidade com JPA.
 * 3. Criar os métodos necessários.
 */


package com.tcc.sistema_estacionamento.modelo;

import java.time.LocalDateTime;
import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;


 @Entity 
public class RegistroEstacionamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id; // Identificador único do registro
    private String placa; // Placa do veiculo
    private LocalDateTime horarioEntrada; // Data e hora da entrada
    private LocalDateTime horarioSaida; // Data e hora da saída
    private BigDecimal valorTotal; // Valor cobrado pelo estacionamento

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

    public LocalDateTime getHorarioSaida() {
        return horarioSaida;
    }

    public void setHorarioEntrada(LocalDateTime horarioEntrada) {
        this.horarioEntrada = horarioEntrada;
    }

    public void setHorarioSaida(LocalDateTime horarioSaida) {
        this.horarioSaida = horarioSaida;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

}