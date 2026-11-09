
package com.tcc.sistema_estacionamento.interface_web;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.tcc.sistema_estacionamento.excecao.ValorInsuficienteException;
import com.tcc.sistema_estacionamento.modelo.*;
import com.tcc.sistema_estacionamento.pagamento_saida.*;
import com.tcc.sistema_estacionamento.repositorio.RegistroEstacionamentoRepositorio;

@RestController
@RequestMapping("/operacoes")
public class ControladorOperacoesPatio {

    private final RegistroEstacionamentoRepositorio repositorio;
    private final ServicoPagamentoSaida pagamentoSaida;
    private final ServicoSaidaGratuita saidaGratuita;
    private final ServicoRenovacaoSaida renovacaoSaida;

    public ControladorOperacoesPatio(
            RegistroEstacionamentoRepositorio repositorio,
            ServicoPagamentoSaida pagamentoSaida,
            ServicoSaidaGratuita saidaGratuita,
            ServicoRenovacaoSaida renovacaoSaida) {

        this.repositorio = repositorio;
        this.pagamentoSaida = pagamentoSaida;
        this.saidaGratuita = saidaGratuita;
        this.renovacaoSaida = renovacaoSaida;
    }

    // Gratuidade: encerra diretamente, sem tempo adicional.
    @PostMapping("/{idRegistro}/gratuidade")
    public Map<String, Object> sairGratuitamente(
            @PathVariable Long idRegistro) {

        saidaGratuita.finalizarSaidaGratuita(idRegistro);

        return Map.of(
                "idRegistro", idRegistro,
                "statusVeiculo", "FINALIZADO",
                "statusPagamento", "ISENTO",
                "mensagem", "Saída gratuita finalizada com sucesso."
        );
    }

    // Pagamento simulado da tarifa do estacionamento.
    @PostMapping("/{idRegistro}/pagamento")
    @Transactional
    public Map<String, Object> simularPagamento(
            @PathVariable Long idRegistro,
            @RequestParam double valorPago) {

        if (!Double.isFinite(valorPago) || valorPago <= 0) {
            throw new IllegalArgumentException(
                    "Informe um valor de pagamento válido."
            );
        }

        pagamentoSaida.processarPagamentoCompleto(
                idRegistro,
                valorPago,
                MetodoPagamento.PIX
        );

        pagamentoSaida.tentarLiberarCancela(idRegistro);

        RegistroEstacionamento registro = buscar(idRegistro);

        return Map.of(
                "idRegistro", registro.getId(),
                "placa", registro.getPlaca(),
                "statusVeiculo", registro.getStatusVeiculo().name(),
                "statusPagamento", registro.getStatusPagamento().name(),
                "mensagem", "Pagamento aprovado. Saída liberada por 15 minutos."
        );
    }

    // Cobrança adicional simulada após expirar a autorização.
    @PostMapping("/{idRegistro}/renovar")
    public Map<String, Object> renovarSaida(
            @PathVariable Long idRegistro) {

        RenovacaoSaida renovacao = renovacaoSaida.renovar(idRegistro);

        return Map.of(
                "idRegistro", idRegistro,
                "statusVeiculo", "LIBERADO",
                "valor", renovacao.getValor(),
                "codigoTransacao", renovacao.getCodigoTransacao(),
                "validoAte", renovacao.getValidadeAte().toString(),
                "mensagem", "Renovação simulada de R$ 10,00 aprovada. "
                        + "Novo prazo: 15 minutos."
        );
    }

    private RegistroEstacionamento buscar(Long idRegistro) {
        return repositorio.findById(idRegistro)
                .orElseThrow(() ->
                        new org.springframework.web.server.ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Veículo não encontrado."
                        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> tratarArgumento(
            IllegalArgumentException erro) {

        return ResponseEntity.badRequest()
                .body(Map.of("erro", erro.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> tratarEstado(
            IllegalStateException erro) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("erro", erro.getMessage()));
    }

    @ExceptionHandler(ValorInsuficienteException.class)
    public ResponseEntity<Map<String, String>> tratarValor(
            ValorInsuficienteException erro) {

        return ResponseEntity.badRequest()
                .body(Map.of("erro", erro.getMessage()));
    }
}
