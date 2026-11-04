
package com.tcc.sistema_estacionamento.nota_fiscal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notas")
public class ControladorQrCode {

    private final ServicoQrCode servicoQrCode;
    private final NotaFiscalSimuladaRepositorio repositorio;

    @Value("${app.url-base:http://localhost:8080}")
    private String urlBase;

    public ControladorQrCode(
            ServicoQrCode servicoQrCode,
            NotaFiscalSimuladaRepositorio repositorio) {

        this.servicoQrCode = servicoQrCode;
        this.repositorio = repositorio;
    }

    // Gera a imagem PNG do QR Code de uma nota existente
    @GetMapping(
        value = "/{codigoNota}/qrcode",
        produces = MediaType.IMAGE_PNG_VALUE
    )
    public ResponseEntity<byte[]> gerarQrCode(
            @PathVariable String codigoNota) {

        // Verifica se a nota existe no banco H2
        if (repositorio.findByCodigoNota(codigoNota).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Monta o endereço que será aberto pelo celular
        String enderecoValidacao =
                urlBase.replaceAll("/+$", "")
                + "/notas/validar/" + codigoNota;

        // Transforma o endereço em imagem PNG
        byte[] imagem = servicoQrCode
                .gerarImagemQrCode(enderecoValidacao);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(imagem);
    }
}
