
/**
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
import java.util.Optional;

public interface RegistroEstacionamentoRepositorio
        extends JpaRepository<RegistroEstacionamento, Long> {

    // Verifica se a placa já possui um registro sem horário de saída
    boolean existsByPlacaAndHorarioSaidaIsNull(String placa);

    // Busca o registro ativo de um veículo pela placa
    Optional<RegistroEstacionamento> findByPlacaAndHorarioSaidaIsNull(String placa);

}
