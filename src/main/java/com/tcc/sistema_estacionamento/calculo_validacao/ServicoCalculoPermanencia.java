
package com.tcc.sistema_estacionamento.calculo_validacao;

import com.tcc.sistema_estacionamento.modelo.RegistroEstacionamento;
import com.tcc.sistema_estacionamento.repositorio.RegistroEstacionamentoRepositorio;
import com.tcc.sistema_estacionamento.excecao.RegistroNaoEncontradoException;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class ServicoCalculoPermanencia {

    // Tempo gratuito de estacionamento
    private static final Duration TEMPO_GRATUITO =
            Duration.ofMinutes(15);

    // Valor por hora iniciada após a carência
    private static final BigDecimal VALOR_HORA =
            new BigDecimal("10.00");

    private final RegistroEstacionamentoRepositorio repositorio;

    // Construtor
    public ServicoCalculoPermanencia(
            RegistroEstacionamentoRepositorio repositorio) {

        this.repositorio = repositorio;
    }

    // Consulta o registro e calcula a tarifa atual
    public BigDecimal calcularTarifa(Long idRegistro) {

        if (idRegistro == null) {
            throw new IllegalArgumentException(
                    "Informe o ID do estacionamento."
            );
        }

        RegistroEstacionamento registro = repositorio
                .findById(idRegistro)
                .orElseThrow(() ->
                        new RegistroNaoEncontradoException(
                                "Estacionamento não encontrado."
                        )
                );

        // Impede calcular um estacionamento encerrado
        if (registro.getHorarioSaida() != null) {
            throw new IllegalStateException(
                    "Este estacionamento já foi finalizado."
            );
        }

        LocalDateTime horarioEntrada =
                registro.getHorarioEntrada();

        LocalDateTime horarioAtual =
                LocalDateTime.now();

        return calcularTarifa(horarioEntrada, horarioAtual);
    }

    // Calcula o valor entre dois horários
    public BigDecimal calcularTarifa(
            LocalDateTime horarioEntrada,
            LocalDateTime horarioAtual) {

        if (horarioEntrada == null || horarioAtual == null) {
            throw new IllegalArgumentException(
                    "Os horários precisam ser informados."
            );
        }

        if (horarioAtual.isBefore(horarioEntrada)) {
            throw new IllegalArgumentException(
                    "O horário atual não pode ser anterior à entrada."
            );
        }

        // Calcula o tempo total estacionado
        Duration tempoPermanencia =
                Duration.between(horarioEntrada, horarioAtual);

        // Até 15 minutos não paga
        if (tempoPermanencia.compareTo(TEMPO_GRATUITO) <= 0) {
            return new BigDecimal("0.00");
        }

        // Desconta os 15 minutos gratuitos
        Duration tempoCobrado =
                tempoPermanencia.minus(TEMPO_GRATUITO);

        // Quantidade de horas completas
        long horasCompletas = tempoCobrado.toHours();

        // Verifica se sobrou alguma fração
        boolean temFracao =
                !tempoCobrado.minusHours(horasCompletas).isZero();

        long horasCobradas = horasCompletas;

        // Uma fração também conta como hora iniciada
        if (temFracao) {
            horasCobradas++;
        }

        // Calcula o valor final
        return VALOR_HORA.multiply(
                BigDecimal.valueOf(horasCobradas)
        );
    }
}
