// Abre e fecha o menu no celular
(function () {
    const botao = document.getElementById("menu-botao");
    const menu = document.querySelector(".menu");

    if (!botao || !menu) {
        return;
    }

    function definirAberto(aberto) {
        menu.classList.toggle("menu--aberto", aberto);
        botao.setAttribute("aria-expanded", String(aberto));
    }

    botao.addEventListener("click", function () {
        definirAberto(!menu.classList.contains("menu--aberto"));
    });

    document.addEventListener("keydown", function (evento) {
        if (evento.key === "Escape") {
            definirAberto(false);
        }
    });
})();