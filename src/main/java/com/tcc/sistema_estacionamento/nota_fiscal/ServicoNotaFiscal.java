
package com.tcc.sistema_estacionamento.nota_fiscal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ServicoNotaFiscal {

    private final NotaFiscalSimuladaRepositorio repositorio;

    // Construtor que recebe o repositório
    public ServicoNotaFiscal(NotaFiscalSimuladaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    
    @Transactional
    public NotaFiscalSimulada gerarNota(BigDecimal valorCompra) {

        
        if (valorCompra == null) {
            throw new IllegalArgumentException(
                "Informe o valor da compra."
            );
        }

     
        if (valorCompra.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                "O valor da compra deve ser maior que zero."
            );
        }

        // Garante que o valor tenha no máximo duas casas decimais
        BigDecimal valorFormatado;

        try {
            valorFormatado = valorCompra.setScale(
                2, RoundingMode.UNNECESSARY
            );
        } catch (ArithmeticException erro) {
            throw new IllegalArgumentException(
                "O valor da compra deve ter no máximo duas casas decimais."
            );
        }

        // Gera um código único para identificar a nota
        String codigo = UUID.randomUUID().toString();

        // Cria um novo objeto de nota fiscal
        NotaFiscalSimulada nota = new NotaFiscalSimulada();

        // Preenche as informações da nota
        nota.setCodigoNota(codigo);
        nota.setValorCompra(valorFormatado);
        nota.setHorarioEmissao(LocalDateTime.now());
        nota.setUtilizada(false);

        // Salva a nota no banco H2
        return repositorio.save(nota);
    }
}
