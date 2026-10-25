/* AgroGest — alternar modo claro/escuro (a preferência fica guardada no browser). */
(function () {
    'use strict';
    var CHAVE = 'agrogest-tema';
    var raiz = document.documentElement;

    document.querySelectorAll('[data-alternar-tema]').forEach(function (botao) {
        botao.addEventListener('click', function () {
            var novo = raiz.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
            raiz.setAttribute('data-theme', novo);
            try { localStorage.setItem(CHAVE, novo); } catch (e) { /* modo privado */ }
        });
    });
})();
