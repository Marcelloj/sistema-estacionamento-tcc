/*
 * RESPONSÁVEL: Marcello
 *
 * OBJETIVO:
 * Receber requisições de entrada de veículos
 * e encaminhá-las para o ServicoEntrada.
 */


package com.tcc.sistema_estacionamento.entrada_veiculos;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.tcc.sistema_estacionamento.modelo.RegistroEstacionamento;


@RestController
@RequestMapping("/entradas")
public class ControladorEntrada {

    private final ServicoEntrada servicoEntrada;

    // Construtor responsável por receber o serviço
    public ControladorEntrada(ServicoEntrada servicoEntrada) {
        this.servicoEntrada = servicoEntrada;
    }

    // Recebe a placa e chama o serviço para registrar a entrada
    @PostMapping
    public RegistroEstacionamento registrarEntrada(@RequestParam String placa) {
        return servicoEntrada.registrarEntrada(placa);
    }

    // Trata erros de validação e retorna HTTP 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> tratarErro(IllegalArgumentException erro) {
        return ResponseEntity.badRequest().body(erro.getMessage());
}
}
