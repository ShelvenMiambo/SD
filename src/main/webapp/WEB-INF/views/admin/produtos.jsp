<%@ page contentType="text/html;charset=UTF-8" %>
<c:set var="titulo" value="Produtos"/>
<c:set var="menu" value="produtos"/>
<%@ include file="/WEB-INF/views/_header.jspf" %>
<c:set var="editar" value="${not empty produto}"/>

<div class="cabecalho-pagina">
    <div>
        <div class="sobretitulo">Administração</div>
        <h1>Produtos</h1>
        <p>${fn:length(produtos)} produto(s) no catálogo</p>
    </div>
    <a class="btn secundario" href="${ctx}/admin/listagens?tipo=produtos">Listagem ordenável</a>
</div>

<div class="grelha-2">
    <section class="cartao tabela-wrap">
        <table>
            <thead>
            <tr><th></th><th>Produto</th><th>Fornecedor</th><th class="num">Preço</th><th class="num">Stock</th><th>Validade</th><th>Ações</th></tr>
            </thead>
            <tbody>
            <c:forEach var="p" items="${produtos}">
                <c:set var="foto" value="${ag:foto(p.fotoUrl, p.categoria, ctx)}"/>
                <tr>
                    <td><img class="miniatura" src="<c:out value='${foto}'/>" alt=""></td>
                    <td><strong><c:out value="${p.nome}"/></strong><br><span class="tag ${p.categoria}">${p.categoria}</span></td>
                    <td><c:out value="${fornecedoresPorId[p.fornecedorId].nome}"/></td>
                    <td class="num">${ag:mt(p.preco)}<br><span class="pequeno texto-suave">/ <c:out value="${p.unidade}"/></span></td>
                    <td class="num">
                        <c:choose>
                            <c:when test="${p.quantidade == 0}"><span class="tag alerta-stock">0</span></c:when>
                            <c:otherwise>${p.quantidade}</c:otherwise>
                        </c:choose>
                    </td>
                    <td>${ag:data(p.validade)}</td>
                    <td>
                        <div class="acoes">
                            <form method="post" action="${ctx}/admin/produtos" class="form-inline">
                                <input type="hidden" name="acao" value="repor">
                                <input type="hidden" name="id" value="${p.id}">
                                <input type="number" name="quantidade" min="1" value="10" aria-label="Quantidade a repor">
                                <button type="submit" class="btn secundario pequeno"><svg class="ic"><use href="#i-mais"/></svg> Repor</button>
                            </form>
                            <a class="btn secundario pequeno" href="${ctx}/admin/produtos?editar=${p.id}"><svg class="ic"><use href="#i-lapis"/></svg> Editar</a>
                            <form method="post" action="${ctx}/admin/produtos" class="form-inline"
                                  onsubmit="return confirm('Remover este produto?')">
                                <input type="hidden" name="acao" value="remover">
                                <input type="hidden" name="id" value="${p.id}">
                                <button type="submit" class="btn perigo pequeno"><svg class="ic"><use href="#i-lixo"/></svg> Remover</button>
                            </form>
                        </div>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty produtos}">
                <tr><td colspan="7" class="vazio">Sem produtos.</td></tr>
            </c:if>
            </tbody>
        </table>
    </section>

    <aside class="cartao" id="formulario">
        <h2>${editar ? 'Editar produto' : 'Novo produto'}</h2>
        <c:if test="${empty fornecedores}">
            <div class="alerta aviso">Crie primeiro um <a href="${ctx}/admin/fornecedores">fornecedor</a>.</div>
        </c:if>
        <form method="post" action="${ctx}/admin/produtos" class="form">
            <input type="hidden" name="acao" value="${editar ? 'atualizar' : 'criar'}">
            <c:if test="${editar}"><input type="hidden" name="id" value="${produto.id}"></c:if>
            <label>Nome <input type="text" name="nome" required maxlength="100" value="<c:out value='${produto.nome}'/>"></label>
            <div class="form-linha">
                <label>Categoria
                    <select name="categoria" required>
                        <c:forEach var="cat" items="${categorias}">
                            <option value="${cat}" ${produto.categoria == cat ? 'selected' : ''}>${cat}</option>
                        </c:forEach>
                    </select>
                </label>
                <label>Fornecedor
                    <select name="fornecedorId" required>
                        <c:forEach var="f" items="${fornecedores}">
                            <option value="${f.id}" ${produto.fornecedorId == f.id ? 'selected' : ''}><c:out value="${f.nome}"/></option>
                        </c:forEach>
                    </select>
                </label>
            </div>
            <div class="form-linha">
                <label>Preço (MT) <input type="number" name="preco" required min="0.01" step="0.01" value="${produto.preco}"></label>
                <label>Quantidade <input type="number" name="quantidade" required min="0" step="1" value="${editar ? produto.quantidade : ''}"></label>
                <label>Unidade <input type="text" name="unidade" required maxlength="20" placeholder="kg, saco, unidade" value="<c:out value='${produto.unidade}'/>"></label>
            </div>
            <label>Validade <input type="date" name="validade" value="${ag:iso(produto.validade)}"></label>
            <label>Foto
                <input type="text" name="fotoUrl" list="fotos-locais" maxlength="500"
                       placeholder="Vazio = fotografia da categoria" value="<c:out value='${produto.fotoUrl}'/>">
                <span class="pequeno texto-suave" style="font-weight:400">Escolha uma fotografia da lista ou cole um URL https://</span>
            </label>
            <datalist id="fotos-locais">
                <option value="/img/produtos/milho.jpg">Grãos de milho</option>
                <option value="/img/produtos/feijao-nhemba.jpg">Feijão nhemba</option>
                <option value="/img/produtos/adubo.jpg">Adubo granulado</option>
                <option value="/img/produtos/enxada.jpg">Enxada</option>
                <option value="/img/produtos/trator.jpg">Trator</option>
            </datalist>
            <label>Descrição <textarea name="descricao" maxlength="1000"><c:out value="${produto.descricao}"/></textarea></label>
            <div class="acoes">
                <button type="submit" class="btn">${editar ? 'Guardar alterações' : 'Criar produto'}</button>
                <c:if test="${editar}"><a class="btn secundario" href="${ctx}/admin/produtos">Cancelar</a></c:if>
            </div>
        </form>
    </aside>
</div>

<%@ include file="/WEB-INF/views/_footer.jspf" %>
