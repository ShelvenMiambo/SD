<%@ page contentType="text/html;charset=UTF-8" %>
<c:set var="titulo" value="Factura ${venda.numeroFactura}"/>
<c:set var="menu" value="compras"/>
<%@ include file="/WEB-INF/views/_header.jspf" %>

<div class="factura">
    <div class="acoes nao-imprimir" style="margin-bottom:1rem">
        <button type="button" class="btn" onclick="window.print()"><svg class="ic"><use href="#i-impressora"/></svg> Imprimir factura</button>
        <c:choose>
            <c:when test="${u.admin}"><a class="btn secundario" href="${ctx}/admin/compras"><svg class="ic"><use href="#i-seta-esq"/></svg> Voltar às compras</a></c:when>
            <c:otherwise><a class="btn secundario" href="${ctx}/minhas-compras"><svg class="ic"><use href="#i-seta-esq"/></svg> Minhas compras</a></c:otherwise>
        </c:choose>
    </div>

    <article class="cartao">
        <c:if test="${venda.anulada}">
            <div class="carimbo-anulada">Anulada em ${ag:dataHora(venda.dataAnulacao)}</div>
        </c:if>
        <div class="factura-cab">
            <div>
                <span class="marca">
                    <svg><use href="#i-logo"/></svg>
                    <span class="marca-nome">Agro<em>Gest</em></span>
                </span>
                <p class="texto-suave pequeno" style="margin:.7rem 0 0">Nampula, Moçambique<br>NUIT 400 000 000 · geral@agro.mz</p>
            </div>
            <div class="factura-num">
                <div class="rotulo-doc">Factura</div>
                <div class="numero"><c:out value="${venda.numeroFactura}"/></div>
                <div class="pequeno">Data: ${ag:dataHora(venda.data)}</div>
            </div>
        </div>

        <div class="factura-partes">
            <div>
                <h3>Cliente</h3>
                <p><strong><c:out value="${venda.clienteNome}"/></strong></p>
                <p><c:out value="${venda.clienteEndereco}"/></p>
                <p>Tel.: <c:out value="${venda.clienteTelefone}"/></p>
                <p><c:out value="${venda.clienteEmail}"/></p>
            </div>
            <div>
                <h3>Pagamento</h3>
                <p>Moeda: Metical (MT)</p>
                <p>IVA: ${taxaIva}%</p>
            </div>
        </div>

        <div class="tabela-wrap">
            <table>
                <thead>
                <tr>
                    <th>#</th>
                    <th>Produto</th>
                    <th class="num">Quantidade</th>
                    <th class="num">Preço unit.</th>
                    <th class="num">Subtotal</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="i" items="${venda.itens}" varStatus="st">
                    <tr>
                        <td>${st.count}</td>
                        <td><c:out value="${i.produtoNome}"/></td>
                        <td class="num">${i.quantidade} <c:out value="${i.unidade}"/></td>
                        <td class="num">${ag:mt(i.precoUnit)}</td>
                        <td class="num">${ag:mt(i.subtotal)}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>

        <div class="resumo factura-totais" style="margin-top:1rem">
            <dl>
                <dt>Subtotal</dt><dd>${ag:mt(venda.subtotal)}</dd>
                <dt>IVA (${taxaIva}%)</dt><dd>${ag:mt(venda.iva)}</dd>
                <dt class="total">Total</dt><dd class="total">${ag:mt(venda.total)}</dd>
            </dl>
        </div>

        <p class="factura-rodape">Obrigado pela sua preferência. Documento processado pelo sistema AgroGest.</p>
    </article>
</div>

<%@ include file="/WEB-INF/views/_footer.jspf" %>
