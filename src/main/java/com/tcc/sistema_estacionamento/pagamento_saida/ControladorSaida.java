
package com.tcc.sistema_estacionamento.pagamento_saida;

import com.tcc.sistema_estacionamento.excecao.RegistroNaoEncontradoException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/saidas")
public class ControladorSaida {

    private final ServicoPagamentoSaida servico;

    public ControladorSaida(ServicoPagamentoSaida servico) {
        this.servico = servico;
    }

    // Libera a cancela para veiculos pagos ou isentos
    @PostMapping("/{idRegistro}/liberar")
    public ResponseEntity<?> liberarCancela(
            @PathVariable Long idRegistro) {

        try {
            servico.tentarLiberarCancela(idRegistro);

            return ResponseEntity.ok(Map.of(
                    "mensagem", "Cancela liberada com sucesso!",
                    "idRegistro", idRegistro
            ));

        } catch (RegistroNaoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", e.getMessage()));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    // Registra a saida definitiva do veiculo
    @PostMapping("/{idRegistro}/finalizar")
    public ResponseEntity<?> finalizarSaida(
            @PathVariable Long idRegistro) {

        try {
            servico.registrarSaida(idRegistro);

            return ResponseEntity.ok(Map.of(
                    "mensagem", "Saida registrada com sucesso!",
                    "idRegistro", idRegistro
            ));

        } catch (RegistroNaoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", e.getMessage()));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("erro", e.getMessage()));
        }
    }
}
