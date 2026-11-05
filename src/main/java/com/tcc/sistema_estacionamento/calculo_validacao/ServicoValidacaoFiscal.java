
package com.tcc.sistema_estacionamento.calculo_validacao;

import com.tcc.sistema_estacionamento.nota_fiscal.NotaFiscalSimulada;
import com.tcc.sistema_estacionamento.nota_fiscal.NotaFiscalSimuladaRepositorio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ServicoValidacaoFiscal {

    // Valor minimo para obter estacionamento gratuito
    private static final BigDecimal VALOR_MINIMO =
            new BigDecimal("50.00");

    private final NotaFiscalSimuladaRepositorio repositorio;

    // Construtor
    public ServicoValidacaoFiscal(
            NotaFiscalSimuladaRepositorio repositorio) {

        this.repositorio = repositorio;
    }

    // Verifica se a nota pode conceder gratuidade
    @Transactional(readOnly = true)
    public NotaFiscalSimulada validarNota(String codigoNota) {

        // Verifica se o codigo foi informado
        if (codigoNota == null || codigoNota.isBlank()) {
            throw new IllegalArgumentException(
                    "Informe o codigo da nota fiscal."
            );
        }

        // Busca a nota fiscal no banco H2
        NotaFiscalSimulada nota = repositorio
                .findByCodigoNota(codigoNota)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Nota fiscal nao encontrada."
                        )
                );

        // Verifica se a nota ja foi utilizada
        if (nota.isUtilizada()) {
            throw new IllegalStateException(
                    "Esta nota fiscal ja foi utilizada."
            );
        }

        // Verifica se a compra atingiu R$ 50,00
        if (nota.getValorCompra() == null ||
                nota.getValorCompra().compareTo(VALOR_MINIMO) < 0) {

            throw new IllegalArgumentException(
                    "Compra inferior a R$ 50,00."
            );
        }

        // Nota aprovada na verificacao
        return nota;
    }
}
