<%@ page contentType="text/html;charset=UTF-8" %>
<c:set var="titulo" value="Compras detalhadas"/>
<c:set var="menu" value="compras"/>
<%@ include file="/WEB-INF/views/_header.jspf" %>
<c:set var="base" value="/admin/compras?"/>

<div class="cabecalho-pagina">
    <div>
        <div class="sobretitulo">Administração</div>
        <h1>Compras detalhadas</h1>
        <p>${fn:length(vendas)} compra(s) · facturação total ${ag:mt(faturacao)}</p>
    </div>
    <div class="acoes">
        <span class="texto-suave pequeno">Ordenar por:</span>
        <t:ordenar campo="data" rotulo="Data" base="${base}"/>
        <t:ordenar campo="valor" rotulo="Valor" base="${base}"/>
        <t:ordenar campo="nome" rotulo="Cliente" base="${base}"/>
    </div>
</div>

<c:forEach var="v" items="${vendas}">
    <article class="compra-detalhe">
        <header>
            <div><strong><c:out value="${v.numeroFactura}"/></strong> · ${ag:dataHora(v.data)}</div>
            <div class="acoes">
                <strong>${ag:mt(v.total)}</strong>
                <a class="btn secundario pequeno" href="${ctx}/factura?id=${v.id}">Factura</a>
            </div>
        </header>
        <div class="cliente">
            <span><svg class="ic"><use href="#i-utilizador"/></svg><strong><c:out value="${v.clienteNome}"/></strong></span>
            <span><svg class="ic"><use href="#i-local"/></svg><c:out value="${v.clienteEndereco}"/></span>
            <span><svg class="ic"><use href="#i-telefone"/></svg><c:out value="${v.clienteTelefone}"/></span>
            <span><svg class="ic"><use href="#i-email"/></svg><c:out value="${v.clienteEmail}"/></span>
        </div>
        <div class="tabela-wrap">
            <table>
                <thead><tr><th>Produto</th><th class="num">Quantidade</th><th class="num">Valor unitário</th><th class="num">Subtotal</th></tr></thead>
                <tbody>
                <c:forEach var="i" items="${v.itens}">
                    <tr>
                        <td><c:out value="${i.produtoNome}"/></td>
                        <td class="num">${i.quantidade} <c:out value="${i.unidade}"/></td>
                        <td class="num">${ag:mt(i.precoUnit)}</td>
                        <td class="num">${ag:mt(i.subtotal)}</td>
                    </tr>
                </c:forEach>
                </tbody>
                <tfoot>
                <tr><td colspan="3" class="num">Subtotal</td><td class="num">${ag:mt(v.subtotal)}</td></tr>
                <tr><td colspan="3" class="num">IVA (16%)</td><td class="num">${ag:mt(v.iva)}</td></tr>
                <tr><td colspan="3" class="num">Total</td><td class="num">${ag:mt(v.total)}</td></tr>
                </tfoot>
            </table>
        </div>
    </article>
</c:forEach>
<c:if test="${empty vendas}"><div class="cartao vazio">Ainda não há compras.</div></c:if>

<%@ include file="/WEB-INF/views/_footer.jspf" %>
