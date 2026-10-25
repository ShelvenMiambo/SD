<%@ page contentType="text/html;charset=UTF-8" %>
<c:set var="titulo" value="Campanhas"/>
<c:set var="menu" value="campanhas"/>
<%@ include file="/WEB-INF/views/_header.jspf" %>
<c:set var="editar" value="${not empty campanha}"/>

<div class="cabecalho-pagina">
    <div>
        <div class="sobretitulo">Administração</div>
        <h1>Campanhas agrícolas</h1>
        <p>Campanhas com cultivos associados não podem ser removidas.</p>
    </div>
</div>

<div class="grelha-2">
    <section class="cartao tabela-wrap">
        <table>
            <thead>
            <tr><th>#</th><th>Nome</th><th>Início</th><th>Fim</th><th>Estado</th><th class="num">Cultivos</th><th>Ações</th></tr>
            </thead>
            <tbody>
            <c:forEach var="c" items="${campanhas}">
                <c:set var="n" value="${empty nCultivos[c.id] ? 0 : nCultivos[c.id]}"/>
                <tr>
                    <td>${c.id}</td>
                    <td><strong><c:out value="${c.nome}"/></strong></td>
                    <td>${ag:data(c.inicio)}</td>
                    <td>${ag:data(c.fim)}</td>
                    <td><span class="tag ${c.fechada ? 'fechada' : 'aberta'}">${c.fechada ? 'Fechada' : 'Aberta'}</span></td>
                    <td class="num"><a href="${ctx}/admin/cultivos?campanhaId=${c.id}">${n}</a></td>
                    <td>
                        <div class="acoes">
                            <a class="btn secundario pequeno" href="${ctx}/admin/campanhas?editar=${c.id}"><svg class="ic"><use href="#i-lapis"/></svg> Editar</a>
                            <form method="post" action="${ctx}/admin/campanhas" class="form-inline"><input type="hidden" name="_csrf" value="${csrf}">
                                <input type="hidden" name="acao" value="${c.fechada ? 'reabrir' : 'fechar'}">
                                <input type="hidden" name="id" value="${c.id}">
                                <button type="submit" class="btn secundario pequeno">${c.fechada ? 'Reabrir' : 'Fechar'}</button>
                            </form>
                            <form method="post" action="${ctx}/admin/campanhas" class="form-inline"
                                  onsubmit="return confirm('Remover esta campanha?')"><input type="hidden" name="_csrf" value="${csrf}">
                                <input type="hidden" name="acao" value="remover">
                                <input type="hidden" name="id" value="${c.id}">
                                <button type="submit" class="btn perigo pequeno" ${n > 0 ? 'title="Tem cultivos associados"' : ''}><svg class="ic"><use href="#i-lixo"/></svg> Remover</button>
                            </form>
                        </div>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty campanhas}">
                <tr><td colspan="7" class="vazio">Sem campanhas.</td></tr>
            </c:if>
            </tbody>
        </table>
    </section>

    <aside class="cartao">
        <h2>${editar ? 'Editar campanha' : 'Nova campanha'}</h2>
        <form method="post" action="${ctx}/admin/campanhas" class="form"><input type="hidden" name="_csrf" value="${csrf}">
            <input type="hidden" name="acao" value="${editar ? 'atualizar' : 'criar'}">
            <c:if test="${editar}"><input type="hidden" name="id" value="${campanha.id}"></c:if>
            <label>Nome <input type="text" name="nome" required maxlength="100" placeholder="Campanha 2026/2027" value="<c:out value='${campanha.nome}'/>"></label>
            <div class="form-linha">
                <label>Início <input type="date" name="inicio" required value="${ag:iso(campanha.inicio)}"></label>
                <label>Fim <input type="date" name="fim" required value="${ag:iso(campanha.fim)}"></label>
            </div>
            <span class="ajuda">O fim tem de ser depois do início; no máximo 24 meses. Os cultivos já registados têm de ficar dentro das datas.</span>
            <div class="acoes">
                <button type="submit" class="btn"><svg class="ic"><use href="#i-${editar ? 'guardar' : 'mais'}"/></svg> ${editar ? 'Guardar alterações' : 'Criar campanha'}</button>
                <c:if test="${editar}"><a class="btn secundario" href="${ctx}/admin/campanhas">Cancelar</a></c:if>
            </div>
        </form>
    </aside>
</div>

<%@ include file="/WEB-INF/views/_footer.jspf" %>
