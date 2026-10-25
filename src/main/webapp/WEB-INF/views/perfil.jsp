<%@ page contentType="text/html;charset=UTF-8" %>
<c:set var="titulo" value="O meu perfil"/>
<c:set var="menu" value="perfil"/>
<%@ include file="/WEB-INF/views/_header.jspf" %>

<div class="cabecalho-pagina">
    <div>
        <div class="sobretitulo">Conta</div>
        <h1>O meu perfil</h1>
        <p>Cliente desde ${ag:dataHora(u.dataRegisto)}</p>
    </div>
</div>

<div class="grelha-2">
    <section class="cartao">
        <h2><svg class="ic"><use href="#i-utilizador"/></svg> Dados de facturação</h2>
        <p class="pequeno texto-suave">Estes dados aparecem nas próximas facturas. As facturas já emitidas não mudam.</p>
        <form method="post" action="${ctx}/perfil" class="form"><input type="hidden" name="_csrf" value="${csrf}">
            <input type="hidden" name="acao" value="dados">
            <label>Email
                <input type="email" value="<c:out value='${u.email}'/>" disabled>
                <span class="ajuda">O email identifica a conta e não pode ser alterado.</span>
            </label>
            <label>Nome completo
                <input type="text" name="nome" value="<c:out value='${u.nome}'/>" required minlength="3" maxlength="100" autocomplete="name">
            </label>
            <label>Telefone
                <input type="tel" name="telefone" value="<c:out value='${u.telefone}'/>" required maxlength="30" autocomplete="tel"
                       pattern="\+?[0-9 \(\)\-]{8,20}" title="Número moçambicano, ex.: +258 84 123 4567">
            </label>
            <div class="form-linha">
                <label>Província
                    <select name="provincia" required>
                        <c:forEach var="pv" items="${provincias}">
                            <option value="${pv}" ${u.provincia == pv ? 'selected' : ''}>${pv}</option>
                        </c:forEach>
                    </select>
                </label>
                <label>Endereço
                    <input type="text" name="endereco" value="<c:out value='${u.endereco}'/>" required maxlength="200" autocomplete="street-address">
                </label>
            </div>
            <div><button type="submit" class="btn"><svg class="ic"><use href="#i-guardar"/></svg> Guardar dados</button></div>
        </form>
    </section>

    <aside class="cartao">
        <h2><svg class="ic"><use href="#i-cadeado"/></svg> Palavra-passe</h2>
        <form method="post" action="${ctx}/perfil" class="form"><input type="hidden" name="_csrf" value="${csrf}">
            <input type="hidden" name="acao" value="password">
            <label>Palavra-passe actual
                <input type="password" name="atual" required autocomplete="current-password">
            </label>
            <label>Nova palavra-passe
                <input type="password" name="nova" required minlength="4" maxlength="72" autocomplete="new-password" data-forca="forca-perfil">
                <span class="forca" id="forca-perfil" aria-live="polite"></span>
            </label>
            <label>Confirmar nova
                <input type="password" name="confirmacao" required minlength="4" maxlength="72" autocomplete="new-password">
            </label>
            <div><button type="submit" class="btn secundario">Alterar palavra-passe</button></div>
        </form>
    </aside>
</div>

<%@ include file="/WEB-INF/views/_footer.jspf" %>
