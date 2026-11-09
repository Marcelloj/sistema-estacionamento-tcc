
package com.tcc.sistema_estacionamento.pagamento_saida;

import com.tcc.sistema_estacionamento.modelo.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ServicoSaidaGratuita {

    private final EntityManager entityManager;

    public ServicoSaidaGratuita(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public void finalizarSaidaGratuita(Long idRegistro) {

        // Bloqueia o registro durante esta operação.
        RegistroEstacionamento registro = entityManager.find(
                RegistroEstacionamento.class,
                idRegistro,
                LockModeType.PESSIMISTIC_WRITE
        );

        if (registro == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Registro não encontrado."
            );
        }

        if (registro.getStatusVeiculo() != StatusVeiculo.ESTACIONADO
                || registro.getStatusPagamento() != StatusPagamento.PENDENTE
                || registro.getHorarioSaida() != null) {

            throw new IllegalStateException(
                    "Este veículo não pode utilizar a saída gratuita."
            );
        }

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime entrada = registro.getHorarioEntrada();

        if (entrada == null || agora.isBefore(entrada)) {
            throw new IllegalStateException(
                    "Horário de entrada inválido."
            );
        }

        // Gratuidade termina exatamente 15 minutos após a entrada.
        if (!agora.isBefore(entrada.plusMinutes(15))) {
            throw new IllegalStateException(
                    "Os 15 minutos gratuitos terminaram."
            );
        }

        registro.setValorTarifaOriginal(BigDecimal.ZERO);
        registro.setValorTotal(BigDecimal.ZERO);
        registro.setValorPago(BigDecimal.ZERO);
        registro.setTroco(BigDecimal.ZERO);

        registro.setStatusPagamento(StatusPagamento.ISENTO);
        registro.setMetodoPagamento(MetodoPagamento.GRATUIDADE);
        registro.setHorarioPagamento(agora);

        registro.setCodigoTransacao(
                "GRATIS-" + UUID.randomUUID()
                        .toString().substring(0, 8).toUpperCase()
        );

        // Libera e finaliza na mesma operação.
        // NÃO inicia outra contagem de 15 minutos.
        registro.setHorarioLiberacaoCancela(agora);
        registro.setHorarioSaida(agora);
        registro.setStatusVeiculo(StatusVeiculo.FINALIZADO);

        entityManager.flush();
    }
}
