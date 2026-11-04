
package com.tcc.sistema_estacionamento.nota_fiscal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/notas")
public class ControladorConsultaNota {

    private final ServicoConsultaNota servicoConsultaNota;

    // Construtor que recebe o serviço de consulta
    public ControladorConsultaNota(
            ServicoConsultaNota servicoConsultaNota) {

        this.servicoConsultaNota = servicoConsultaNota;
    }

    // Consulta os dados de uma nota fiscal pelo código
    @GetMapping("/{codigoNota}")
    public ResponseEntity<DadosNota> consultarNota(
            @PathVariable String codigoNota) {

        // Busca a nota no banco H2
        NotaFiscalSimulada nota =
                servicoConsultaNota.consultarPorCodigo(codigoNota);

        // Prepara somente os dados necessários para a tela
        DadosNota dados = new DadosNota(
                nota.getCodigoNota(),
                nota.getValorCompra(),
                nota.getHorarioEmissao(),
                nota.isUtilizada()
        );

        return ResponseEntity.ok(dados);
    }

    // Dados enviados para o navegador
    public record DadosNota(
            String codigoNota,
            BigDecimal valorCompra,
            LocalDateTime horarioEmissao,
            boolean utilizada
    ) {
    }

    // Caso a nota não seja encontrada
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> tratarNotaNaoEncontrada(
            NoSuchElementException erro) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("erro", erro.getMessage()));
    }

    // Caso o código informado seja inválido
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> tratarCodigoInvalido(
            IllegalArgumentException erro) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", erro.getMessage()));
    }
}
