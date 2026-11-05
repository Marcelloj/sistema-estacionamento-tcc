
package com.tcc.sistema_estacionamento.calculo_validacao;

import com.tcc.sistema_estacionamento.modelo.RegistroEstacionamento;
import com.tcc.sistema_estacionamento.modelo.StatusPagamento;
import com.tcc.sistema_estacionamento.repositorio.RegistroEstacionamentoRepositorio;
import com.tcc.sistema_estacionamento.nota_fiscal.NotaFiscalSimulada;
import com.tcc.sistema_estacionamento.nota_fiscal.NotaFiscalSimuladaRepositorio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ServicoAplicacaoIsencao {

    private final NotaFiscalSimuladaRepositorio repositorioNotas;
    private final RegistroEstacionamentoRepositorio repositorioRegistros;
    private final ServicoValidacaoFiscal servicoValidacao;
    private final ServicoCalculoPermanencia servicoCalculo;

    public ServicoAplicacaoIsencao(
            NotaFiscalSimuladaRepositorio repositorioNotas,
            RegistroEstacionamentoRepositorio repositorioRegistros,
            ServicoValidacaoFiscal servicoValidacao,
            ServicoCalculoPermanencia servicoCalculo) {

        this.repositorioNotas = repositorioNotas;
        this.repositorioRegistros = repositorioRegistros;
        this.servicoValidacao = servicoValidacao;
        this.servicoCalculo = servicoCalculo;
    }

    @Transactional
    public RegistroEstacionamento aplicarIsencao(
            String codigoNota, Long idRegistro) {

        if (codigoNota == null || codigoNota.isBlank()) {
            throw new IllegalArgumentException(
                    "Informe o codigo da nota fiscal."
            );
        }

        if (idRegistro == null) {
            throw new IllegalArgumentException(
                    "Informe o ID do estacionamento."
            );
        }

        // Bloqueia a nota durante sua utilizacao
        NotaFiscalSimulada nota = repositorioNotas
                .buscarParaUtilizacao(codigoNota)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Nota fiscal nao encontrada."
                        )
                );

        // Verifica valor minimo e uso anterior
        servicoValidacao.validarNota(codigoNota);

        // Busca o estacionamento
        RegistroEstacionamento registro = repositorioRegistros
                .findById(idRegistro)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Estacionamento nao encontrado."
                        )
                );

        // Confirma se o veiculo ainda esta estacionado
        if (registro.getHorarioSaida() != null) {
            throw new IllegalStateException(
                    "Este veiculo ja saiu do estacionamento."
            );
        }

        // Impede aplicar isencao a um pagamento finalizado
        if (registro.getStatusPagamento() != StatusPagamento.PENDENTE) {
            throw new IllegalStateException(
                    "Este estacionamento nao possui pagamento pendente."
            );
        }

        // Calcula quanto seria cobrado sem o beneficio
        BigDecimal tarifaOriginal =
                servicoCalculo.calcularTarifa(idRegistro);

        // Registra o valor original e aplica gratuidade
registro.setValorTarifaOriginal(tarifaOriginal);

// Zera o valor a pagar
registro.setValorTotal(BigDecimal.ZERO);

// Atualiza o pagamento para ISENTO
registro.setStatusPagamento(StatusPagamento.ISENTO);

        // Impede que a nota seja utilizada novamente
        nota.setUtilizada(true);
        nota.setIdRegistroUtilizacao(idRegistro);

        // Salva as alteracoes no banco H2
        repositorioNotas.save(nota);
        repositorioRegistros.save(registro);

        return registro;
    }
}
