
package com.tcc.sistema_estacionamento.interface_web;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.tcc.sistema_estacionamento.calculo_validacao.ServicoAplicacaoIsencao;
import com.tcc.sistema_estacionamento.modelo.RegistroEstacionamento;
import com.tcc.sistema_estacionamento.pagamento_saida.ServicoPagamentoSaida;
import com.tcc.sistema_estacionamento.repositorio.RegistroEstacionamentoRepositorio;

@RestController
@RequestMapping("/validacoes")
public class ControladorValidacaoMobile {

    private final RegistroEstacionamentoRepositorio repositorio;
    private final ServicoAplicacaoIsencao servicoIsencao;
    private final ServicoPagamentoSaida servicoSaida;

    public ControladorValidacaoMobile(
            RegistroEstacionamentoRepositorio repositorio,
            ServicoAplicacaoIsencao servicoIsencao,
            ServicoPagamentoSaida servicoSaida) {

        this.repositorio = repositorio;
        this.servicoIsencao = servicoIsencao;
        this.servicoSaida = servicoSaida;
    }

    /**
     * Recebe a nota fiscal e a placa informadas no celular.
     * Confere as regras de isencao e libera a cancela virtual.
     *
     * A saida definitiva do veiculo e registrada separadamente.
     */
    @PostMapping("/nota")
    @Transactional
    public RespostaValidacao validarNota(
            @RequestParam String codigoNota,
            @RequestParam String placa) {

        // 1. Valida os dados recebidos
        if (codigoNota == null || codigoNota.isBlank()) {
            throw new IllegalArgumentException(
                    "Informe o codigo da nota fiscal."
            );
        }

        if (placa == null || placa.isBlank()) {
            throw new IllegalArgumentException(
                    "Informe a placa do veiculo."
            );
        }

        String placaNormalizada =
                placa.trim().toUpperCase(Locale.ROOT);

        // 2. Busca um veiculo que ainda nao saiu
        RegistroEstacionamento registro = repositorio
                .findByPlacaAndHorarioSaidaIsNull(placaNormalizada)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Nao existe veiculo ativo com esta placa."
                ));

        // 3. Aplica a isencao usando as regras do backend:
        //    nota existente, nao utilizada e compra >= R$ 50.
        servicoIsencao.aplicarIsencao(
                codigoNota.trim(),
                registro.getId()
        );

        // 4. Libera a cancela virtual somente
        //    depois da isencao aprovada
        servicoSaida.tentarLiberarCancela(registro.getId());

        // 5. Consulta o registro atualizado
        RegistroEstacionamento atualizado = repositorio
                .findById(registro.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Registro de estacionamento nao encontrado."
                ));

        // 6. Devolve o resultado para a pagina do celular
        return new RespostaValidacao(
                atualizado.getId(),
                atualizado.getPlaca(),
                atualizado.getStatusPagamento().name(),
                atualizado.getStatusVeiculo().name(),
                atualizado.getValorTotal(),
                "Isencao aplicada e cancela virtual liberada."
        );
    }

    public record RespostaValidacao(
            Long idRegistro,
            String placa,
            String statusPagamento,
            String statusVeiculo,
            BigDecimal valorTotal,
            String mensagem
    ) {
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> tratarDadosInvalidos(
            IllegalArgumentException erro) {

        return ResponseEntity
                .badRequest()
                .body(Map.of("erro", erro.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> tratarRegraNegocio(
            IllegalStateException erro) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("erro", erro.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> tratarNaoEncontrado(
            NoSuchElementException erro) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("erro", erro.getMessage()));
    }
}
