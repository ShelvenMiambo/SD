/* AgroGest — comportamento das páginas (tudo funciona também sem JavaScript; isto só melhora a experiência). */
(function () {
    'use strict';
    var raiz = document.documentElement;
    var corpo = document.body;

    /* ---------- Modo claro/escuro ---------- */
    document.querySelectorAll('[data-alternar-tema]').forEach(function (botao) {
        botao.addEventListener('click', function () {
            var novo = raiz.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
            raiz.setAttribute('data-theme', novo);
            try { localStorage.setItem('agrogest-tema', novo); } catch (e) { /* modo privado */ }
        });
    });

    /* ---------- Repor o formulário que falhou e assinalar o campo com erro ---------- */
    var anterior = null;
    try { anterior = JSON.parse(corpo.getAttribute('data-form-anterior') || 'null'); } catch (e) { anterior = null; }
    var campoErro = corpo.getAttribute('data-erro-campo');

    function formularioAlvo() {
        var forms = Array.prototype.slice.call(document.querySelectorAll('form.form'));
        if (anterior && anterior.acao) {
            var porAcao = forms.filter(function (f) {
                var a = f.querySelector('input[name="acao"]');
                return a && a.value === anterior.acao;
            });
            if (porAcao.length) { return porAcao[0]; }
        }
        if (campoErro) {
            var comCampo = forms.filter(function (f) { return f.querySelector('[name="' + campoErro + '"]'); });
            if (comCampo.length) { return comCampo[0]; }
        }
        return forms[0] || null;
    }

    var alvo = (anterior || campoErro) ? formularioAlvo() : null;
    if (alvo && anterior) {
        Object.keys(anterior).forEach(function (nome) {
            if (nome === 'acao' || nome === 'id') { return; }
            var campo = alvo.querySelector('[name="' + nome + '"]');
            if (!campo || campo.type === 'hidden' || campo.type === 'password') { return; }
            campo.value = anterior[nome];
            campo.dispatchEvent(new Event('change', { bubbles: true }));
        });
    }
    if (alvo && campoErro) {
        var errado = alvo.querySelector('[name="' + campoErro + '"]');
        var mensagem = document.querySelector('.alerta.erro span');
        if (errado && errado.type !== 'hidden') {
            errado.classList.add('invalido');
            errado.setAttribute('aria-invalid', 'true');
            if (mensagem) {
                var nota = document.createElement('span');
                nota.className = 'erro-campo';
                nota.id = 'erro-' + campoErro;
                nota.textContent = mensagem.textContent;
                (errado.closest('.campo-icone') || errado).insertAdjacentElement('afterend', nota);
                errado.setAttribute('aria-describedby', nota.id);
            }
            errado.focus({ preventScroll: false });
            errado.addEventListener('input', function limpar() {
                errado.classList.remove('invalido');
                errado.removeAttribute('aria-invalid');
                var n = document.getElementById('erro-' + campoErro);
                if (n) { n.remove(); }
                errado.removeEventListener('input', limpar);
            });
        }
    }

    /* ---------- Enviar cada formulário uma só vez (evita compras ou registos duplicados) ---------- */
    document.querySelectorAll('form').forEach(function (form) {
        form.addEventListener('submit', function (e) {
            if (e.defaultPrevented) { return; }        // o confirm() foi cancelado
            if (form.dataset.enviado) { e.preventDefault(); return; }
            form.dataset.enviado = '1';
            var botao = e.submitter || form.querySelector('button[type="submit"], button:not([type])');
            if (botao && form.method.toLowerCase() === 'post') {
                // O valor do botão (ex.: "rapido=50") tem de seguir mesmo com o botão desativado.
                if (botao.name) {
                    var oculto = document.createElement('input');
                    oculto.type = 'hidden';
                    oculto.name = botao.name;
                    oculto.value = botao.value;
                    form.appendChild(oculto);
                }
                setTimeout(function () {
                    botao.disabled = true;
                    if (botao.dataset.aProcessar !== undefined) { botao.textContent = 'A processar…'; }
                }, 0);
            }
        });
    });

    /* ---------- Cultivos: cultura "Outra" e variedades sugeridas ---------- */
    var cultura = document.querySelector('select[name="cultura"]');
    if (cultura) {
        var outra = document.querySelector('[data-cultura-outra]');
        var lista = document.getElementById('lista-variedades');
        var variedades = {};
        try { variedades = JSON.parse(cultura.getAttribute('data-variedades') || '{}'); } catch (e) { variedades = {}; }
        var atualizarCultura = function () {
            var eOutra = cultura.value === 'Outra';
            if (outra) {
                outra.hidden = !eOutra;
                var input = outra.querySelector('input');
                if (input) { input.required = eOutra; }
            }
            if (lista) {
                lista.innerHTML = '';
                (variedades[cultura.value] || []).forEach(function (v) {
                    var o = document.createElement('option');
                    o.value = v;
                    lista.appendChild(o);
                });
            }
        };
        cultura.addEventListener('change', atualizarCultura);
        atualizarCultura();
    }

    /* ---------- Produtos: só mostrar fornecedores que vendem a categoria escolhida ---------- */
    var categoria = document.querySelector('select[name="categoria"][data-filtra-fornecedor]');
    var fornecedor = document.querySelector('select[name="fornecedorId"]');
    if (categoria && fornecedor) {
        var filtrar = function () {
            var primeiroValido = null;
            Array.prototype.forEach.call(fornecedor.options, function (o) {
                var serve = (o.getAttribute('data-categorias') || '').split(' ').indexOf(categoria.value) >= 0;
                o.hidden = !serve;
                o.disabled = !serve;
                if (serve && !primeiroValido) { primeiroValido = o; }
            });
            var atual = fornecedor.options[fornecedor.selectedIndex];
            if ((!atual || atual.disabled) && primeiroValido) { fornecedor.value = primeiroValido.value; }
            var aviso = document.querySelector('[data-sem-fornecedor]');
            if (aviso) { aviso.hidden = !!primeiroValido; }
        };
        categoria.addEventListener('change', filtrar);
        filtrar();
    }

    /* ---------- Força da palavra-passe ---------- */
    document.querySelectorAll('input[data-forca]').forEach(function (campo) {
        var barra = document.getElementById(campo.getAttribute('data-forca'));
        if (!barra) { return; }
        campo.addEventListener('input', function () {
            var v = campo.value;
            var pontos = 0;
            if (v.length >= 8) { pontos++; }
            if (v.length >= 12) { pontos++; }
            if (/[a-z]/.test(v) && /[A-Z]/.test(v)) { pontos++; }
            if (/\d/.test(v)) { pontos++; }
            if (/[^A-Za-z0-9]/.test(v)) { pontos++; }
            var nivel = v.length === 0 ? '' : (v.length < 6 || pontos <= 1 ? 'fraca' : (pontos <= 3 ? 'media' : 'forte'));
            barra.setAttribute('data-nivel', nivel);
            barra.textContent = { '': '', fraca: 'Palavra-passe fraca', media: 'Palavra-passe razoável', forte: 'Palavra-passe forte' }[nivel];
        });
    });
})();
