
package com.tcc.sistema_estacionamento.interface_web;

import com.tcc.sistema_estacionamento.calculo_validacao.ServicoCalculoPermanencia;
import com.tcc.sistema_estacionamento.modelo.RegistroEstacionamento;
import com.tcc.sistema_estacionamento.modelo.StatusPagamento;
import com.tcc.sistema_estacionamento.modelo.StatusVeiculo;
import com.tcc.sistema_estacionamento.repositorio.RegistroEstacionamentoRepositorio;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ControladorPaginaMobile {

    private final RegistroEstacionamentoRepositorio repositorio;
    private final ServicoCalculoPermanencia calculo;

    public ControladorPaginaMobile(
            RegistroEstacionamentoRepositorio repositorio,
            ServicoCalculoPermanencia calculo) {

        this.repositorio = repositorio;
        this.calculo = calculo;
    }

    // Abre a nova página responsiva.
    @GetMapping("/estacionamento/mobile")
    public String abrirPaginaMobile() {
        return "estacionamento-mobile";
    }

    // Consulta um estacionamento ativo usando a placa.
    @GetMapping("/api/mobile/consultar")
    @ResponseBody
    public Map<String, Object> consultarPlaca(
            @RequestParam String placa) {

        String placaFormatada = placa
                .trim()
                .toUpperCase();

        RegistroEstacionamento registro = repositorio
                .findByPlacaAndHorarioSaidaIsNull(placaFormatada)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Não existe estacionamento ativo para esta placa."
                        )
                );

        BigDecimal tarifa;

        if (registro.getStatusPagamento() == StatusPagamento.PENDENTE) {
            tarifa = calculo.calcularTarifa(registro.getId());
        } else {
            tarifa = registro.getValorTotal() != null
                    ? registro.getValorTotal()
                    : BigDecimal.ZERO;
        }

        boolean gratuito = false;

        if (registro.getStatusVeiculo() == StatusVeiculo.ESTACIONADO
                && registro.getStatusPagamento() == StatusPagamento.PENDENTE
                && registro.getHorarioEntrada() != null) {

            LocalDateTime agora = LocalDateTime.now();

            gratuito = !agora.isBefore(registro.getHorarioEntrada())
                    && agora.isBefore(
                            registro.getHorarioEntrada().plusMinutes(15)
                    );
        }

        Map<String, Object> resposta = new HashMap<>();

        resposta.put("idRegistro", registro.getId());
        resposta.put("placa", registro.getPlaca());
        resposta.put("statusVeiculo", registro.getStatusVeiculo().name());
        resposta.put("statusPagamento", registro.getStatusPagamento().name());
        resposta.put("tarifa", tarifa);
        resposta.put("gratuidadeDisponivel", gratuito);
        resposta.put("horarioEntrada", registro.getHorarioEntrada());
        resposta.put(
                "horarioLiberacaoCancela",
                registro.getHorarioLiberacaoCancela()
        );
        resposta.put("horarioServidor", LocalDateTime.now());

        return resposta;
    }
}
