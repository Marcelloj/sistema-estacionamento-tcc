package com.tcc.sistema_estacionamento.pagamento_saida;

import com.tcc.sistema_estacionamento.excecao.RegistroNaoEncontradoException;
import com.tcc.sistema_estacionamento.modelo.*;
import com.tcc.sistema_estacionamento.repositorio.RegistroEstacionamentoRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - Módulo Pagamento, Saída e Cancela Virtual")
class ServicoPagamentoSaidaTest {

    @Mock
    private RegistroEstacionamentoRepositorio repositorio;

    @InjectMocks
    private ServicoPagamentoSaida servico;

    private RegistroEstacionamento registroMock;

    @BeforeEach
    void setUp() {
        registroMock = new RegistroEstacionamento();
        registroMock.setId(1L);
        registroMock.setPlaca("UNA-2026");
        registroMock.setHorarioEntrada(LocalDateTime.now().minusHours(2));
        registroMock.setValorTarifaOriginal(15.0);
        registroMock.setValorTarifa(15.0);
        registroMock.setStatusPagamento(StatusPagamento.PENDENTE);
        registroMock.setStatusVeiculo(StatusVeiculo.ESTACIONADO);
    }

    @Test
    void deveProcessarPagamentoComSucesso() {
        when(repositorio.findById(1L)).thenReturn(Optional.of(registroMock));
        boolean pago = servico.processarPagamento(1L, 15.0);
        assertTrue(pago);
        assertEquals(StatusPagamento.PAGO, registroMock.getStatusPagamento());
        verify(repositorio, times(1)).save(registroMock);
    }

    @Test
    void naoDevePermitirSaidaSemPagamento() {
        when(repositorio.findById(1L)).thenReturn(Optional.of(registroMock));
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            servico.tentarLiberarCancela(1L);
        });
        assertTrue(exception.getMessage().contains("Saída não autorizada"));
    }

    @Test
    void deveLiberarCancelaQuandoPago() {
        registroMock.setStatusPagamento(StatusPagamento.PAGO);
        when(repositorio.findById(1L)).thenReturn(Optional.of(registroMock));
        boolean liberado = servico.tentarLiberarCancela(1L);
        assertTrue(liberado);
        assertEquals(StatusVeiculo.LIBERADO, registroMock.getStatusVeiculo());
    }

    @Test
    void deveRegistrarSaidaQuandoLiberado() {
        registroMock.setStatusVeiculo(StatusVeiculo.LIBERADO);
        when(repositorio.findById(1L)).thenReturn(Optional.of(registroMock));
        servico.registrarSaida(1L);
        assertEquals(StatusVeiculo.FINALIZADO, registroMock.getStatusVeiculo());
        assertNotNull(registroMock.getHorarioSaida());
        verify(repositorio, times(1)).save(registroMock);
    }
}