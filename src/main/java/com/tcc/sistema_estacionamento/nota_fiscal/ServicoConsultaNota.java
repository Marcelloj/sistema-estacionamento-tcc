
package com.tcc.sistema_estacionamento.nota_fiscal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class ServicoConsultaNota {

    private final NotaFiscalSimuladaRepositorio repositorio;

    // Construtor que recebe o repositório
    public ServicoConsultaNota(
            NotaFiscalSimuladaRepositorio repositorio) {

        this.repositorio = repositorio;
    }

    // Consulta uma nota fiscal pelo código único
    @Transactional(readOnly = true)
    public NotaFiscalSimulada consultarPorCodigo(String codigoNota) {

        // Verifica se o código foi informado
        if (codigoNota == null || codigoNota.isBlank()) {
            throw new IllegalArgumentException(
                "O código da nota não pode estar vazio."
            );
        }

        // Remove espaços extras do código
        String codigoNormalizado = codigoNota.trim();

        // Busca a nota no banco H2
        return repositorio
                .findByCodigoNota(codigoNormalizado)
                .orElseThrow(() -> new NoSuchElementException(
                    "Nota fiscal não encontrada: " + codigoNormalizado
                ));
    }
}
