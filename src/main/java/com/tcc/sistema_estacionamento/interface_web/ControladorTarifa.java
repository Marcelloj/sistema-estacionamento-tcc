
package com.tcc.sistema_estacionamento.interface_web;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.tcc.sistema_estacionamento.modelo.RegistroEstacionamento;
import com.tcc.sistema_estacionamento.repositorio.RegistroEstacionamentoRepositorio;
import com.tcc.sistema_estacionamento.calculo_validacao.ServicoCalculoPermanencia;

/**
 * Consulta a tarifa dos veículos estacionados.
 *
 * Este controlador não realiza pagamentos e não altera o banco.
 * Apenas consulta o registro e utiliza o cálculo oficial do sistema.
 */
@RestController
@RequestMapping("/tarifas")
public class ControladorTarifa {

    private final RegistroEstacionamentoRepositorio repositorio;
    private final ServicoCalculoPermanencia servicoCalculo;

    public ControladorTarifa(
            RegistroEstacionamentoRepositorio repositorio,
            ServicoCalculoPermanencia servicoCalculo) {

        this.repositorio = repositorio;
        this.servicoCalculo = servicoCalculo;
    }

    @GetMapping("/{idRegistro}")
    public RespostaTarifa consultarTarifa(
            @PathVariable Long idRegistro) {

        RegistroEstacionamento registro = repositorio.findById(idRegistro)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Registro de estacionamento não encontrado."
                ));

        String statusPagamento =
                String.valueOf(registro.getStatusPagamento());

        BigDecimal tarifaCalculada;

        if ("PENDENTE".equals(statusPagamento)) {

            // Enquanto está pendente, a tarifa acompanha
            // o tempo de permanência do veículo.
            tarifaCalculada =
                    servicoCalculo.calcularTarifa(idRegistro);

        } else {

            // Após pagamento ou isenção, preservamos
            // o valor registrado naquela operação.
            tarifaCalculada = registro.getValorTarifaOriginal();

            if (tarifaCalculada == null) {
                tarifaCalculada = registro.getValorTotal();
            }

            if (tarifaCalculada == null) {
                tarifaCalculada =
                        servicoCalculo.calcularTarifa(idRegistro);
            }
        }

        BigDecimal valorPendente =
                "PENDENTE".equals(statusPagamento)
                        ? tarifaCalculada
                        : BigDecimal.ZERO;

        return new RespostaTarifa(
                registro.getId(),
                registro.getPlaca(),
                tarifaCalculada,
                valorPendente,
                statusPagamento
        );
    }

    public record RespostaTarifa(
            Long idRegistro,
            String placa,
            BigDecimal tarifaCalculada,
            BigDecimal valorPendente,
            String statusPagamento
    ) {
    }
}
