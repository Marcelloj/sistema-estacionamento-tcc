/*
 * RESPONSÁVEL: Marcello
 *
 * FUNCIONALIDADE: Entrada de veículos
 *
 * TAREFAS:
 * 1. Registrar a entrada de um veículo.
 * 2. Salvar a placa e o horário de entrada.
 * 3. Impedir entradas duplicadas.
 */


package com.tcc.sistema_estacionamento.entrada_veiculos;

import org.springframework.stereotype.Service;
import com.tcc.sistema_estacionamento.repositorio.RegistroEstacionamentoRepositorio;


@Service
public class ServicoEntrada {

    private final RegistroEstacionamentoRepositorio repositorio;

    public ServicoEntrada(RegistroEstacionamentoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

}
