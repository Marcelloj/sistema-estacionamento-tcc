
package com.tcc.sistema_estacionamento.interface_web;

import com.tcc.sistema_estacionamento.nota_fiscal.NotaFiscalSimulada;
import com.tcc.sistema_estacionamento.nota_fiscal.ServicoConsultaNota;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ControladorPaginaValidacao {

    private final ServicoConsultaNota servicoConsultaNota;

    private static final BigDecimal VALOR_MINIMO =
            new BigDecimal("50.00");

    public ControladorPaginaValidacao(
            ServicoConsultaNota servicoConsultaNota) {

        this.servicoConsultaNota = servicoConsultaNota;
    }

    @GetMapping("/notas/validar/{codigoNota}")
    public String abrirPaginaValidacao(
            @PathVariable String codigoNota,
            Model model) {

        try {

            // Consulta a nota fiscal existente no banco H2
            NotaFiscalSimulada nota =
                    servicoConsultaNota.consultarPorCodigo(codigoNota);

            // Confere se a compra atingiu R$ 50,00
            boolean valorSuficiente =
                    nota.getValorCompra().compareTo(VALOR_MINIMO) >= 0;

            boolean utilizada = nota.isUtilizada();

            // A nota precisa ter valor suficiente
            // e não pode ter sido utilizada antes.
            boolean elegivel = valorSuficiente && !utilizada;

            NumberFormat formatoMoeda =
                    NumberFormat.getCurrencyInstance(
                            Locale.forLanguageTag("pt-BR")
                    );

            DateTimeFormatter formatoData =
                    DateTimeFormatter.ofPattern(
                            "dd/MM/yyyy 'às' HH:mm"
                    );

            model.addAttribute("notaDisponivel", true);
            model.addAttribute("codigoNota", nota.getCodigoNota());

            model.addAttribute(
                    "valorCompra",
                    formatoMoeda.format(nota.getValorCompra())
            );

            model.addAttribute(
                    "valorMinimo",
                    formatoMoeda.format(VALOR_MINIMO)
            );

            model.addAttribute(
                    "horarioEmissao",
                    nota.getHorarioEmissao().format(formatoData)
            );

            model.addAttribute("utilizada", utilizada);
            model.addAttribute("elegivel", elegivel);

        } catch (NoSuchElementException | IllegalArgumentException erro) {

            model.addAttribute("notaDisponivel", false);
            model.addAttribute(
                    "erro",
                    "Nota fiscal não encontrada ou código inválido."
            );
        }

        // Nome do arquivo HTML em templates/
        return "validar-nota";
    }
}
