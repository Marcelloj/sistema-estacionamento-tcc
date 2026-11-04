
package com.tcc.sistema_estacionamento.nota_fiscal;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ServicoQrCode {

    // Recebe um endereço e gera a imagem do QR Code
    public byte[] gerarImagemQrCode(String enderecoValidacao) {

        // Verifica se o endereço foi informado
        if (enderecoValidacao == null || enderecoValidacao.isBlank()) {
            throw new IllegalArgumentException(
                "O endereço de validação não pode estar vazio."
            );
        }

        try {

            // Cria o gerador de QR Code
            QRCodeWriter gerador = new QRCodeWriter();

            // Converte o endereço em uma matriz de QR Code
            BitMatrix matriz = gerador.encode(
                enderecoValidacao,
                BarcodeFormat.QR_CODE,
                300,
                300
            );

            // Prepara a imagem para ser armazenada em memória
            ByteArrayOutputStream saida = new ByteArrayOutputStream();

            // Converte a matriz em uma imagem PNG
            MatrixToImageWriter.writeToStream(
                matriz,
                "PNG",
                saida
            );

            // Retorna os bytes da imagem
            return saida.toByteArray();

        } catch (WriterException | IOException erro) {

            throw new IllegalStateException(
                "Não foi possível gerar o QR Code.",
                erro
            );
        }
    }
}
