
package com.tcc.sistema_estacionamento.nota_fiscal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/notas")
public class ControladorNotaFiscal {

    private final ServicoNotaFiscal servicoNotaFiscal;

    // Recebe o serviço responsável por gerar as notas
    public ControladorNotaFiscal(ServicoNotaFiscal servicoNotaFiscal) {
        this.servicoNotaFiscal = servicoNotaFiscal;
    }

    // Recebe o valor da compra e gera a nota
    @PostMapping
    public ResponseEntity<NotaFiscalSimulada> gerarNota(
            @RequestParam BigDecimal valorCompra) {

        NotaFiscalSimulada nota =
                servicoNotaFiscal.gerarNota(valorCompra);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(nota);
    }

    // Trata erros de validação do valor da compra
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> tratarErro(
            IllegalArgumentException erro) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", erro.getMessage()));
    }
}
