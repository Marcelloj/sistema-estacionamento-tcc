
package com.tcc.sistema_estacionamento.consulta_veiculos;

import com.tcc.sistema_estacionamento.modelo.RegistroEstacionamento;
import com.tcc.sistema_estacionamento.excecao.RegistroNaoEncontradoException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/consultas")
public class ControladorConsulta {

    private final ServicoConsulta servicoConsulta;

    // Recebe o serviço por injeção de dependência
    public ControladorConsulta(ServicoConsulta servicoConsulta) {
        this.servicoConsulta = servicoConsulta;
    }

    // Consulta um veículo ativo pela placa
    @GetMapping("/ativas")
    public RegistroEstacionamento consultarVeiculo(
            @RequestParam String placa) {

        return servicoConsulta.consultarPorPlaca(placa);
    }

    // Retorna erro 400 quando a placa informada é inválida
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> tratarPlacaInvalida(
            IllegalArgumentException erro) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", erro.getMessage()));
    }

    // Retorna erro 404 quando não encontra o veículo
    @ExceptionHandler(RegistroNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> tratarRegistroNaoEncontrado(
            RegistroNaoEncontradoException erro) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("erro", erro.getMessage()));
    }
}
