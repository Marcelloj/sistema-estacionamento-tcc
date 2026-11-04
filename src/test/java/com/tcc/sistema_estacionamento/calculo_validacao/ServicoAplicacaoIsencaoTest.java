
package com.tcc.sistema_estacionamento.calculo_validacao;

import com.tcc.sistema_estacionamento.modelo.RegistroEstacionamento;
import com.tcc.sistema_estacionamento.modelo.StatusPagamento;
import com.tcc.sistema_estacionamento.nota_fiscal.NotaFiscalSimulada;
import com.tcc.sistema_estacionamento.nota_fiscal.NotaFiscalSimuladaRepositorio;
import com.tcc.sistema_estacionamento.repositorio.RegistroEstacionamentoRepositorio;
import org.springframework.test.util.ReflectionTestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServicoAplicacaoIsencaoTest {

    private NotaFiscalSimuladaRepositorio repositorioNotas;
    private RegistroEstacionamentoRepositorio repositorioRegistros;
    private ServicoCalculoPermanencia servicoCalculo;
    private ServicoAplicacaoIsencao servico;

    private NotaFiscalSimulada nota;
    private RegistroEstacionamento registro;

    @BeforeEach
    void prepararTeste() {

        repositorioNotas = mock(NotaFiscalSimuladaRepositorio.class);
        repositorioRegistros = mock(RegistroEstacionamentoRepositorio.class);
        servicoCalculo = mock(ServicoCalculoPermanencia.class);

        ServicoValidacaoFiscal validacao =
                new ServicoValidacaoFiscal(repositorioNotas);

        servico = new ServicoAplicacaoIsencao(
                repositorioNotas,
                repositorioRegistros,
                validacao,
                servicoCalculo
        );

        nota = new NotaFiscalSimulada();
        nota.setCodigoNota("NOTA-123");
        nota.setValorCompra(new BigDecimal("75.00"));
        nota.setUtilizada(false);

        registro = new RegistroEstacionamento();
        ReflectionTestUtils.setField(registro, "id", 1L);
        registro.setPlaca("ABC1D23");

        when(repositorioNotas.buscarParaUtilizacao("NOTA-123"))
                .thenReturn(Optional.of(nota));

        when(repositorioNotas.findByCodigoNota("NOTA-123"))
                .thenReturn(Optional.of(nota));

        when(repositorioRegistros.findById(1L))
                .thenReturn(Optional.of(registro));
    }

    @Test
    void deveAplicarIsencaoEVincularNota() {

        when(servicoCalculo.calcularTarifa(1L))
                .thenReturn(new BigDecimal("20.00"));

        RegistroEstacionamento resultado =
                servico.aplicarIsencao("NOTA-123", 1L);

        assertEquals(StatusPagamento.ISENTO,
                resultado.getStatusPagamento());

                assertEquals(20.00, resultado.getValorTarifaOriginal().doubleValue(), 0.001);

                assertEquals(0.00, resultado.getValorTotal().doubleValue(), 0.001);

        assertTrue(nota.isUtilizada());

        assertEquals(1L, nota.getIdRegistroUtilizacao());

        verify(repositorioNotas).save(nota);
        verify(repositorioRegistros).save(registro);
    }

    @Test
    void deveImpedirReutilizacaoDaNota() {

        nota.setUtilizada(true);

        assertThrows(IllegalStateException.class, () ->
                servico.aplicarIsencao("NOTA-123", 1L)
        );

        verify(repositorioNotas, never()).save(any());
        verify(repositorioRegistros, never()).save(any());
    }

    @Test
    void deveRejeitarCompraInferiorA50Reais() {

        nota.setValorCompra(new BigDecimal("49.99"));

        assertThrows(IllegalArgumentException.class, () ->
                servico.aplicarIsencao("NOTA-123", 1L)
        );

        assertFalse(nota.isUtilizada());

        verify(repositorioNotas, never()).save(any());
        verify(repositorioRegistros, never()).save(any());
    }
}
