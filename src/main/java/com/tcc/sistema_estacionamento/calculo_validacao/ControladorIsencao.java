
package com.tcc.sistema_estacionamento.calculo_validacao;

import com.tcc.sistema_estacionamento.modelo.RegistroEstacionamento;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/isencoes")
public class ControladorIsencao {

    private final ServicoAplicacaoIsencao servico;

    public ControladorIsencao(ServicoAplicacaoIsencao servico) {
        this.servico = servico;
    }

    @PostMapping
    public ResponseEntity<?> aplicarIsencao(
            @RequestParam String codigoNota,
            @RequestParam Long idRegistro) {

        try {

            RegistroEstacionamento registro =
                    servico.aplicarIsencao(codigoNota, idRegistro);

            return ResponseEntity.ok(Map.of(
                    "mensagem", "Isencao aplicada com sucesso!",
                    "idRegistro", registro.getId(),
                    "placa", registro.getPlaca(),
                    "tarifaOriginal", registro.getValorTarifaOriginal(),
                    "tarifaFinal", registro.getValorTotal(),
                    "statusPagamento", registro.getStatusPagamento().name()
            ));

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                    Map.of("erro", e.getMessage())
            );

        } catch (IllegalStateException e) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("erro", e.getMessage()));
        }
    }
}
