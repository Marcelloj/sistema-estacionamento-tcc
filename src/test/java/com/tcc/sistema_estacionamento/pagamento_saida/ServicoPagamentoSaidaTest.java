
package com.tcc.sistema_estacionamento.pagamento_saida;

import com.tcc.sistema_estacionamento.excecao.RegistroNaoEncontradoException;
import com.tcc.sistema_estacionamento.excecao.ValorInsuficienteException;
import com.tcc.sistema_estacionamento.modelo.RegistroEstacionamento;
import com.tcc.sistema_estacionamento.modelo.StatusPagamento;
import com.tcc.sistema_estacionamento.modelo.StatusVeiculo;
import com.tcc.sistema_estacionamento.pagamento_saida.ServicoPagamentoSaida;
import com.tcc.sistema_estacionamento.repositorio.RegistroEstacionamentoRepositorio;
import com.tcc.sistema_estacionamento.calculo_validacao.ServicoCalculoPermanencia;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários - Pagamento e Saída")
class ServicoPagamentoSaidaTest {

    @Mock
    private RegistroEstacionamentoRepositorio repositorio;

    @Mock
    private ServicoCalculoPermanencia servicoCalculo;

    @InjectMocks
    private ServicoPagamentoSaida servico;

    private RegistroEstacionamento registroMock;

    @BeforeEach
    void prepararTeste() {

        registroMock = new RegistroEstacionamento();

        // Define um ID apenas para o ambiente de teste.
        // Não precisamos adicionar setId() na entidade.
        ReflectionTestUtils.setField(
                registroMock, "id", 1L
        );

        // Placa válida no padrão Mercosul.
        registroMock.setPlaca("ABC1D23");

        registroMock.setHorarioEntrada(
                LocalDateTime.now().minusHours(2)
        );

        registroMock.setValorTarifaOriginal(
                new BigDecimal("15.00")
        );

        registroMock.setValorTotal(
                new BigDecimal("15.00")
        );

        registroMock.setStatusPagamento(
                StatusPagamento.PENDENTE
        );

        registroMock.setStatusVeiculo(
                StatusVeiculo.ESTACIONADO
        );
    }

    @Test
    @DisplayName("Deve processar pagamento com sucesso")
    void deveProcessarPagamentoComSucesso() {

        when(servicoCalculo.calcularTarifa(1L))
        .thenReturn(new BigDecimal("15.00"));

        when(repositorio.findById(1L))
                .thenReturn(Optional.of(registroMock));

        boolean pago = servico.processarPagamento(
                1L, 15.00
        );

        assertTrue(pago);

        assertEquals(
                StatusPagamento.PAGO,
                registroMock.getStatusPagamento()
        );

        assertEquals(
                0,
                registroMock.getValorPago()
                        .compareTo(new BigDecimal("15.00"))
        );

        verify(repositorio).save(registroMock);
    }

    @Test
    @DisplayName("Não deve permitir pagamento insuficiente")
    void naoDevePermitirPagamentoInsuficiente() {

        when(servicoCalculo.calcularTarifa(1L))
        .thenReturn(new BigDecimal("15.00"));

        when(repositorio.findById(1L))
                .thenReturn(Optional.of(registroMock));

        assertThrows(
                ValorInsuficienteException.class,
                () -> servico.processarPagamento(1L, 10.00)
        );

        assertEquals(
                StatusPagamento.PENDENTE,
                registroMock.getStatusPagamento()
        );

        verify(repositorio, never()).save(any());
    }

    @Test
    @DisplayName("Não deve permitir saída sem pagamento")
    void naoDevePermitirSaidaSemPagamento() {

        when(repositorio.findById(1L))
                .thenReturn(Optional.of(registroMock));

        IllegalStateException erro = assertThrows(
                IllegalStateException.class,
                () -> servico.tentarLiberarCancela(1L)
        );

        assertTrue(
                erro.getMessage().contains("Saída não autorizada")
        );

        verify(repositorio, never()).save(any());
    }

    @Test
    @DisplayName("Deve liberar cancela quando pago")
    void deveLiberarCancelaQuandoPago() {

        registroMock.setStatusPagamento(
                StatusPagamento.PAGO
        );

        registroMock.setValorPago(
                new BigDecimal("15.00")
        );

        when(repositorio.findById(1L))
                .thenReturn(Optional.of(registroMock));

        boolean liberado = servico.tentarLiberarCancela(1L);

        assertTrue(liberado);

        assertEquals(
                StatusVeiculo.LIBERADO,
                registroMock.getStatusVeiculo()
        );

        assertNotNull(
                registroMock.getHorarioLiberacaoCancela()
        );

        verify(repositorio).save(registroMock);
    }

    @Test
    @DisplayName("Deve liberar cancela quando isento")
    void deveLiberarCancelaQuandoIsento() {

        registroMock.setStatusPagamento(
                StatusPagamento.ISENTO
        );

        registroMock.setValorTotal(BigDecimal.ZERO);

        when(repositorio.findById(1L))
                .thenReturn(Optional.of(registroMock));

        boolean liberado = servico.tentarLiberarCancela(1L);

        assertTrue(liberado);

        assertEquals(
                StatusVeiculo.LIBERADO,
                registroMock.getStatusVeiculo()
        );
    }

    @Test
    @DisplayName("Não deve conceder gratuidade sem validação")
    void naoDeveConcederGratuidadeIndevida() {

        registroMock.setValorTotal(BigDecimal.ZERO);

        // Mesmo com valor zero, o status continua PENDENTE.
        when(repositorio.findById(1L))
                .thenReturn(Optional.of(registroMock));

        assertThrows(
                IllegalStateException.class,
                () -> servico.tentarLiberarCancela(1L)
        );

        assertEquals(
                StatusPagamento.PENDENTE,
                registroMock.getStatusPagamento()
        );
    }

    @Test
    @DisplayName("Deve registrar saída quando liberado")
    void deveRegistrarSaidaQuandoLiberado() {

        registroMock.setStatusVeiculo(
                StatusVeiculo.LIBERADO
        );

        when(repositorio.findById(1L))
                .thenReturn(Optional.of(registroMock));

        servico.registrarSaida(1L);

        assertEquals(
                StatusVeiculo.FINALIZADO,
                registroMock.getStatusVeiculo()
        );

        assertNotNull(registroMock.getHorarioSaida());

        verify(repositorio).save(registroMock);
    }

    @Test
    @DisplayName("Não deve registrar saída sem liberar cancela")
    void naoDeveRegistrarSaidaSemLiberacao() {

        when(repositorio.findById(1L))
                .thenReturn(Optional.of(registroMock));

        assertThrows(
                IllegalStateException.class,
                () -> servico.registrarSaida(1L)
        );

        assertNull(registroMock.getHorarioSaida());

        verify(repositorio, never()).save(any());
    }

    @Test
    @DisplayName("Não deve permitir pagamento duplicado")
    void naoDevePermitirPagamentoDuplicado() {

        registroMock.setStatusPagamento(
                StatusPagamento.PAGO
        );

        when(repositorio.findById(1L))
                .thenReturn(Optional.of(registroMock));

        assertThrows(
                IllegalStateException.class,
                () -> servico.processarPagamento(1L, 15.00)
        );

        verify(repositorio, never()).save(any());
    }

    @Test
    @DisplayName("Deve informar quando registro não existe")
    void deveInformarRegistroNaoEncontrado() {

        when(repositorio.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                RegistroNaoEncontradoException.class,
                () -> servico.tentarLiberarCancela(999L)
        );
    }
}
