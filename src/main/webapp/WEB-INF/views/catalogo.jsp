<%@ page contentType="text/html;charset=UTF-8" %>
<c:set var="titulo" value="Catálogo"/>
<c:set var="menu" value="catalogo"/>
<%@ include file="/WEB-INF/views/_header.jspf" %>

<div class="cabecalho-pagina">
    <div>
        <div class="sobretitulo">Catálogo</div>
        <h1>Insumos para a campanha</h1>
        <p>Olá, <c:out value="${u.nome}"/>. ${fn:length(produtos)} produto(s)<c:if test="${not empty categoriaSel}"> em ${fn:toLowerCase(categoriaSel)}</c:if>, com stock e preços actualizados.</p>
    </div>
    <a class="btn secundario" href="${ctx}/carrinho"><svg class="ic"><use href="#i-carrinho"/></svg> Carrinho (${carrinho.totalLinhas})</a>
</div>

<nav class="filtros" aria-label="Filtrar por categoria">
    <a href="${ctx}/catalogo" class="${empty categoriaSel ? 'ativo' : ''}">Todas</a>
    <c:forEach var="cat" items="${categorias}">
        <a href="${ctx}/catalogo?categoria=${cat}" class="${cat == categoriaSel ? 'ativo' : ''}">${cat}</a>
    </c:forEach>
</nav>

<c:choose>
    <c:when test="${empty produtos}">
        <div class="cartao vazio"><svg class="ic ic-lg"><use href="#i-pacote"/></svg><p>Não há produtos nesta categoria.</p></div>
    </c:when>
    <c:otherwise>
        <div class="produtos">
            <c:forEach var="p" items="${produtos}">
                <c:set var="foto" value="${ag:foto(p.fotoUrl, p.categoria, ctx)}"/>
                <c:set var="noCarrinho" value="${carrinho.quantidadeDe(p.id)}"/>
                <article class="produto">
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
                            <dd>${ag:data(p.validade)}<c:if test="${ag:expirado(p.validade)}"> <span class="tag alerta-stock">expirado</span></c:if></dd>
                            <dt>Fornecedor</dt>
                            <dd><c:out value="${fornecedores[p.fornecedorId].nome}"/></dd>
                            <c:if test="${noCarrinho > 0}">
                                <dt>No carrinho</dt>
                                <dd><span class="tag">${noCarrinho} <c:out value="${p.unidade}"/></span></dd>
                            </c:if>
                        </dl>
                        <form method="post" action="${ctx}/carrinho/adicionar">
                            <input type="hidden" name="produtoId" value="${p.id}">
                            <input type="hidden" name="categoria" value="${categoriaSel}">
                            <input type="number" name="quantidade" value="1" min="1" aria-label="Quantidade"
                                   ${p.quantidade == 0 ? 'disabled' : ''}>
                            <button type="submit" class="btn" ${p.quantidade == 0 ? 'disabled' : ''}>
                                <svg class="ic"><use href="#i-carrinho"/></svg> Adicionar
                            </button>
                        </form>
                    </div>
                </article>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/views/_footer.jspf" %>
