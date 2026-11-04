// Tela de entrada: envia a placa ao Java (POST /entradas) e mostra o resultado.
(function () {
    const formulario = document.getElementById("form-entrada");
    const campoPlaca = document.getElementById("placa");
    const botao = document.getElementById("botao-entrada");
    const mensagem = document.getElementById("mensagem");
    const cronometro = document.getElementById("cronometro");

    let horarioEntrada = null;
    let intervalo = null;

    // Mostra a placa sempre em maiúsculas enquanto o usuário digita (só visual).
    campoPlaca.addEventListener("input", function () {
        campoPlaca.value = campoPlaca.value.toUpperCase();
    });

    function mostrarMensagem(texto, tipo) {
        mensagem.textContent = texto;
        mensagem.className = "mensagem mensagem--" + tipo;
        mensagem.hidden = false;
    }

    // O Java devolve LocalDateTime como texto ISO ("2026-11-03T22:44:10.123456")
    // ou, em algumas configurações, como lista [ano, mes, dia, hora, min, seg].
    function lerHorario(valor) {
        if (Array.isArray(valor)) {
            return new Date(valor[0], valor[1] - 1, valor[2], valor[3] || 0, valor[4] || 0, valor[5] || 0);
        }
        if (typeof valor === "string") {
            // Mantém no máximo 3 casas decimais nos segundos (compatível com todos os navegadores).
            return new Date(valor.replace(/(\.\d{3})\d+/, "$1"));
        }
        return null;
    }

    function doisDigitos(n) {
        return String(n).padStart(2, "0");
    }

    function atualizarCronometro() {
        if (!horarioEntrada) {
            return;
        }
        const total = Math.max(0, Math.floor((Date.now() - horarioEntrada.getTime()) / 1000));
        const horas = Math.floor(total / 3600);
        const minutos = Math.floor((total % 3600) / 60);
        const segundos = total % 60;
        cronometro.textContent = doisDigitos(horas) + ":" + doisDigitos(minutos) + ":" + doisDigitos(segundos);
    }

    function mostrarRegistro(registro) {
        horarioEntrada = lerHorario(registro.horarioEntrada);

        document.getElementById("dado-placa").textContent = registro.placa;
        document.getElementById("dado-entrada").textContent =
            horarioEntrada ? horarioEntrada.toLocaleString("pt-BR") : "—";
        // Estes campos só existem se a entidade do backend os enviar.
        document.getElementById("dado-veiculo").textContent = registro.statusVeiculo || "não informado";
        document.getElementById("dado-pagamento").textContent = registro.statusPagamento || "não informado";

        clearInterval(intervalo);
        atualizarCronometro();
        intervalo = setInterval(atualizarCronometro, 1000);
    }

    formulario.addEventListener("submit", async function (evento) {
        evento.preventDefault();

        const placa = campoPlaca.value.trim();
        if (placa === "") {
            mostrarMensagem("Digite a placa do veículo.", "erro");
            campoPlaca.focus();
            return;
        }

        botao.disabled = true;
        botao.textContent = "Registrando...";

        try {
            const resposta = await fetch("/entradas", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: new URLSearchParams({ placa: placa })
            });

            if (!resposta.ok) {
                // Em erro de validação o Java devolve a mensagem em texto simples.
                const texto = await resposta.text();
                mostrarMensagem(texto || "Não foi possível registrar a entrada.", "erro");
                return;
            }

            const registro = await resposta.json();
            mostrarRegistro(registro);
            mostrarMensagem("Entrada registrada com sucesso.", "sucesso");
            campoPlaca.value = "";
        } catch (erro) {
            mostrarMensagem("Não foi possível falar com o servidor. Ele está rodando?", "erro");
        } finally {
            botao.disabled = false;
            botao.textContent = "Registrar entrada";
        }
    });
})();