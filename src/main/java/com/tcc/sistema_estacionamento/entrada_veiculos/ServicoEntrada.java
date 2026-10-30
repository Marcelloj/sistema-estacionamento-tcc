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
import com.tcc.sistema_estacionamento.modelo.RegistroEstacionamento;
import java.time.LocalDateTime;


@Service
public class ServicoEntrada {

    private final RegistroEstacionamentoRepositorio repositorio;

    public ServicoEntrada(RegistroEstacionamentoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public RegistroEstacionamento registrarEntrada(String placa) {

        if (placa == null || placa.isBlank()) {
            throw new IllegalArgumentException("A placa não pode estar vazia.");
        }

        placa = placa.trim().toUpperCase(); // Padroniza a placa, removendo espaços e deixando as letras maiúsculas

        if (!placa.matches("[A-Z]{3}[0-9][A-Z0-9][0-9]{2}")){ // Verifica se a placa segue o formato brasileiro antigo ou Mercosul
            throw new IllegalArgumentException("Formato de placa inválido.");
        }

        if ( repositorio.existsByPlacaAndHorarioSaidaIsNull(placa)) {
            throw new IllegalArgumentException("Esse veículo já está no estacionamento.");
        }

        RegistroEstacionamento registro = new RegistroEstacionamento();
        registro.setPlaca(placa);
        registro.setHorarioEntrada(LocalDateTime.now());
        return repositorio.save(registro);      

    }

}
