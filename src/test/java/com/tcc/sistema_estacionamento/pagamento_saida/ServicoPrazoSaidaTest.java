
package com.tcc.sistema_estacionamento.pagamento_saida;

import com.tcc.sistema_estacionamento.modelo.RegistroEstacionamento;
import com.tcc.sistema_estacionamento.modelo.StatusVeiculo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ServicoPrazoSaidaTest {

    private ServicoPrazoSaida servico;
    private RegistroEstacionamento registro;

    @BeforeEach
    void prepararTeste() {

        servico = new ServicoPrazoSaida();

        registro = new RegistroEstacionamento();
        registro.setPlaca("ABC1D23");

        registro.setStatusVeiculo(
                StatusVeiculo.LIBERADO
        );
    }

    @Test
    void devePermitirSaidaDentroDos30Minutos() {

        // Cancela liberada ha 5 minutos
        registro.setHorarioLiberacaoCancela(
                LocalDateTime.now().minusMinutes(5)
        );

        assertDoesNotThrow(() ->
                servico.validarPrazoSaida(registro)
        );
    }

    @Test
    void deveBloquearSaidaApos30Minutos() {

        // Cancela liberada ha 31 minutos
        registro.setHorarioLiberacaoCancela(
                LocalDateTime.now().minusMinutes(31)
        );

        IllegalStateException erro = assertThrows(
                IllegalStateException.class,
                () -> servico.validarPrazoSaida(registro)
        );

        assertTrue(
                erro.getMessage().contains("15 minutos")
        );
    }

    @Test
    void naoDevePermitirSaidaSemLiberacao() {

        // Veiculo ainda estacionado
        registro.setStatusVeiculo(
                StatusVeiculo.ESTACIONADO
        );

        assertThrows(
                IllegalStateException.class,
                () -> servico.validarPrazoSaida(registro)
        );
    }
}
