package com.tcc.sistema_estacionamento.interface_web;

import java.util.Comparator;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tcc.sistema_estacionamento.modelo.RegistroEstacionamento;
import com.tcc.sistema_estacionamento.repositorio.RegistroEstacionamentoRepositorio;

/*
 * RESPONSÁVEL: Felipe (interface web)
 *
 * OBJETIVO:
 * Fornecer ao painel a lista de todos os veículos que ainda estão
 * no estacionamento. Apenas leitura: nenhuma regra de negócio aqui.
 */
@RestController
@RequestMapping("/veiculos")
public class ControladorVeiculos {

    private final RegistroEstacionamentoRepositorio repositorio;

    // O Spring entrega o repositório pronto (injeção de dependência)
    public ControladorVeiculos(RegistroEstacionamentoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    // Lista os registros sem horário de saída, do mais recente ao mais antigo
    @GetMapping("/ativos")
    public List<RegistroEstacionamento> listarAtivos() {
        return repositorio.findAll().stream()
                .filter(registro -> registro.getHorarioSaida() == null)
                .sorted(Comparator.comparing(RegistroEstacionamento::getHorarioEntrada).reversed())
                .toList();
    }
}