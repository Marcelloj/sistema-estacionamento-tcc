
package com.tcc.sistema_estacionamento.pagamento_saida;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.tcc.sistema_estacionamento.modelo.RegistroEstacionamento;
import com.tcc.sistema_estacionamento.modelo.StatusVeiculo;

/**
 * Responsavel pelo prazo de 30 minutos
 * apos a liberacao da cancela virtual.
 */
@Service
public class ServicoPrazoSaida {

    private static final long PRAZO_MINUTOS = 15;

    /**
     * Calcula ate quando o cliente pode sair.
     */
    public LocalDateTime calcularLimite(
            RegistroEstacionamento registro) {

        if (registro.getHorarioLiberacaoCancela() == null) {
            throw new IllegalStateException(
                    "O estacionamento ainda nao foi liberado."
            );
        }

        return registro.getHorarioLiberacaoCancela()
                .plusMinutes(PRAZO_MINUTOS);
    }

    /**
     * Calcula os segundos restantes para a saida.
     * Nunca devolve valores negativos.
     */
    public long calcularSegundosRestantes(
            RegistroEstacionamento registro) {

        LocalDateTime limite = calcularLimite(registro);

        long segundos = Duration.between(
                LocalDateTime.now(),
                limite
        ).getSeconds();

        return Math.max(0, segundos);
    }

    /**
     * Verifica se os 30 minutos ja terminaram.
     */
    public boolean prazoExpirado(
            RegistroEstacionamento registro) {

        LocalDateTime limite = calcularLimite(registro);

        return !LocalDateTime.now().isBefore(limite);
    }

    /**
     * Regra que sera utilizada antes de finalizar
     * a saida do veiculo.
     */
    public void validarPrazoSaida(
            RegistroEstacionamento registro) {

        if (registro.getStatusVeiculo()
                != StatusVeiculo.LIBERADO) {

            throw new IllegalStateException(
                    "A cancela ainda nao foi liberada."
            );
        }

        if (prazoExpirado(registro)) {

            throw new IllegalStateException(
                    "O prazo de 15 minutos expirou. É necessária uma nova autorização."
            );
        }
    }
}
