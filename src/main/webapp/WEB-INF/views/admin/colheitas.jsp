<%@ page contentType="text/html;charset=UTF-8" %>
<c:set var="titulo" value="Colheitas"/>
<c:set var="menu" value="colheitas"/>
<%@ include file="/WEB-INF/views/_header.jspf" %>

<div class="cabecalho-pagina">
    <div>
        <div class="sobretitulo">Administração</div>
        <h1>Colheitas</h1>
        <p>${fn:length(colheitas)} colheita(s) · valor total ${ag:mt(total)}</p>
    </div>
    <form method="get" action="${ctx}/admin/colheitas" class="form-inline">
        <select name="cultivoId" onchange="this.form.submit()" aria-label="Filtrar por cultivo">
            <option value="0">Todos os cultivos</option>
            <c:forEach var="cv" items="${cultivos}">
                <option value="${cv.id}" ${cv.id == cultivoId ? 'selected' : ''}><c:out value="${cv.cultura} — ${campanhasPorId[cv.campanhaId].nome}"/></option>
            </c:forEach>
        </select>
        <noscript><button type="submit" class="btn secundario pequeno">Filtrar</button></noscript>
    </form>
</div>

<div class="grelha-2">
    <section class="cartao tabela-wrap">
        <table>
            <thead>
            <tr><th>#</th><th>Data</th><th>Cultivo</th><th class="num">Quantidade</th><th class="num">Preço/kg</th><th class="num">Valor total</th><th></th></tr>
            </thead>
            <tbody>
            <c:forEach var="col" items="${colheitas}">
                <c:set var="cv" value="${cultivosPorId[col.cultivoId]}"/>
                <tr>
                    <td>${col.id}</td>
                    <td>${ag:data(col.data)}</td>
                    <td><c:out value="${cv.cultura}"/> <span class="pequeno texto-suave"><c:out value="${cv.variedade}"/></span></td>
                    <td class="num">${ag:num(col.quantidadeKg)} kg</td>
                    <td class="num">${ag:mt(col.precoKg)}</td>
                    <td class="num"><strong>${ag:mt(col.valorTotal)}</strong></td>
                    <td>
                        <form method="post" action="${ctx}/admin/colheitas" class="form-inline"
                              onsubmit="return confirm('Remover esta colheita?')">
                            <input type="hidden" name="acao" value="remover">
                            <input type="hidden" name="id" value="${col.id}">
                            <input type="hidden" name="filtroCultivo" value="${cultivoId}">
                            <button type="submit" class="btn perigo pequeno"><svg class="ic"><use href="#i-lixo"/></svg> Remover</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty colheitas}">
                <tr><td colspan="7" class="vazio">Sem colheitas.</td></tr>
            </c:if>
            </tbody>
            <c:if test="${not empty colheitas}">
                <tfoot><tr><td colspan="5">Total</td><td class="num">${ag:mt(total)}</td><td></td></tr></tfoot>
            </c:if>
        </table>
    </section>

    <aside class="cartao">
        <h2>Registar colheita</h2>
        <c:if test="${empty cultivos}">
            <div class="alerta aviso">Registe primeiro um <a href="${ctx}/admin/cultivos">cultivo</a>.</div>
        </c:if>
        <form method="post" action="${ctx}/admin/colheitas" class="form">
            <input type="hidden" name="acao" value="criar">
            <input type="hidden" name="filtroCultivo" value="${cultivoId}">
            <label>Cultivo
                <select name="cultivoId" required>
                    <c:forEach var="cv" items="${cultivos}">
                        <option value="${cv.id}" ${cv.id == cultivoId ? 'selected' : ''}><c:out value="${cv.cultura} (${cv.variedade}) — ${campanhasPorId[cv.campanhaId].nome}"/></option>
                    </c:forEach>
                </select>
            </label>
            <label>Data <input type="date" name="data" required></label>
            <div class="form-linha">
                <label>Quantidade (kg) <input type="number" name="quantidadeKg" required min="0.01" step="0.01" placeholder="3200"></label>
                <label>Preço por kg (MT) <input type="number" name="precoKg" required min="0.01" step="0.01" placeholder="22"></label>
            </div>
            <p class="pequeno texto-suave">O valor total (kg × preço/kg) é calculado pelo sistema.</p>
            <button type="submit" class="btn"><svg class="ic"><use href="#i-mais"/></svg> Registar colheita</button>
        </form>
    </aside>
</div>

<%@ include file="/WEB-INF/views/_footer.jspf" %>
