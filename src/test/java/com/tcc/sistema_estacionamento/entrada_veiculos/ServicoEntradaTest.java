
package com.tcc.sistema_estacionamento.entrada_veiculos;

import com.tcc.sistema_estacionamento.modelo.RegistroEstacionamento;
import com.tcc.sistema_estacionamento.repositorio.RegistroEstacionamentoRepositorio;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ServicoEntradaTest {

    @Autowired
    private ServicoEntrada servicoEntrada;

    @Autowired
    private RegistroEstacionamentoRepositorio repositorio;

    // CT01 - Verifica o cadastro de uma placa válida
    @Test
    void deveRegistrarEntradaValida() {

        RegistroEstacionamento registro =
                servicoEntrada.registrarEntrada("ABC1D23");

        assertEquals("ABC1D23", registro.getPlaca());
        assertNotNull(registro.getId());
        assertNotNull(registro.getHorarioEntrada());
        assertNull(registro.getHorarioSaida());

        // Verifica se o registro existe no banco
        assertTrue(repositorio.findById(registro.getId()).isPresent());
    }

    // CT02 - Impede entrada duplicada de veículo estacionado
    @Test
    void deveImpedirEntradaDuplicada() {

        servicoEntrada.registrarEntrada("TST1A23");

        assertThrows(IllegalArgumentException.class, () -> {
            servicoEntrada.registrarEntrada("TST1A23");
        });
    }

    // CT03 - Rejeita placa com formato inválido
    @Test
    void deveRejeitarPlacaInvalida() {

        assertThrows(IllegalArgumentException.class, () -> {
            servicoEntrada.registrarEntrada("ABC12");
        });
    }

    // CT04 - Impede cadastro de placa vazia
    @Test
    void deveRejeitarPlacaVazia() {

        assertThrows(IllegalArgumentException.class, () -> {
            servicoEntrada.registrarEntrada("");
        });
    }

    // CT05 - Impede cadastro de placa nula
    @Test
    void deveRejeitarPlacaNula() {

        assertThrows(IllegalArgumentException.class, () -> {
            servicoEntrada.registrarEntrada(null);
        });
    }

    // CT06 - Padroniza a placa antes de salvar
    @Test
    void devePadronizarPlaca() {

        RegistroEstacionamento registro =
                servicoEntrada.registrarEntrada("  abc1d23  ");

        assertEquals("ABC1D23", registro.getPlaca());
    }

    // CT07 - Aceita placa brasileira no formato antigo
    @Test
    void deveAceitarPlacaFormatoAntigo() {

        RegistroEstacionamento registro =
                servicoEntrada.registrarEntrada("ABC1234");

        assertEquals("ABC1234", registro.getPlaca());
        assertNotNull(registro.getId());
    }
}
