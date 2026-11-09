
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
public class ServicoRenovacaoSaida {

    private static final BigDecimal TAXA = new BigDecimal("10.00");

    private final EntityManager entityManager;
    private final ServicoPrazoSaida servicoPrazoSaida;

    public ServicoRenovacaoSaida(
            EntityManager entityManager,
            ServicoPrazoSaida servicoPrazoSaida) {

        this.entityManager = entityManager;
        this.servicoPrazoSaida = servicoPrazoSaida;
    }

    @Transactional
    public RenovacaoSaida renovar(Long idRegistro) {

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

        if (registro.getStatusVeiculo() != StatusVeiculo.LIBERADO
                || registro.getHorarioSaida() != null) {

            throw new IllegalStateException(
                    "Somente veículos liberados e ainda no pátio "
                    + "podem renovar a saída."
            );
        }

        if (!servicoPrazoSaida.prazoExpirado(registro)) {
            throw new IllegalStateException(
                    "A autorização ainda está válida."
            );
        }

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime validade = agora.plusMinutes(15);

        RenovacaoSaida renovacao = new RenovacaoSaida(
                registro.getId(),
                TAXA,
                agora,
                registro.getHorarioLiberacaoCancela(),
                validade,
                "REN-" + UUID.randomUUID().toString()
                        .substring(0, 8).toUpperCase()
        );

        // Registra o pagamento PIX fictício de R$ 10.
        entityManager.persist(renovacao);

        // Reinicia SOMENTE o prazo de saída.
        // O pagamento ou a isenção original permanece intacto.
        registro.setHorarioLiberacaoCancela(agora);

        entityManager.flush();

        return renovacao;
    }
}
