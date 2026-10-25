<%@ page contentType="text/html;charset=UTF-8" %>
<c:set var="titulo" value="Confirmar compra"/>
<c:set var="menu" value="carrinho"/>
<%@ include file="/WEB-INF/views/_header.jspf" %>

<div class="cabecalho-pagina">
    <div>
        <div class="sobretitulo">Loja</div>
        <h1>Confirmar compra</h1>
        <p>Verifique os dados antes de confirmar. O stock é descontado e a factura é emitida de imediato.</p>
    </div>
</div>

<c:if test="${not stockOk}">
    <div class="alerta erro">Há produtos sem stock suficiente. <a href="${ctx}/carrinho">Volte ao carrinho</a> para ajustar as quantidades.</div>
</c:if>

<div class="grelha-2">
    <div class="cartao tabela-wrap">
        <h2>Itens</h2>
        <table>
            <thead>
            <tr>
                <th>Produto</th>
                <th class="num">Qtd.</th>
                <th class="num">Preço unit.</th>
                <th class="num">Subtotal</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="l" items="${linhas}">
                <tr>
                    <td><c:out value="${l.produto.nome}"/>
                        <c:if test="${not l.stockSuficiente}"> <span class="tag alerta-stock">só ${l.produto.quantidade} disponível</span></c:if>
                    </td>
                    <td class="num">${l.quantidade} <c:out value="${l.produto.unidade}"/></td>
                    <td class="num">${ag:mt(l.produto.preco)}</td>
                    <td class="num">${ag:mt(l.subtotal)}</td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>

    <aside>
        <div class="cartao">
            <h2>Dados de facturação</h2>
            <p><strong><c:out value="${cliente.nome}"/></strong><br>
                <c:out value="${cliente.email}"/><br>
                <c:out value="${cliente.telefone}"/><br>
                <c:out value="${cliente.endereco}"/></p>
        </div>
        <div class="cartao resumo">
            <dl>
                <dt>Subtotal</dt><dd>${ag:mt(subtotal)}</dd>
                <dt>IVA (16%)</dt><dd>${ag:mt(iva)}</dd>
                <dt class="total">Total</dt><dd class="total">${ag:mt(total)}</dd>
            </dl>
            <form method="post" action="${ctx}/compra">
                <button type="submit" class="btn bloco grande" ${stockOk ? '' : 'disabled'}>Confirmar e emitir factura</button>
            </form>
            <p style="margin-top:.6rem"><a href="${ctx}/carrinho">Voltar ao carrinho</a></p>
        </div>
    </aside>
</div>

<%@ include file="/WEB-INF/views/_footer.jspf" %>
