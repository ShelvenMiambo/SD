<%@ page contentType="text/html;charset=UTF-8" %>
<c:set var="titulo" value="Cultivos"/>
<c:set var="menu" value="cultivos"/>
<%@ include file="/WEB-INF/views/_header.jspf" %>

<div class="cabecalho-pagina">
    <div>
        <div class="sobretitulo">Administração</div>
        <h1>Cultivos</h1>
        <p>Cultivos com colheitas associadas não podem ser removidos.</p>
    </div>
    <form method="get" action="${ctx}/admin/cultivos" class="form-inline">
        <select name="campanhaId" onchange="this.form.submit()" aria-label="Filtrar por campanha">
            <option value="0">Todas as campanhas</option>
            <c:forEach var="c" items="${campanhas}">
                <option value="${c.id}" ${c.id == campanhaId ? 'selected' : ''}><c:out value="${c.nome}"/></option>
            </c:forEach>
        </select>
        <noscript><button type="submit" class="btn secundario pequeno">Filtrar</button></noscript>
    </form>
</div>

<div class="grelha-2">
    <section class="cartao tabela-wrap">
        <table>
            <thead>
            <tr><th>#</th><th>Cultura</th><th>Variedade</th><th>Campanha</th><th class="num">Hectares</th><th>Plantio</th><th class="num">Colheitas</th><th class="num">Valor colhido</th><th></th></tr>
            </thead>
            <tbody>
            <c:forEach var="cv" items="${cultivos}">
                <tr>
                    <td>${cv.id}</td>
                    <td><strong><c:out value="${cv.cultura}"/></strong></td>
                    <td><c:out value="${cv.variedade}"/></td>
                    <td><c:out value="${campanhasPorId[cv.campanhaId].nome}"/></td>
                    <td class="num">${ag:num(cv.hectares)} ha</td>
                    <td>${ag:data(cv.dataPlantio)}</td>
                    <td class="num"><a href="${ctx}/admin/colheitas?cultivoId=${cv.id}">${nColheitas[cv.id]}</a></td>
                    <td class="num">${ag:mt(valorColhido[cv.id])}</td>
                    <td>
                        <form method="post" action="${ctx}/admin/cultivos" class="form-inline"
                              onsubmit="return confirm('Remover este cultivo?')"><input type="hidden" name="_csrf" value="${csrf}">
                            <input type="hidden" name="acao" value="remover">
                            <input type="hidden" name="id" value="${cv.id}">
                            <input type="hidden" name="filtroCampanha" value="${campanhaId}">
                            <button type="submit" class="btn perigo pequeno"><svg class="ic"><use href="#i-lixo"/></svg> Remover</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty cultivos}">
                <tr><td colspan="9" class="vazio">Sem cultivos.</td></tr>
            </c:if>
            </tbody>
        </table>
    </section>

    <aside class="cartao">
        <h2>Registar cultivo</h2>
        <form method="post" action="${ctx}/admin/cultivos" class="form"><input type="hidden" name="_csrf" value="${csrf}">
            <input type="hidden" name="acao" value="criar">
            <input type="hidden" name="filtroCampanha" value="${campanhaId}">
            <label>Campanha
                <select name="campanhaId" required>
                    <c:forEach var="c" items="${campanhas}">
                        <c:if test="${not c.fechada}">
                            <option value="${c.id}" ${c.id == campanhaId ? 'selected' : ''}><c:out value="${c.nome}"/> (${ag:data(c.inicio)} a ${ag:data(c.fim)})</option>
                        </c:if>
                    </c:forEach>
                </select>
            </label>
            <div class="form-linha">
                <label>Cultura
                    <select name="cultura" required data-variedades="${fn:escapeXml(variedadesJson)}">
                        <option value="">Escolha…</option>
                        <c:forEach var="cu" items="${culturas}">
                            <option value="${cu}">${cu}</option>
                        </c:forEach>
                        <option value="${outra}">Outra…</option>
                    </select>
                </label>
                <label>Variedade
                    <input type="text" name="variedade" maxlength="60" list="lista-variedades" placeholder="Escolha ou escreva">
                    <datalist id="lista-variedades"></datalist>
                </label>
            </div>
            <label data-cultura-outra hidden>Nome da cultura
                <input type="text" name="culturaOutra" maxlength="60" placeholder="Ex.: Pimento">
            </label>
            <div class="form-linha">
                <label>Hectares <input type="number" name="hectares" required min="0.01" max="100000" step="0.01"></label>
                <label>Data de plantio <input type="date" name="dataPlantio" required></label>
            </div>
            <span class="ajuda">A data de plantio tem de estar dentro das datas da campanha escolhida.</span>
            <button type="submit" class="btn"><svg class="ic"><use href="#i-mais"/></svg> Registar cultivo</button>
            <p class="pequeno texto-suave">Só aparecem campanhas abertas.</p>
        </form>
    </aside>
</div>

<%@ include file="/WEB-INF/views/_footer.jspf" %>
