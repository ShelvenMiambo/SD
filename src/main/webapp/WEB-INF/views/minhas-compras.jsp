<%@ page contentType="text/html;charset=UTF-8" %>
<c:set var="titulo" value="Minhas compras"/>
<c:set var="menu" value="compras"/>
<%@ include file="/WEB-INF/views/_header.jspf" %>

<div class="cabecalho-pagina">
    <div>
        <div class="sobretitulo">Histórico</div>
        <h1>Minhas compras</h1>
        <p>${fn:length(vendas)} compra(s) · total gasto <strong>${ag:mt(totalGasto)}</strong></p>
    </div>
</div>

<div class="cartao tabela-wrap">
    <c:choose>
        <c:when test="${empty vendas}">
            <div class="vazio">
                <p>Ainda não fez nenhuma compra.</p>
                <a class="btn" href="${ctx}/catalogo">Ver catálogo</a>
            </div>
        </c:when>
        <c:otherwise>
            <table>
                <thead>
                <tr>
                    <th>Factura</th>
                    <th>Data</th>
                    <th>Produtos</th>
                    <th class="num">Total</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="v" items="${vendas}">
                    <tr>
                        <td><strong><c:out value="${v.numeroFactura}"/></strong></td>
                        <td>${ag:dataHora(v.data)}</td>
                        <td>
                            <c:forEach var="i" items="${v.itens}" varStatus="st">
                                <c:out value="${i.produtoNome}"/> (${i.quantidade})<c:if test="${not st.last}">, </c:if>
                            </c:forEach>
                        </td>
                        <td class="num">${ag:mt(v.total)}</td>
                        <td><a class="btn secundario pequeno" href="${ctx}/factura?id=${v.id}">Ver factura</a></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="/WEB-INF/views/_footer.jspf" %>
