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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class ServicoPagamentoSaida {

    private final RegistroEstacionamentoRepositorio repositorio;

    public ServicoPagamentoSaida(RegistroEstacionamentoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public boolean processarPagamento(Long idRegistro, double valorPago) {
        return processarPagamentoCompleto(idRegistro, valorPago, MetodoPagamento.PIX) != null;
    }

    public ComprovantePagamento processarPagamentoCompleto(Long idRegistro, double valorPago, MetodoPagamento metodo) {
        Optional<RegistroEstacionamento> registroOpt = repositorio.findById(idRegistro);
        if (registroOpt.isEmpty()) {
            throw new RegistroNaoEncontradoException("Registro com ID " + idRegistro + " não foi localizado.");
        }
        RegistroEstacionamento registro = registroOpt.get();

        if (registro.getValorTarifa() <= 0.0) {
            registro.setStatusPagamento(StatusPagamento.ISENTO);
            registro.setMetodoPagamento(MetodoPagamento.GRATUIDADE);
            registro.setValorPago(0.0);
            registro.setTroco(0.0);
            registro.setHorarioPagamento(LocalDateTime.now());
            registro.setCodigoTransacao("ISENTO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            repositorio.save(registro);
            return new ComprovantePagamento(
                registro.getCodigoTransacao(),
                registro.getId(),
                registro.getPlaca(),
                MetodoPagamento.GRATUIDADE,
                0.0,
                0.0,
                0.0,
                registro.getHorarioPagamento(),
                "ISENÇÃO AUTORIZADA: Carência ou benefício validado."
            );
        }

        if (valorPago < registro.getValorTarifa()) {
            throw new ValorInsuficienteException(
                String.format("Valor pago (R$ %.2f) é insuficiente para a tarifa de R$ %.2f",
                    valorPago, registro.getValorTarifa())
            );
        }

        double troco = Math.max(0.0, valorPago - registro.getValorTarifa());
        registro.setStatusPagamento(StatusPagamento.PAGO);
        registro.setValorPago(valorPago);
        registro.setTroco(troco);
        registro.setMetodoPagamento(metodo != null ? metodo : MetodoPagamento.CARTAO_CREDITO);
        registro.setHorarioPagamento(LocalDateTime.now());
        registro.setCodigoTransacao("AUT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        repositorio.save(registro);

        return new ComprovantePagamento(
            registro.getCodigoTransacao(),
            registro.getId(),
            registro.getPlaca(),
            registro.getMetodoPagamento(),
            registro.getValorTarifa(),
            registro.getValorPago(),
            registro.getTroco(),
            registro.getHorarioPagamento(),
            "PAGAMENTO FICTÍCIO APROVADO COM SUCESSO."
        );
    }

    public boolean tentarLiberarCancela(Long idRegistro) {
        Optional<RegistroEstacionamento> registroOpt = repositorio.findById(idRegistro);
        if (registroOpt.isEmpty()) {
            throw new RegistroNaoEncontradoException("Registro com ID " + idRegistro + " não foi localizado.");
        }
        RegistroEstacionamento registro = registroOpt.get();

        if (registro.getStatusVeiculo() == StatusVeiculo.FINALIZADO) {
            throw new IllegalStateException("O veículo com placa " + registro.getPlaca() + " já realizou a saída anteriormente.");
        }

        if (registro.getStatusPagamento() == StatusPagamento.PAGO ||
            registro.getStatusPagamento() == StatusPagamento.ISENTO) {
            registro.setStatusVeiculo(StatusVeiculo.LIBERADO);
            registro.setHorarioLiberacaoCancela(LocalDateTime.now());
            repositorio.save(registro);
            return true;
        } else {
            throw new IllegalStateException("Saída não autorizada: Pagamento pendente para o veículo " + registro.getPlaca());
        }
    }

    public void registrarSaida(Long idRegistro) {
        Optional<RegistroEstacionamento> registroOpt = repositorio.findById(idRegistro);
        if (registroOpt.isEmpty()) {
            throw new RegistroNaoEncontradoException("Registro com ID " + idRegistro + " não foi localizado.");
        }
        RegistroEstacionamento registro = registroOpt.get();

        if (registro.getStatusVeiculo() == StatusVeiculo.LIBERADO) {
            registro.setStatusVeiculo(StatusVeiculo.FINALIZADO);
            registro.setHorarioSaida(LocalDateTime.now());
            repositorio.save(registro);
        } else {
            throw new IllegalStateException("O veículo não está liberado para saída. Verifique a cancela.");
        }
    }
}