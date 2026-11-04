
package com.tcc.sistema_estacionamento.nota_fiscal;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NotaFiscalSimuladaRepositorio
        extends JpaRepository<NotaFiscalSimulada, Long> {

    // Busca a nota e bloqueia durante a utilizacao
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT n
        FROM NotaFiscalSimulada n
        WHERE n.codigoNota = :codigoNota
        """)
    Optional<NotaFiscalSimulada> buscarParaUtilizacao(
            @Param("codigoNota") String codigoNota
    );

    // Procura uma nota pelo codigo unico
    Optional<NotaFiscalSimulada> findByCodigoNota(String codigoNota);

    // Verifica se ja existe uma nota com esse codigo
    boolean existsByCodigoNota(String codigoNota);
}
