package com.tcc.sistema_estacionamento.interface_web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ControladorPaginas {

    // Quando o navegador pedir "/", este método é executado
    @GetMapping("/")
    public String paginaInicial(Model modelo) {
        modelo.addAttribute("tituloSistema", "Estacionamento Digital");
        modelo.addAttribute("subtituloSistema",
                "Protótipo Acadêmico — Engenharia de Software | UNA Aimorés");
        return "inicio"; // procura templates/inicio.html
    }
}