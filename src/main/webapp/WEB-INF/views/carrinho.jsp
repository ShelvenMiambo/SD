<%@ page contentType="text/html;charset=UTF-8" %>
<c:set var="titulo" value="Carrinho"/>
<c:set var="menu" value="carrinho"/>
<%@ include file="/WEB-INF/views/_header.jspf" %>

<div class="cabecalho-pagina">
    <div>
        <div class="sobretitulo">Loja</div>
        <h1>Carrinho de compras</h1>
        <p>${fn:length(linhas)} linha(s)</p>
    </div>
    <a class="btn secundario" href="${ctx}/catalogo">Continuar a comprar</a>
</div>

<c:choose>
    <c:when test="${empty linhas}">
        <div class="cartao vazio">
            <p>O seu carrinho está vazio.</p>
            <a class="btn" href="${ctx}/catalogo">Ir para o catálogo</a>
        </div>
    </c:when>
    <c:otherwise>
        <c:if test="${not stockOk}">
            <div class="alerta aviso">Alguns produtos já não têm stock suficiente. Ajuste as quantidades assinaladas antes de finalizar.</div>
        </c:if>
        <div class="grelha-2">
            <div class="cartao tabela-wrap">
                <table>
                    <thead>
                    <tr>
                        <th>Produto</th>
                        <th class="num">Preço unit.</th>
                        <th>Quantidade</th>
                        <th class="num">Subtotal</th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="l" items="${linhas}">
                        <tr>
                            <td>
                                <strong><c:out value="${l.produto.nome}"/></strong><br>
                                <span class="pequeno texto-suave">Disponível: ${l.produto.quantidade} <c:out value="${l.produto.unidade}"/></span>
                                <c:if test="${not l.stockSuficiente}"><br><span class="tag alerta-stock">stock insuficiente</span></c:if>
                            </td>
                            <td class="num">${ag:mt(l.produto.preco)}</td>
                            <td>
                                <form method="post" action="${ctx}/carrinho/atualizar" class="form-inline">
                                    <input type="hidden" name="produtoId" value="${l.produto.id}">
                                    <input type="number" name="quantidade" value="${l.quantidade}" min="0" aria-label="Quantidade">
                                    <button type="submit" class="btn secundario pequeno"><svg class="ic"><use href="#i-visto"/></svg> Atualizar</button>
                                </form>
                            </td>
                            <td class="num">${ag:mt(l.subtotal)}</td>
                            <td>
                                <form method="post" action="${ctx}/carrinho/remover" class="form-inline">
                                    <input type="hidden" name="produtoId" value="${l.produto.id}">
                                    <button type="submit" class="btn perigo pequeno"><svg class="ic"><use href="#i-lixo"/></svg> Remover</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
                <form method="post" action="${ctx}/carrinho/esvaziar" style="margin-top:1rem"
                      onsubmit="return confirm('Esvaziar o carrinho?')">
                    <button type="submit" class="btn perigo pequeno">Esvaziar carrinho</button>
                </form>
            </div>

            <aside class="cartao resumo">
                <h2>Resumo</h2>
                <dl>
                    <dt>Subtotal</dt><dd>${ag:mt(subtotal)}</dd>
                    <dt>IVA (16%)</dt><dd>${ag:mt(iva)}</dd>
                    <dt class="total">Total</dt><dd class="total">${ag:mt(total)}</dd>
                </dl>
                <c:choose>
                    <c:when test="${stockOk}">
                        <a class="btn bloco grande" href="${ctx}/compra">Finalizar compra</a>
                    </c:when>
                    <c:otherwise>
                        <button class="btn bloco grande" disabled>Finalizar compra</button>
                    </c:otherwise>
                </c:choose>
            </aside>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/views/_footer.jspf" %>
