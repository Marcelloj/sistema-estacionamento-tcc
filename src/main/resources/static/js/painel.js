
(function () {
    "use strict";

    const $ = (id) => document.getElementById(id);
    const cards = new Map();
    const notasDaSessao = new Map();

    const QUINZE_MINUTOS = 15 * 60 * 1000;

    let selecionado = null;
    let carregando = false;
    let operando = false;
    let ultimoAvisoConexao = 0;

    const rotulosVeiculo = {
        ESTACIONADO: "Estacionado",
        LIBERADO: "Liberado",
        FINALIZADO: "Finalizado"
    };

    const rotulosPagamento = {
        PENDENTE: "Pendente",
        PAGO: "Pago",
        ISENTO: "Isento"
    };

    function horario(valor) {
        if (Array.isArray(valor)) {
            return new Date(
                valor[0],
                valor[1] - 1,
                valor[2],
                valor[3] || 0,
                valor[4] || 0,
                valor[5] || 0
            );
        }

        if (typeof valor === "string") {
            return new Date(
                valor.replace(/(\.\d{3})\d+/, "$1")
            );
        }

        return null;
    }

    function dataValida(data) {
        return data instanceof Date &&
            !Number.isNaN(data.getTime());
    }

    function tempo(milissegundos) {
        const segundos = Math.max(
            0,
            Math.floor(milissegundos / 1000)
        );

        const horas = Math.floor(segundos / 3600);
        const minutos = Math.floor((segundos % 3600) / 60);
        const resto = segundos % 60;

        return [horas, minutos, resto]
            .map((n) => String(n).padStart(2, "0"))
            .join(":");
    }

    function moeda(valor) {
        if (
            valor === null ||
            valor === undefined ||
            !Number.isFinite(Number(valor))
        ) {
            return "A calcular";
        }

        return Number(valor).toLocaleString("pt-BR", {
            style: "currency",
            currency: "BRL"
        });
    }

    function dataFormatada(valor) {
        const data = horario(valor);

        return dataValida(data)
            ? data.toLocaleString("pt-BR")
            : "—";
    }

    function aviso(texto, tipo = "sucesso") {
        const elemento = document.createElement("div");

        elemento.className = "aviso aviso--" + tipo;
        elemento.textContent = texto;

        $("avisos").appendChild(elemento);

        setTimeout(() => elemento.remove(), 4500);
    }

    function mensagemOperacao(texto) {
        $("mensagem-operacao").textContent = texto;
        $("mensagem-operacao").hidden = false;
    }

    async function respostaErro(resposta) {
        const texto = await resposta.text();

        try {
            const dados = JSON.parse(texto);

            return dados.erro ||
                dados.detail ||
                dados.message ||
                "Operação não autorizada.";
        } catch (_) {
            return texto || "Operação não autorizada.";
        }
    }

    async function lerResposta(resposta) {
        const texto = await resposta.text();

        if (!texto) {
            return {};
        }

        try {
            const dados = JSON.parse(texto);

            return typeof dados === "object" && dados !== null
                ? dados
                : { mensagem: String(dados) };
        } catch (_) {
            return { mensagem: texto };
        }
    }

    function estaLiberado(registro) {
        return registro &&
            registro.statusVeiculo === "LIBERADO";
    }

    function estaPendente(registro) {
        return registro &&
            registro.statusVeiculo === "ESTACIONADO" &&
            registro.statusPagamento === "PENDENTE";
    }

    function prazoRestante(registro) {
        const liberacao = horario(
            registro.horarioLiberacaoCancela
        );

        if (!dataValida(liberacao)) {
            return 0;
        }

        return Math.max(
            0,
            liberacao.getTime() +
            QUINZE_MINUTOS -
            Date.now()
        );
    }

    function permanencia(registro) {
        const entrada = horario(registro.horarioEntrada);

        if (!dataValida(entrada)) {
            return 0;
        }

        // Mantém o horário original de liberação,
        // mesmo que o veículo renove sua saída.
        const primeiraLiberacao = horario(
            registro.horarioPrimeiraLiberacao ||
            registro.horarioLiberacaoCancela
        );

        const fim =
            estaLiberado(registro) &&
            dataValida(primeiraLiberacao)
                ? primeiraLiberacao.getTime()
                : Date.now();

        return Math.max(
            0,
            fim - entrada.getTime()
        );
    }

    function statusExibido(registro) {
        if (
            estaLiberado(registro) &&
            prazoRestante(registro) === 0
        ) {
            return "Validade expirada";
        }

        return rotulosVeiculo[registro.statusVeiculo] ||
            "Desconhecido";
    }

    function tarifaExibida(card) {
        if (card.registro.statusPagamento === "ISENTO") {
            return "R$ 0,00 (isento)";
        }

        return card.tarifaAtual === null
            ? "Consultando..."
            : moeda(card.tarifaAtual);
    }

    function atualizarContador() {
        $("contador-patio").textContent =
            String(cards.size);

        $("mensagem-vazio").hidden =
            cards.size > 0;
    }

    function preencherCard(card) {
        const registro = card.registro;

        const campo = (nome) =>
            card.elemento.querySelector(
                '[data-campo="' + nome + '"]'
            );

        campo("placa").textContent =
            registro.placa;

        campo("entrada").textContent =
            dataFormatada(registro.horarioEntrada);

        campo("statusVeiculo").textContent =
            statusExibido(registro);

        const expirado =
            estaLiberado(registro) &&
            prazoRestante(registro) === 0;

        campo("statusVeiculo").dataset.status =
            expirado
                ? "EXPIRADO"
                : (registro.statusVeiculo || "");

        campo("pagamento").textContent =
            rotulosPagamento[registro.statusPagamento] ||
            "Desconhecido";

        campo("pagamento").dataset.status =
            registro.statusPagamento || "";

        campo("tarifa").textContent =
            tarifaExibida(card);
    }

    function criarCard(registro) {
        const elemento = $("modelo-card")
            .content
            .firstElementChild
            .cloneNode(true);

        elemento.dataset.id = String(registro.id);

        const card = {
            elemento,
            registro,
            tarifaAtual: null,
            consultando: false
        };

        elemento.querySelector(
            '[data-acao="detalhes"]'
        ).addEventListener("click", () => {
            abrirDetalhes(registro.id);
        });

        cards.set(registro.id, card);

        $("grade-veiculos").prepend(elemento);

        preencherCard(card);
        atualizarTarifa(registro.id);
        atualizarContador();
    }

    function atualizarCard(registro) {
        const card = cards.get(registro.id);

        if (!card) {
            criarCard(registro);
            return;
        }

        card.registro = registro;
        preencherCard(card);
    }

    async function atualizarTarifa(id) {
        const card = cards.get(id);

        if (!card || card.consultando) {
            return;
        }

        card.consultando = true;

        try {
            const resposta = await fetch(
                "/tarifas/" + id,
                { cache: "no-store" }
            );

            if (!resposta.ok) {
                throw new Error("Tarifa indisponível.");
            }

            const dados = await resposta.json();

            if (
                dados.tarifaCalculada !== null &&
                dados.tarifaCalculada !== undefined &&
                Number.isFinite(
                    Number(dados.tarifaCalculada)
                )
            ) {
                card.tarifaAtual =
                    Number(dados.tarifaCalculada);
            }

            if (cards.get(id) !== card) {
                return;
            }

            preencherCard(card);

            if (selecionado === id) {
                atualizarDetalhes();
            }

        } catch (erro) {
            console.error(
                "Erro ao consultar tarifa:",
                erro
            );

        } finally {
            card.consultando = false;
        }
    }

    async function carregarVeiculos() {
        if (carregando) {
            return;
        }

        carregando = true;

        try {
            const resposta = await fetch(
                "/veiculos/ativos",
                { cache: "no-store" }
            );

            if (!resposta.ok) {
                throw new Error(
                    "Consulta do pátio falhou."
                );
            }

            const registros = await resposta.json();

            if (!Array.isArray(registros)) {
                throw new Error(
                    "Formato de resposta inválido."
                );
            }

            const idsAtivos = new Set(
                registros.map((r) => r.id)
            );

            // Remove somente os veículos que
            // já finalizaram sua permanência.
            for (const [id, card] of cards) {
                if (!idsAtivos.has(id)) {
                    card.elemento.remove();

                    cards.delete(id);
                    notasDaSessao.delete(id);

                    if (selecionado === id) {
                        $("modal-detalhes").close();
                        selecionado = null;
                    }
                }
            }

            // Atualiza o restante com os dados do H2.
            registros
                .slice()
                .reverse()
                .forEach(atualizarCard);

            atualizarContador();
            atualizarRelogios();

        } catch (erro) {
            console.error(
                "Erro ao carregar veículos:",
                erro
            );

            if (
                Date.now() - ultimoAvisoConexao >
                30000
            ) {
                aviso(
                    "Não foi possível atualizar os veículos.",
                    "erro"
                );

                ultimoAvisoConexao = Date.now();
            }

        } finally {
            carregando = false;
        }
    }

    function atualizarRelogios() {
        for (const card of cards.values()) {
            const registro = card.registro;

            card.elemento.querySelector(
                '[data-campo="relogio"]'
            ).textContent =
                tempo(permanencia(registro));

            const bloco = card.elemento.querySelector(
                '[data-bloco="prazo"]'
            );

            const liberado = estaLiberado(registro);

            bloco.hidden = !liberado;

            if (liberado) {
                const restante =
                    prazoRestante(registro);

                bloco.querySelector(
                    '[data-campo="prazo"]'
                ).textContent = tempo(restante);

                bloco.classList.toggle(
                    "expirado",
                    restante === 0
                );

                preencherCard(card);
            }
        }

        atualizarDetalhes();
    }

    function atualizarDetalhes() {
        if (
            selecionado === null ||
            !$("modal-detalhes").open
        ) {
            return;
        }

        const card = cards.get(selecionado);

        if (!card) {
            return;
        }

        const registro = card.registro;

        $("detalhes-placa").textContent =
            registro.placa;

        $("detalhes-entrada").textContent =
            dataFormatada(registro.horarioEntrada);

        $("detalhes-tempo").textContent =
            tempo(permanencia(registro));

        $("detalhes-status").textContent =
            statusExibido(registro);

        $("detalhes-pagamento").textContent =
            rotulosPagamento[
                registro.statusPagamento
            ] || "—";

        $("detalhes-tarifa").textContent =
            tarifaExibida(card);

        const pendente = estaPendente(registro);
        const liberado = estaLiberado(registro);

        const autorizacaoPendente =
            registro.statusVeiculo === "ESTACIONADO" &&
            registro.statusPagamento !== "PENDENTE";

        $("secao-pendente").hidden = !pendente;

        $("secao-autorizar").hidden =
            !autorizacaoPendente;

        $("secao-liberado").hidden =
            !liberado;

        if (pendente) {
            const dentroDaTolerancia =
                permanencia(registro) <
                QUINZE_MINUTOS;

            $("botao-validar-gratuidade").disabled =
                !dentroDaTolerancia || operando;

            $("informacao-gratuidade").textContent =
                dentroDaTolerancia
                    ? "Você pode sair gratuitamente até " +
                      "completar 15 minutos desde a entrada. " +
                      "A saída será finalizada imediatamente."
                    : "Os 15 minutos gratuitos terminaram. " +
                      "É necessário pagar ou validar uma nota elegível.";

            $("area-pagamento").hidden =
                dentroDaTolerancia;

            const valor = Number(card.tarifaAtual);

            $("botao-pagar").textContent =
                !dentroDaTolerancia &&
                card.tarifaAtual !== null &&
                Number.isFinite(valor) &&
                valor > 0
                    ? "Simular pagamento PIX — " +
                      moeda(valor)
                    : "Consultar tarifa e simular pagamento PIX";

            $("botao-pagar").disabled = operando;

            $("botao-gerar-nota").disabled =
                operando;
        }

        $("botao-liberar").disabled =
            operando;

        if (liberado) {
            const restante = prazoRestante(registro);
            const expirado = restante === 0;

            $("detalhes-prazo").textContent =
                tempo(restante);

            $("bloco-prazo").classList.toggle(
                "expirado",
                expirado
            );

            $("informacao-saida").textContent =
                expirado
                    ? "A autorização venceu. " +
                      "Renove por R$ 10,00 para receber " +
                      "mais 15 minutos."
                    : "Cancela liberada. Finalize a " +
                      "saída dentro dos 15 minutos.";

            $("botao-finalizar-saida").hidden =
                expirado;

            $("botao-finalizar-saida").disabled =
                expirado || operando;

            $("botao-renovar-saida").hidden =
                !expirado;

            $("botao-renovar-saida").disabled =
                !expirado || operando;
        }
    }

    function mostrarNota(nota) {
        $("resultado-nota").hidden = false;

        $("nota-codigo").textContent =
            nota.codigoNota;

        $("nota-valor").textContent =
            moeda(nota.valorCompra);

        $("nota-emissao").textContent =
            dataFormatada(nota.horarioEmissao);

        $("nota-qrcode").hidden = true;

        $("mensagem-qrcode").textContent =
            "Carregando QR Code...";

        $("nota-qrcode").src =
            "/notas/" +
            encodeURIComponent(nota.codigoNota) +
            "/qrcode";
    }

    function abrirDetalhes(id) {
        if (!cards.has(id)) {
            return;
        }

        selecionado = id;

        $("form-nota-fiscal").reset();

        $("mensagem-nota").hidden = true;
        $("resultado-nota").hidden = true;

        $("nota-qrcode").hidden = true;
        $("nota-qrcode").removeAttribute("src");

        $("mensagem-operacao").hidden = true;

        const nota = notasDaSessao.get(id);

        if (nota) {
            mostrarNota(nota);
        }

        $("modal-detalhes").showModal();

        atualizarDetalhes();
        atualizarTarifa(id);
    }

    function fecharDetalhes() {
        $("modal-detalhes").close();
        selecionado = null;
    }

    $("botao-fechar-detalhes")
        .addEventListener("click", fecharDetalhes);

    $("modal-detalhes")
        .addEventListener("close", () => {
            selecionado = null;
        });

    // =====================================
    // CADASTRO DE VEÍCULOS
    // =====================================

    $("botao-abrir-cadastro")
        .addEventListener("click", () => {
            $("form-entrada").reset();
            $("mensagem-cadastro").hidden = true;

            $("modal-cadastro").showModal();
            $("placa").focus();
        });

    $("botao-cancelar-cadastro")
        .addEventListener("click", () => {
            $("modal-cadastro").close();
        });

    $("placa").addEventListener("input", () => {
        $("placa").value =
            $("placa").value.toUpperCase();
    });

    $("form-entrada").addEventListener(
        "submit",
        async (evento) => {
            evento.preventDefault();

            const placa = $("placa")
                .value.trim().toUpperCase();

            if (!placa) {
                return;
            }

            const botao = $("botao-entrada");
            botao.disabled = true;

            try {
                const resposta = await fetch(
                    "/entradas",
                    {
                        method: "POST",
                        headers: {
                            "Content-Type":
                                "application/x-www-form-urlencoded"
                        },
                        body: new URLSearchParams({
                            placa
                        })
                    }
                );

                if (!resposta.ok) {
                    throw new Error(
                        await respostaErro(resposta)
                    );
                }

                const registro = await resposta.json();

                atualizarCard(registro);

                $("modal-cadastro").close();

                aviso(
                    "Entrada registrada: " +
                    registro.placa
                );

                await carregarVeiculos();

            } catch (erro) {
                $("mensagem-cadastro").textContent =
                    erro.message;

                $("mensagem-cadastro").hidden =
                    false;

            } finally {
                botao.disabled = false;
            }
        }
    );

    // =====================================
    // GERAÇÃO DE NOTA FISCAL
    // =====================================

    $("nota-qrcode").addEventListener("load", () => {
        $("nota-qrcode").hidden = false;

        $("mensagem-qrcode").textContent =
            "QR Code pronto.";
    });

    $("nota-qrcode").addEventListener("error", () => {
        $("mensagem-qrcode").textContent =
            "Não foi possível carregar o QR Code.";
    });

    $("form-nota-fiscal").addEventListener(
        "submit",
        async (evento) => {
            evento.preventDefault();

            const id = selecionado;
            const card = cards.get(id);

            if (
                !card ||
                !estaPendente(card.registro) ||
                operando
            ) {
                return;
            }

            const valor = Number(
                $("valor-compra").value
            );

            if (
                !Number.isFinite(valor) ||
                valor <= 0 ||
                !$("valor-compra").checkValidity()
            ) {
                return;
            }

            $("botao-gerar-nota").disabled = true;

            try {
                const resposta = await fetch(
                    "/notas",
                    {
                        method: "POST",
                        headers: {
                            "Content-Type":
                                "application/x-www-form-urlencoded"
                        },
                        body: new URLSearchParams({
                            valorCompra:
                                valor.toFixed(2)
                        })
                    }
                );

                if (!resposta.ok) {
                    throw new Error(
                        await respostaErro(resposta)
                    );
                }

                const nota = await resposta.json();

                if (!nota.codigoNota) {
                    throw new Error(
                        "Nota sem código."
                    );
                }

                notasDaSessao.set(id, nota);

                if (selecionado === id) {
                    mostrarNota(nota);

                    $("mensagem-nota").textContent =
                        valor >= 50
                            ? "Nota gerada. Leia o QR Code " +
                              "para validar a isenção."
                            : "Nota gerada, mas compras " +
                              "abaixo de R$ 50,00 não " +
                              "concedem isenção.";

                    $("mensagem-nota").hidden =
                        false;
                }

            } catch (erro) {
                $("mensagem-nota").textContent =
                    erro.message;

                $("mensagem-nota").hidden =
                    false;

            } finally {
                $("botao-gerar-nota").disabled =
                    false;

                atualizarDetalhes();
            }
        }
    );

    // =====================================
    // PAGAMENTO, GRATUIDADE E SAÍDA
    // =====================================

    async function executarOperacao(
        url,
        parametros = null
    ) {
        if (operando || selecionado === null) {
            return;
        }

        const id = selecionado;

        operando = true;

        mensagemOperacao(
            "Processando operação..."
        );

        atualizarDetalhes();

        try {
            const resposta = await fetch(
                url,
                {
                    method: "POST",
                    headers: parametros
                        ? {
                            "Content-Type":
                                "application/x-www-form-urlencoded"
                          }
                        : {},
                    body: parametros
                        ? new URLSearchParams(parametros)
                        : undefined
                }
            );

            if (!resposta.ok) {
                throw new Error(
                    await respostaErro(resposta)
                );
            }

            const dados =
                await lerResposta(resposta);

            aviso(
                dados.mensagem ||
                "Operação realizada com sucesso."
            );

            const finalizou =
                url.endsWith("/finalizar") ||
                url.endsWith("/gratuidade");

            if (finalizou) {
                cards.get(id)?.elemento.remove();

                cards.delete(id);
                notasDaSessao.delete(id);

                atualizarContador();

                if (
                    selecionado === id &&
                    $("modal-detalhes").open
                ) {
                    fecharDetalhes();
                }

            } else {
                mensagemOperacao(
                    dados.mensagem ||
                    "Operação realizada."
                );
            }

            await carregarVeiculos();

        } catch (erro) {
            mensagemOperacao(
                erro.message ||
                "Falha na operação."
            );

            await atualizarTarifa(id);

        } finally {
            operando = false;
            atualizarDetalhes();
        }
    }

    // SAÍDA GRATUITA ATÉ 15 MINUTOS

    $("botao-validar-gratuidade")
        .addEventListener("click", () => {
            if (selecionado === null) {
                return;
            }

            executarOperacao(
                "/operacoes/" +
                selecionado +
                "/gratuidade"
            );
        });

    // PAGAMENTO SIMULADO

    $("botao-pagar")
        .addEventListener("click", async () => {
            if (
                operando ||
                selecionado === null
            ) {
                return;
            }

            const id = selecionado;
            const card = cards.get(id);

            if (
                !card ||
                !estaPendente(card.registro)
            ) {
                return;
            }

            $("botao-pagar").disabled = true;

            try {
                // Consulta novamente o preço no backend.
                const resposta = await fetch(
                    "/tarifas/" + id,
                    { cache: "no-store" }
                );

                if (!resposta.ok) {
                    throw new Error(
                        await respostaErro(resposta)
                    );
                }

                const tarifa = await resposta.json();

                if (
                    tarifa.statusPagamento !==
                    "PENDENTE"
                ) {
                    throw new Error(
                        "O estacionamento já foi validado."
                    );
                }

                const valor = Number(
                    tarifa.valorPendente ??
                    tarifa.tarifaCalculada
                );

                if (
                    !Number.isFinite(valor) ||
                    valor <= 0
                ) {
                    throw new Error(
                        "Tarifa indisponível. " +
                        "Atualize a página e tente novamente."
                    );
                }

                card.tarifaAtual = valor;

                if (selecionado === id) {
                    await executarOperacao(
                        "/operacoes/" +
                        id +
                        "/pagamento",
                        {
                            valorPago:
                                valor.toFixed(2)
                        }
                    );
                }

            } catch (erro) {
                mensagemOperacao(
                    erro.message ||
                    "Erro ao consultar pagamento."
                );

            } finally {
                atualizarDetalhes();
            }
        });

    // LIBERAR CANCELA EM CASOS PENDENTES

    $("botao-liberar")
        .addEventListener("click", () => {
            if (selecionado === null) {
                return;
            }

            executarOperacao(
                "/saidas/" +
                selecionado +
                "/liberar"
            );
        });

    // FINALIZAR SAÍDA

    $("botao-finalizar-saida")
        .addEventListener("click", () => {
            const card = cards.get(selecionado);

            if (
                !card ||
                !estaLiberado(card.registro)
            ) {
                return;
            }

            if (
                prazoRestante(card.registro) <= 0
            ) {
                mensagemOperacao(
                    "A autorização de saída expirou."
                );
                return;
            }

            executarOperacao(
                "/saidas/" +
                selecionado +
                "/finalizar"
            );
        });

    // RENOVAÇÃO DE R$ 10,00

    $("botao-renovar-saida")
        .addEventListener("click", () => {
            const card = cards.get(selecionado);

            if (
                !card ||
                !estaLiberado(card.registro)
            ) {
                return;
            }

            if (
                prazoRestante(card.registro) > 0
            ) {
                mensagemOperacao(
                    "A autorização ainda está válida."
                );
                return;
            }

            executarOperacao(
                "/operacoes/" +
                selecionado +
                "/renovar"
            );
        });

    // =====================================
    // ATUALIZAÇÃO AUTOMÁTICA
    // =====================================

    // Relógios atualizados a cada segundo.
    setInterval(atualizarRelogios, 1000);

    // Sincroniza veículos com o H2 a cada 5 segundos.
    setInterval(carregarVeiculos, 5000);

    // Recalcula as tarifas a cada 30 segundos.
    setInterval(() => {
        for (const id of cards.keys()) {
            atualizarTarifa(id);
        }
    }, 30000);

    atualizarContador();
    carregarVeiculos();

})();
