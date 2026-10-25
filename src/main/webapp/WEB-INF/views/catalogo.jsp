<%@ page contentType="text/html;charset=UTF-8" %>
<c:set var="titulo" value="Catálogo"/>
<c:set var="menu" value="${modoAdmin ? 'catalogo-admin' : 'catalogo'}"/>
<%@ include file="/WEB-INF/views/_header.jspf" %>

<div class="cabecalho-pagina">
    <div>
        <div class="sobretitulo">Catálogo</div>
        <h1>Insumos para a campanha</h1>
        <p>
            <c:choose>
                <c:when test="${modoAdmin}">Vista do cliente, só para consulta: o administrador não efetua compras.</c:when>
                <c:otherwise>Olá, <c:out value="${u.nome}"/>. ${fn:length(produtos)} produto(s)<c:if test="${not empty categoriaSel}"> em ${fn:toLowerCase(categoriaSel)}</c:if><c:if test="${not empty q}"> para «<c:out value="${q}"/>»</c:if>.</c:otherwise>
            </c:choose>
        </p>
    </div>
    <c:if test="${not modoAdmin}">
        <a class="btn secundario" href="${ctx}/carrinho"><svg class="ic"><use href="#i-carrinho"/></svg> Carrinho (${carrinho.totalLinhas})</a>
    </c:if>
</div>

<div class="barra-filtros">
    <nav class="filtros" aria-label="Filtrar por categoria">
        <c:url var="urlTodas" value="/catalogo"><c:if test="${not empty q}"><c:param name="q" value="${q}"/></c:if></c:url>
        <a href="${urlTodas}" class="${empty categoriaSel ? 'ativo' : ''}">Todas</a>
        <c:forEach var="cat" items="${categorias}">
            <c:url var="urlCat" value="/catalogo"><c:param name="categoria" value="${cat}"/><c:if test="${not empty q}"><c:param name="q" value="${q}"/></c:if></c:url>
            <a href="${urlCat}" class="${cat == categoriaSel ? 'ativo' : ''}">${cat}</a>
        </c:forEach>
    </nav>
    <form method="get" action="${ctx}/catalogo" class="pesquisa" role="search">
        <c:if test="${not empty categoriaSel}"><input type="hidden" name="categoria" value="${categoriaSel}"></c:if>
        <svg class="ic"><use href="#i-lista"/></svg>
        <input type="search" name="q" value="<c:out value='${q}'/>" placeholder="Pesquisar produto…" maxlength="60" aria-label="Pesquisar produto">
        <button type="submit" class="btn secundario">Pesquisar</button>
    </form>
</div>

<c:choose>
    <c:when test="${empty produtos}">
        <div class="cartao vazio">
            <svg class="ic ic-lg"><use href="#i-pacote"/></svg>
            <p>Nenhum produto encontrado<c:if test="${not empty q}"> para «<c:out value="${q}"/>»</c:if>.</p>
            <c:if test="${not empty q or not empty categoriaSel}"><a class="btn secundario" href="${ctx}/catalogo">Ver todos</a></c:if>
        </div>
    </c:when>
    <c:otherwise>
        <div class="produtos">
            <c:forEach var="p" items="${produtos}">
                <c:set var="foto" value="${ag:foto(p.fotoUrl, p.categoria, ctx)}"/>
                <c:set var="noCarrinho" value="${empty carrinho ? 0 : carrinho.quantidadeDe(p.id)}"/>
                <c:set var="expirado" value="${ag:expirado(p.validade)}"/>
                <c:set var="podeAdicionar" value="${disponivel[p.id]}"/>
                <article class="produto ${p.quantidade == 0 or expirado ? 'indisponivel' : ''}" id="p${p.id}">
                    <div class="produto-foto">
                        <img src="<c:out value='${foto}'/>" alt="<c:out value='${p.nome}'/>" loading="lazy">
                        <span class="tag sobre-foto ${p.categoria}">${p.categoria}</span>
                    </div>
                    <div class="produto-corpo">
                        <h3><c:out value="${p.nome}"/></h3>
                        <p class="descricao"><c:out value="${p.descricao}"/></p>
                        <div class="preco">${ag:mt(p.preco)} <small>/ <c:out value="${p.unidade}"/></small></div>
                        <dl>
                            <dt>Disponível</dt>
                            <dd>
                                <c:choose>
                                    <c:when test="${p.quantidade > 0}">${p.quantidade} <c:out value="${p.unidade}"/></c:when>
                                    <c:otherwise><span class="esgotado">Esgotado</span></c:otherwise>
                                </c:choose>
                            </dd>
                            <dt>Validade</dt>
                            <dd>${ag:data(p.validade)}<c:if test="${expirado}"> <span class="tag alerta-stock">fora de validade</span></c:if></dd>
                            <dt>Fornecedor</dt>
                            <dd><c:out value="${fornecedores[p.fornecedorId].nome}"/></dd>
                            <c:if test="${noCarrinho > 0}">
                                <dt>No carrinho</dt>
                                <dd><span class="tag">${noCarrinho} <c:out value="${p.unidade}"/></span></dd>
                            </c:if>
                        </dl>
                        <c:if test="${not modoAdmin}">
                            <form method="post" action="${ctx}/carrinho/adicionar"><input type="hidden" name="_csrf" value="${csrf}">
                                <input type="hidden" name="produtoId" value="${p.id}">
                                <input type="hidden" name="categoria" value="${categoriaSel}">
                                <input type="hidden" name="q" value="<c:out value='${q}'/>">
                                <c:choose>
                                    <c:when test="${expirado}">
                                        <button type="button" class="btn bloco" disabled>Fora de validade</button>
                                    </c:when>
                                    <c:when test="${p.quantidade == 0}">
                                        <button type="button" class="btn bloco" disabled>Esgotado</button>
                                    </c:when>
                                    <c:when test="${podeAdicionar == 0}">
                                        <button type="button" class="btn secundario bloco" disabled>Já tem todo o stock no carrinho</button>
                                    </c:when>
                                    <c:otherwise>
                                        <input type="number" name="quantidade" value="1" min="1" max="${podeAdicionar}" step="1" required
                                               aria-label="Quantidade (máximo ${podeAdicionar})" title="Máximo ${podeAdicionar}">
                                        <button type="submit" class="btn">
                                            <svg class="ic"><use href="#i-carrinho"/></svg> Adicionar
                                        </button>
                                    </c:otherwise>
                                </c:choose>
                            </form>
                            <c:if test="${podeAdicionar > 0 and not expirado}">
                                <div class="limite">máx. ${podeAdicionar} <c:out value="${p.unidade}"/></div>
                            </c:if>
                        </c:if>
                    </div>
                </article>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/views/_footer.jspf" %>
