
package com.tcc.sistema_estacionamento.consulta_veiculos;

import com.tcc.sistema_estacionamento.modelo.RegistroEstacionamento;
import com.tcc.sistema_estacionamento.repositorio.RegistroEstacionamentoRepositorio;
import com.tcc.sistema_estacionamento.excecao.RegistroNaoEncontradoException;

import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class ServicoConsulta {

    private final RegistroEstacionamentoRepositorio repositorio;

    // Construtor que recebe o repositório
    public ServicoConsulta(RegistroEstacionamentoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    // Consulta o estacionamento ativo utilizando a placa
    public RegistroEstacionamento consultarPorPlaca(String placa) {

        if (placa == null || placa.isBlank()) {
            throw new IllegalArgumentException(
                "A placa não pode estar vazia."
            );
        }

        // Padroniza a placa antes de consultar
        String placaNormalizada = placa.trim().toUpperCase(Locale.ROOT);

        // Busca o veículo que ainda não registrou saída
        return repositorio
            .findByPlacaAndHorarioSaidaIsNull(placaNormalizada)
            .orElseThrow(() -> new RegistroNaoEncontradoException(
                "Nenhum estacionamento ativo encontrado para a placa "
                + placaNormalizada
            ));
    }
}
