
package com.tcc.sistema_estacionamento.pagamento_saida;

import com.tcc.sistema_estacionamento.excecao.RegistroNaoEncontradoException;
import com.tcc.sistema_estacionamento.excecao.ValorInsuficienteException;
import com.tcc.sistema_estacionamento.modelo.ComprovantePagamento;
import com.tcc.sistema_estacionamento.modelo.MetodoPagamento;
import com.tcc.sistema_estacionamento.modelo.RegistroEstacionamento;
import com.tcc.sistema_estacionamento.modelo.StatusPagamento;
import com.tcc.sistema_estacionamento.modelo.StatusVeiculo;
import com.tcc.sistema_estacionamento.repositorio.RegistroEstacionamentoRepositorio;
import com.tcc.sistema_estacionamento.calculo_validacao.ServicoCalculoPermanencia;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ServicoPagamentoSaida {

    private final RegistroEstacionamentoRepositorio repositorio;
    private final ServicoCalculoPermanencia servicoCalculo;

    // Serviço sem dependências externas, responsável pelo prazo.
    // Mantemos o construtor original para compatibilidade com os testes.
    private final ServicoPrazoSaida servicoPrazoSaida =
            new ServicoPrazoSaida();

    public ServicoPagamentoSaida(
            RegistroEstacionamentoRepositorio repositorio,
            ServicoCalculoPermanencia servicoCalculo) {

        this.repositorio = repositorio;
        this.servicoCalculo = servicoCalculo;
    }

    /**
     * Simula um pagamento utilizando PIX.
     */
    @Transactional
    public boolean processarPagamento(
            Long idRegistro,
            double valorPago) {

        ComprovantePagamento comprovante =
                processarPagamentoCompleto(
                        idRegistro,
                        valorPago,
                        MetodoPagamento.PIX
                );

        return comprovante != null;
    }

    /**
     * Processa o pagamento e gera um comprovante fictício.
     *
     * Também reconhece a gratuidade dos primeiros
     * 15 minutos, conforme a tarifa calculada.
     */
    @Transactional
    public ComprovantePagamento processarPagamentoCompleto(
            Long idRegistro,
            double valorPago,
            MetodoPagamento metodo) {

        RegistroEstacionamento registro =
                repositorio.findById(idRegistro)
                        .orElseThrow(() ->
                                new RegistroNaoEncontradoException(
                                        "Registro com ID " +
                                        idRegistro +
                                        " não encontrado."
                                )
                        );

        // Não permite pagamento após a saída.
        if (registro.getStatusVeiculo()
                == StatusVeiculo.FINALIZADO) {

            throw new IllegalStateException(
                    "O veículo já saiu do estacionamento."
            );
        }

        // Não permite novo pagamento após a liberação.
        if (registro.getStatusVeiculo()
                == StatusVeiculo.LIBERADO) {

            throw new IllegalStateException(
                    "A saída já foi liberada para este veículo."
            );
        }

        // Impede pagamento duplicado.
        if (registro.getStatusPagamento()
                == StatusPagamento.PAGO) {

            throw new IllegalStateException(
                    "Este estacionamento já foi pago."
            );
        }

        // Calcula a tarifa quando o pagamento está pendente.
        if (registro.getStatusPagamento()
                == StatusPagamento.PENDENTE) {

            BigDecimal tarifaCalculada =
                    servicoCalculo.calcularTarifa(idRegistro);

            registro.setValorTarifaOriginal(tarifaCalculada);
            registro.setValorTotal(tarifaCalculada);

            // Até 15 minutos: tarifa zero.
            if (tarifaCalculada.compareTo(BigDecimal.ZERO) == 0) {

                registro.setStatusPagamento(
                        StatusPagamento.ISENTO
                );
            }
        }

        BigDecimal valorTotal = registro.getValorTotal();

        // GRATUIDADE / ISENÇÃO
        if (registro.getStatusPagamento()
                == StatusPagamento.ISENTO) {

            if (valorTotal == null ||
                    valorTotal.compareTo(BigDecimal.ZERO) != 0) {

                throw new IllegalStateException(
                        "A isenção não está consistente " +
                        "com o valor total."
                );
            }

            // Evita gerar transações repetidas.
            if (registro.getCodigoTransacao() == null) {

                registro.setMetodoPagamento(
                        MetodoPagamento.GRATUIDADE
                );

                registro.setValorPago(BigDecimal.ZERO);
                registro.setTroco(BigDecimal.ZERO);

                registro.setHorarioPagamento(
                        LocalDateTime.now()
                );

                registro.setCodigoTransacao(
                        "ISENTO-" +
                        UUID.randomUUID().toString()
                                .substring(0, 8)
                                .toUpperCase()
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
        if (registro.getStatusPagamento()
                != StatusPagamento.PENDENTE) {

            throw new IllegalStateException(
                    "O registro não está disponível para pagamento."
            );
        }

        if (valorTotal == null) {
            throw new IllegalStateException(
                    "A tarifa ainda não foi calculada."
            );
        }

        if (valorTotal.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalStateException(
                    "Valor total inválido."
            );
        }

        if (!Double.isFinite(valorPago) || valorPago < 0) {

            throw new IllegalArgumentException(
                    "O valor informado para pagamento é inválido."
            );
        }

        BigDecimal pagamento;

        try {

            pagamento = BigDecimal.valueOf(valorPago)
                    .setScale(2, RoundingMode.UNNECESSARY);

        } catch (ArithmeticException erro) {

            throw new IllegalArgumentException(
                    "Informe o pagamento com no máximo duas casas decimais."
            );
        }

        if (pagamento.compareTo(valorTotal) < 0) {

            throw new ValorInsuficienteException(
                    "Valor insuficiente. Total devido: R$ " +
                    valorTotal
            );
        }

        if (metodo == MetodoPagamento.GRATUIDADE) {

            throw new IllegalArgumentException(
                    "Gratuidade não é um método válido para pagamento."
            );
        }

        // PROCESSAMENTO DO PAGAMENTO
        BigDecimal troco = pagamento.subtract(valorTotal);

        registro.setStatusPagamento(StatusPagamento.PAGO);
        registro.setValorPago(pagamento);
        registro.setTroco(troco);

        registro.setMetodoPagamento(
                metodo != null
                        ? metodo
                        : MetodoPagamento.PIX
        );

        registro.setHorarioPagamento(LocalDateTime.now());

        registro.setCodigoTransacao(
                "AUT-" +
                UUID.randomUUID().toString()
                        .substring(0, 8)
                        .toUpperCase()
        );

        repositorio.save(registro);

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

    /**
     * Libera a cancela virtual.
     *
     * Apenas veículos pagos ou isentos podem ser liberados.
     * A primeira liberação inicia o prazo de 30 minutos.
     */
    @Transactional
    public boolean tentarLiberarCancela(Long idRegistro) {

        RegistroEstacionamento registro =
                repositorio.findById(idRegistro)
                        .orElseThrow(() ->
                                new RegistroNaoEncontradoException(
                                        "Registro com ID " +
                                        idRegistro +
                                        " não encontrado."
                                )
                        );

        if (registro.getStatusVeiculo()
                == StatusVeiculo.FINALIZADO) {

            throw new IllegalStateException(
                    "Este veículo já realizou a saída."
            );
        }

        BigDecimal valorTotal = registro.getValorTotal();

        boolean pagamentoValido =
                registro.getStatusPagamento()
                        == StatusPagamento.PAGO
                && valorTotal != null
                && registro.getValorPago() != null
                && registro.getValorPago()
                        .compareTo(valorTotal) >= 0;

        boolean isencaoValida =
                registro.getStatusPagamento()
                        == StatusPagamento.ISENTO
                && valorTotal != null
                && valorTotal.compareTo(BigDecimal.ZERO) == 0;

        if (!pagamentoValido && !isencaoValida) {

            throw new IllegalStateException(
                    "Saída não autorizada: pagamento " +
                    "ou isenção pendente."
            );
        }

        // Se já foi liberado, NÃO reinicia os 30 minutos.
        if (registro.getStatusVeiculo()
                == StatusVeiculo.LIBERADO) {

            // Também impede reutilizar uma autorização vencida.
            servicoPrazoSaida.validarPrazoSaida(registro);

            return true;
        }

        if (registro.getStatusVeiculo()
                != StatusVeiculo.ESTACIONADO) {

            throw new IllegalStateException(
                    "Estado do veículo inválido para liberar a cancela."
            );
        }

        // Inicia a validade da liberação.
        registro.setStatusVeiculo(StatusVeiculo.LIBERADO);

        registro.setHorarioLiberacaoCancela(
                LocalDateTime.now()
        );

        repositorio.save(registro);

        return true;
    }

    /**
     * Finaliza a saída do veículo.
     *
     * O veículo só pode sair se:
     * 1. A cancela estiver liberada;
     * 2. Os 30 minutos ainda não tiverem terminado.
     */
    @Transactional
    public void registrarSaida(Long idRegistro) {

        RegistroEstacionamento registro =
                repositorio.findById(idRegistro)
                        .orElseThrow(() ->
                                new RegistroNaoEncontradoException(
                                        "Registro com ID " +
                                        idRegistro +
                                        " não encontrado."
                                )
                        );

        // A regra verifica o status e o prazo de validade.
        servicoPrazoSaida.validarPrazoSaida(registro);

        // Saída autorizada dentro do prazo.
        registro.setStatusVeiculo(StatusVeiculo.FINALIZADO);

        registro.setHorarioSaida(LocalDateTime.now());

        repositorio.save(registro);
    }
}
