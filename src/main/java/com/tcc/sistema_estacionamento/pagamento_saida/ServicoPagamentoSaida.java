
package com.tcc.sistema_estacionamento.pagamento_saida;

import com.tcc.sistema_estacionamento.excecao.RegistroNaoEncontradoException;
import com.tcc.sistema_estacionamento.excecao.ValorInsuficienteException;
import com.tcc.sistema_estacionamento.modelo.ComprovantePagamento;
import com.tcc.sistema_estacionamento.modelo.MetodoPagamento;
import com.tcc.sistema_estacionamento.modelo.RegistroEstacionamento;
import com.tcc.sistema_estacionamento.modelo.StatusPagamento;
import com.tcc.sistema_estacionamento.modelo.StatusVeiculo;
import com.tcc.sistema_estacionamento.repositorio.RegistroEstacionamentoRepositorio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ServicoPagamentoSaida {

    private final RegistroEstacionamentoRepositorio repositorio;

    public ServicoPagamentoSaida(
            RegistroEstacionamentoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    // Método simplificado para simular pagamento via PIX.
    @Transactional
    public boolean processarPagamento(Long idRegistro, double valorPago) {

        ComprovantePagamento comprovante = processarPagamentoCompleto(
                idRegistro,
                valorPago,
                MetodoPagamento.PIX
        );

        return comprovante != null;
    }

    // Processa o pagamento e gera um comprovante fictício.
    @Transactional
    public ComprovantePagamento processarPagamentoCompleto(
            Long idRegistro,
            double valorPago,
            MetodoPagamento metodo) {

        RegistroEstacionamento registro = repositorio.findById(idRegistro)
                .orElseThrow(() -> new RegistroNaoEncontradoException(
                        "Registro com ID " + idRegistro + " não encontrado."
                ));

        // Não permite pagamento após liberação ou saída.
        if (registro.getStatusVeiculo() == StatusVeiculo.FINALIZADO) {
            throw new IllegalStateException(
                    "O veículo já saiu do estacionamento."
            );
        }

        if (registro.getStatusVeiculo() == StatusVeiculo.LIBERADO) {
            throw new IllegalStateException(
                    "A saída já foi liberada para este veículo."
            );
        }

        // Impede pagamento duplicado.
        if (registro.getStatusPagamento() == StatusPagamento.PAGO) {
            throw new IllegalStateException(
                    "Este estacionamento já foi pago."
            );
        }

        BigDecimal valorTotal = registro.getValorTotal();

        // A isenção deve ter sido autorizada pelo módulo de validação.
        if (registro.getStatusPagamento() == StatusPagamento.ISENTO) {

            if (valorTotal == null ||
                    valorTotal.compareTo(BigDecimal.ZERO) != 0) {

                throw new IllegalStateException(
                        "A isenção não está consistente com o valor total."
                );
            }

            // Não gera uma nova transação se já existe comprovante.
            if (registro.getCodigoTransacao() == null) {

                registro.setMetodoPagamento(MetodoPagamento.GRATUIDADE);
                registro.setValorPago(BigDecimal.ZERO);
                registro.setTroco(BigDecimal.ZERO);
                registro.setHorarioPagamento(LocalDateTime.now());

                registro.setCodigoTransacao(
                        "ISENTO-" +
                        UUID.randomUUID().toString()
                                .substring(0, 8).toUpperCase()
                );

                repositorio.save(registro);
            }

            return new ComprovantePagamento(
                    registro.getCodigoTransacao(),
                    registro.getId(),
                    registro.getPlaca(),
                    MetodoPagamento.GRATUIDADE,
                    0.0,
                    0.0,
                    0.0,
                    registro.getHorarioPagamento(),
                    "ISENÇÃO AUTORIZADA."
            );
        }

        // Outros estados não podem iniciar pagamento.
        if (registro.getStatusPagamento() != StatusPagamento.PENDENTE) {
            throw new IllegalStateException(
                    "O registro não está disponível para pagamento."
            );
        }

        // Não permite pagamento antes do cálculo da tarifa.
        if (valorTotal == null) {
            throw new IllegalStateException(
                    "A tarifa do estacionamento ainda não foi calculada."
            );
        }

        if (valorTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException(
                    "Valor total inválido. Verifique o cálculo ou a isenção."
            );
        }

        // Valida o valor informado para o pagamento fictício.
        if (!Double.isFinite(valorPago) || valorPago < 0) {
            throw new IllegalArgumentException(
                    "O valor informado para pagamento é inválido."
            );
        }

        BigDecimal pagamento;

        try {
            pagamento = BigDecimal.valueOf(valorPago)
                    .setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(
                    "Informe o pagamento com no máximo duas casas decimais."
            );
        }

        if (pagamento.compareTo(valorTotal) < 0) {
            throw new ValorInsuficienteException(
                    "Valor insuficiente. Total devido: R$ " + valorTotal
            );
        }

        if (metodo == MetodoPagamento.GRATUIDADE) {
            throw new IllegalArgumentException(
                    "Gratuidade não é um método válido para pagamento."
            );
        }

        // Calcula o troco usando BigDecimal.
        BigDecimal troco = pagamento.subtract(valorTotal);

        registro.setStatusPagamento(StatusPagamento.PAGO);
        registro.setValorPago(pagamento);
        registro.setTroco(troco);

        registro.setMetodoPagamento(
                metodo != null ? metodo : MetodoPagamento.PIX
        );

        registro.setHorarioPagamento(LocalDateTime.now());

        registro.setCodigoTransacao(
                "AUT-" +
                UUID.randomUUID().toString()
                        .substring(0, 8).toUpperCase()
        );

        repositorio.save(registro);

        // O comprovante ainda utiliza double na versão do Vinicius.
        // Depois vamos corrigir essa classe também.
        return new ComprovantePagamento(
                registro.getCodigoTransacao(),
                registro.getId(),
                registro.getPlaca(),
                registro.getMetodoPagamento(),
                valorTotal.doubleValue(),
                pagamento.doubleValue(),
                troco.doubleValue(),
                registro.getHorarioPagamento(),
                "PAGAMENTO FICTÍCIO APROVADO."
        );
    }

    // Autoriza ou bloqueia a cancela virtual.
    @Transactional
    public boolean tentarLiberarCancela(Long idRegistro) {

        RegistroEstacionamento registro = repositorio.findById(idRegistro)
                .orElseThrow(() -> new RegistroNaoEncontradoException(
                        "Registro com ID " + idRegistro + " não encontrado."
                ));

        if (registro.getStatusVeiculo() == StatusVeiculo.FINALIZADO) {
            throw new IllegalStateException(
                    "Este veículo já realizou a saída."
            );
        }

        BigDecimal valorTotal = registro.getValorTotal();

        boolean pagamentoValido =
                registro.getStatusPagamento() == StatusPagamento.PAGO
                && valorTotal != null
                && registro.getValorPago() != null
                && registro.getValorPago().compareTo(valorTotal) >= 0;

        boolean isencaoValida =
                registro.getStatusPagamento() == StatusPagamento.ISENTO
                && valorTotal != null
                && valorTotal.compareTo(BigDecimal.ZERO) == 0;

        if (!pagamentoValido && !isencaoValida) {
            throw new IllegalStateException(
                    "Saída não autorizada: pagamento ou isenção pendente."
            );
        }

        // Evita alterar repetidamente o horário de liberação.
        if (registro.getStatusVeiculo() == StatusVeiculo.LIBERADO) {
            return true;
        }

        if (registro.getStatusVeiculo() != StatusVeiculo.ESTACIONADO) {
            throw new IllegalStateException(
                    "Estado do veículo inválido para liberar a cancela."
            );
        }

        registro.setStatusVeiculo(StatusVeiculo.LIBERADO);
        registro.setHorarioLiberacaoCancela(LocalDateTime.now());

        repositorio.save(registro);

        return true;
    }

    // Registra a saída e encerra a permanência do veículo.
    @Transactional
    public void registrarSaida(Long idRegistro) {

        RegistroEstacionamento registro = repositorio.findById(idRegistro)
                .orElseThrow(() -> new RegistroNaoEncontradoException(
                        "Registro com ID " + idRegistro + " não encontrado."
                ));

        if (registro.getStatusVeiculo() != StatusVeiculo.LIBERADO) {
            throw new IllegalStateException(
                    "A cancela ainda não foi liberada para este veículo."
            );
        }

        registro.setStatusVeiculo(StatusVeiculo.FINALIZADO);
        registro.setHorarioSaida(LocalDateTime.now());

        repositorio.save(registro);
    }
}
