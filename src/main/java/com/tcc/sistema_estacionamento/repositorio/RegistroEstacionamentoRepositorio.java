/*
 * RESPONSÁVEL: Marcello
 *
 * OBJETIVO:
 * Permitir o acesso aos registros de estacionamento
 * armazenados no banco de dados H2.
 *
 * UTILIZADO POR:
 * Entrada de veículos, cálculo e validação,
 * pagamento e saída.
 */


package com.tcc.sistema_estacionamento.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import com.tcc.sistema_estacionamento.modelo.RegistroEstacionamento;


public interface RegistroEstacionamentoRepositorio
        extends JpaRepository<RegistroEstacionamento, Long> {

}
