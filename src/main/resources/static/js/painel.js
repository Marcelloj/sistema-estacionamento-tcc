// Painel do estacionamento - ETAPA 5: cadastro de veículos e cards.
// Os dados vêm do Java (POST /entradas). O JavaScript só mostra o que o servidor devolveu.
(function () {
    "use strict";

    const botaoAbrir = document.getElementById("botao-abrir-cadastro");
    const modal = document.getElementById("modal-cadastro");
    const formulario = document.getElementById("form-entrada");
    const campoPlaca = document.getElementById("placa");
    const botaoEnviar = document.getElementById("botao-entrada");
    const botaoCancelar = document.getElementById("botao-cancelar-cadastro");
    const mensagem = document.getElementById("mensagem-cadastro");
    const grade = document.getElementById("grade-veiculos");
    const mensagemVazio = document.getElementById("mensagem-vazio");
    const contador = document.getElementById("contador-patio");
    const modeloCard = document.getElementById("modelo-card");
    const avisos = document.getElementById("avisos");

    // Cards que estão na tela: id do registro -> { elemento, horarioEntrada }
    const cards = new Map();

    const ROTULOS_VEICULO = { ESTACIONADO: "Estacionado", LIBERADO: "Liberado", FINALIZADO: "Finalizado" };
    const ROTULOS_PAGAMENTO = { PENDENTE: "Pendente", PAGO: "Pago", ISENTO: "Isento" };

    // ---------- Utilidades ----------

    function doisDigitos(n) {
        return String(n).padStart(2, "0");
    }

    // O Java devolve LocalDateTime como texto ISO ("2026-11-04T21:50:10.123456")
    // ou como lista [ano, mes, dia, hora, min, seg].
    function lerHorario(valor) {
        if (Array.isArray(valor)) {
            return new Date(valor[0], valor[1] - 1, valor[2], valor[3] || 0, valor[4] || 0, valor[5] || 0);
        }
        if (typeof valor === "string") {
            return new Date(valor.replace(/(\.\d{3})\d+/, "$1"));
        }
        return null;
    }

    function formatarTempo(milissegundos) {
        const total = Math.max(0, Math.floor(milissegundos / 1000));
        const horas = Math.floor(total / 3600);
        const minutos = Math.floor((total % 3600) / 60);
        const segundos = total % 60;
        return doisDigitos(horas) + ":" + doisDigitos(minutos) + ":" + doisDigitos(segundos);
    }

    // ---------- Notificações ----------

    function mostrarAviso(texto, tipo) {
        const aviso = document.createElement("div");
        aviso.className = "aviso aviso--" + tipo;
        aviso.textContent = texto;
        avisos.appendChild(aviso);
        setTimeout(function () {
            aviso.remove();
        }, 4500);
    }

    function mostrarErroNoModal(texto) {
        mensagem.textContent = texto;
        mensagem.className = "mensagem mensagem--erro";
        mensagem.hidden = false;
    }

    // ---------- Cards ----------

    function atualizarContador() {
        contador.textContent = String(cards.size);
        mensagemVazio.hidden = cards.size > 0;
    }

    function preencherCard(elemento, registro) {
        const campo = function (nome) {
            return elemento.querySelector('[data-campo="' + nome + '"]');
        };
        const horario = lerHorario(registro.horarioEntrada);

        campo("placa").textContent = registro.placa;
        campo("entrada").textContent = horario ? horario.toLocaleString("pt-BR") : "—";

        const status = campo("statusVeiculo");
        status.textContent = ROTULOS_VEICULO[registro.statusVeiculo] || "Não informado";
        status.dataset.status = registro.statusVeiculo || "";

        const pagamento = campo("pagamento");
        pagamento.textContent = ROTULOS_PAGAMENTO[registro.statusPagamento] || "Não informado";
        pagamento.dataset.status = registro.statusPagamento || "";

        // A tarifa oficial é calculada pelo Java; será ligada na próxima etapa.
        campo("tarifa").textContent = "—";
    }

    function criarCard(registro) {
        if (cards.has(registro.id)) {
            return;
        }

        const elemento = modeloCard.content.firstElementChild.cloneNode(true);
        elemento.dataset.id = registro.id;
        preencherCard(elemento, registro);

        // O modal de detalhes será ligado em uma etapa seguinte.
        const botaoDetalhes = elemento.querySelector('[data-acao="detalhes"]');
        botaoDetalhes.disabled = true;
        botaoDetalhes.title = "Disponível nas próximas etapas";

        grade.prepend(elemento);
        cards.set(registro.id, { elemento: elemento, horarioEntrada: lerHorario(registro.horarioEntrada) });

        atualizarContador();
        atualizarRelogios();
    }

    // Um único relógio atualiza todos os cards a cada segundo
    function atualizarRelogios() {
        const agora = Date.now();
        cards.forEach(function (card) {
            if (!card.horarioEntrada) {
                return;
            }
            const relogio = card.elemento.querySelector('[data-campo="relogio"]');
            relogio.textContent = formatarTempo(agora - card.horarioEntrada.getTime());
        });
    }

    setInterval(atualizarRelogios, 1000);

    // ---------- Modal de cadastro ----------

    function abrirModal() {
        formulario.reset();
        mensagem.hidden = true;
        modal.showModal();
        campoPlaca.focus();
    }

    botaoAbrir.addEventListener("click", abrirModal);

    botaoCancelar.addEventListener("click", function () {
        modal.close();
    });

    // Mostra a placa em maiúsculas enquanto digita (só visual; o Java também padroniza)
    campoPlaca.addEventListener("input", function () {
        campoPlaca.value = campoPlaca.value.toUpperCase();
    });

    // ---------- Envio ao Java ----------

    async function lerMensagemDeErro(resposta) {
        const texto = await resposta.text();
        try {
            const json = JSON.parse(texto);
            if (json && json.erro) {
                return json.erro;
            }
        } catch (e) {
            // não era JSON: o /entradas devolve texto simples
        }
        return texto || "Não foi possível registrar a entrada.";
    }

    formulario.addEventListener("submit", async function (evento) {
        evento.preventDefault();

        const placa = campoPlaca.value.trim();
        if (placa === "") {
            mostrarErroNoModal("Digite a placa do veículo.");
            campoPlaca.focus();
            return;
        }

        botaoEnviar.disabled = true;
        botaoEnviar.textContent = "Registrando...";

        try {
            const resposta = await fetch("/entradas", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: new URLSearchParams({ placa: placa })
            });

            if (!resposta.ok) {
                mostrarErroNoModal(await lerMensagemDeErro(resposta));
                return;
            }

            const registro = await resposta.json();
            criarCard(registro);
            modal.close();
            mostrarAviso("Entrada registrada: " + registro.placa, "sucesso");
        } catch (erro) {
            mostrarErroNoModal("Não foi possível falar com o servidor. Ele está rodando?");
        } finally {
            botaoEnviar.disabled = false;
            botaoEnviar.textContent = "Registrar entrada";
        }
    });

    atualizarContador();
})();