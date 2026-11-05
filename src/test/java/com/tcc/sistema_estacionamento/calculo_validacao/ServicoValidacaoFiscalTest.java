
package com.tcc.sistema_estacionamento.calculo_validacao;

import com.tcc.sistema_estacionamento.nota_fiscal.NotaFiscalSimulada;
import com.tcc.sistema_estacionamento.nota_fiscal.NotaFiscalSimuladaRepositorio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ServicoValidacaoFiscalTest {

    private ServicoValidacaoFiscal servico;
    private NotaFiscalSimuladaRepositorio repositorio;

    @BeforeEach
    void prepararTeste() {
        repositorio = mock(NotaFiscalSimuladaRepositorio.class);
        servico = new ServicoValidacaoFiscal(repositorio);
    }

    // Cria uma nota ficticia para os testes
    private NotaFiscalSimulada criarNota(
            String valor, boolean utilizada) {

        NotaFiscalSimulada nota = new NotaFiscalSimulada();

        nota.setCodigoNota("NOTA-123");
        nota.setValorCompra(new BigDecimal(valor));
        nota.setUtilizada(utilizada);

        return nota;
    }

    @Test
    void deveAceitarCompraDe50Reais() {

        NotaFiscalSimulada nota = criarNota("50.00", false);

        when(repositorio.findByCodigoNota("NOTA-123"))
                .thenReturn(Optional.of(nota));

        NotaFiscalSimulada resultado =
                servico.validarNota("NOTA-123");

        assertEquals(nota, resultado);
    }

    @Test
    void deveAceitarCompraAcimaDe50Reais() {

        NotaFiscalSimulada nota = criarNota("100.00", false);

        when(repositorio.findByCodigoNota("NOTA-123"))
                .thenReturn(Optional.of(nota));

        assertDoesNotThrow(() ->
                servico.validarNota("NOTA-123")
        );
    }

    @Test
    void deveRejeitarCompraInferiorA50Reais() {

        NotaFiscalSimulada nota = criarNota("35.00", false);

        when(repositorio.findByCodigoNota("NOTA-123"))
                .thenReturn(Optional.of(nota));

        assertThrows(IllegalArgumentException.class, () ->
                servico.validarNota("NOTA-123")
        );
    }

    @Test
    void deveRejeitarNotaJaUtilizada() {

        NotaFiscalSimulada nota = criarNota("100.00", true);

        when(repositorio.findByCodigoNota("NOTA-123"))
                .thenReturn(Optional.of(nota));

        assertThrows(IllegalStateException.class, () ->
                servico.validarNota("NOTA-123")
        );
    }

    @Test
    void deveRejeitarNotaInexistente() {

        when(repositorio.findByCodigoNota("NOTA-999"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                servico.validarNota("NOTA-999")
        );
    }

    @Test
    void deveRejeitarCodigoVazio() {

        assertThrows(IllegalArgumentException.class, () ->
                servico.validarNota("")
        );
    }
}

