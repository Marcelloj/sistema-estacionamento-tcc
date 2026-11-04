
package com.tcc.sistema_estacionamento.calculo_validacao;

import com.tcc.sistema_estacionamento.repositorio.RegistroEstacionamentoRepositorio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class ServicoCalculoPermanenciaTest {

    private ServicoCalculoPermanencia servico;
    private LocalDateTime entrada;

    @BeforeEach
    void prepararTeste() {

        RegistroEstacionamentoRepositorio repositorio =
                mock(RegistroEstacionamentoRepositorio.class);

        servico = new ServicoCalculoPermanencia(repositorio);

        entrada = LocalDateTime.of(2026, 11, 4, 10, 0);
    }

    @Test
    void deveSerGratuitoAte15Minutos() {

        LocalDateTime saida = entrada.plusMinutes(15);

        BigDecimal valor = servico.calcularTarifa(entrada, saida);

        assertEquals(new BigDecimal("0.00"), valor);
    }

    @Test
    void deveCobrar10ReaisApos16Minutos() {

        LocalDateTime saida = entrada.plusMinutes(16);

        BigDecimal valor = servico.calcularTarifa(entrada, saida);

        assertEquals(new BigDecimal("10.00"), valor);
    }

    @Test
    void deveCobrar10ReaisAposUmaHoraE15Minutos() {

        LocalDateTime saida = entrada.plusMinutes(75);

        BigDecimal valor = servico.calcularTarifa(entrada, saida);

        assertEquals(new BigDecimal("10.00"), valor);
    }

    @Test
    void deveCobrar20ReaisAposUmaHoraE16Minutos() {

        LocalDateTime saida = entrada.plusMinutes(76);

        BigDecimal valor = servico.calcularTarifa(entrada, saida);

        assertEquals(new BigDecimal("20.00"), valor);
    }

    @Test
    void deveCobrar30ReaisAposDuasHorasE30Minutos() {

        LocalDateTime saida = entrada.plusMinutes(150);

        BigDecimal valor = servico.calcularTarifa(entrada, saida);

        assertEquals(new BigDecimal("30.00"), valor);
    }
}
