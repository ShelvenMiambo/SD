<%@ page contentType="text/html;charset=UTF-8" %>
<c:set var="titulo" value="Painel"/>
<c:set var="menu" value="dashboard"/>
<%@ include file="/WEB-INF/views/_header.jspf" %>

<div class="cabecalho-pagina">
    <div>
        <div class="sobretitulo">Painel</div>
        <h1>Olá, <c:out value="${u.nome}"/></h1>
        <p>Facturação acumulada em ${nVendas} venda(s)</p>
        <div class="destaque-valor" style="margin-top:.5rem">${ag:mt(faturacao)}</div>
    </div>
    <div class="acoes">
        <a class="btn secundario" href="${ctx}/admin/relatorios"><svg class="ic"><use href="#i-grafico"/></svg> Relatórios</a>
        <a class="btn" href="${ctx}/admin/produtos"><svg class="ic"><use href="#i-mais"/></svg> Novo produto</a>
    </div>
</div>

<div class="indicadores">
    <a class="indicador" href="${ctx}/admin/fornecedores">
        <span class="icone"><svg class="ic"><use href="#i-camiao"/></svg>Fornecedores</span>
        <span class="valor">${nFornecedores}</span>
    </a>
    <a class="indicador" href="${ctx}/admin/produtos">
        <span class="icone"><svg class="ic"><use href="#i-pacote"/></svg>Produtos</span>
        <span class="valor">${nProdutos}</span>
    </a>
    <a class="indicador" href="${ctx}/admin/listagens?tipo=clientes">
        <span class="icone"><svg class="ic"><use href="#i-utilizadores"/></svg>Clientes</span>
        <span class="valor">${nClientes}</span>
    </a>
    <a class="indicador" href="${ctx}/admin/compras">
        <span class="icone"><svg class="ic"><use href="#i-recibo"/></svg>Vendas</span>
        <span class="valor">${nVendas}</span>
    </a>
    <a class="indicador" href="${ctx}/admin/campanhas">
        <span class="icone"><svg class="ic"><use href="#i-calendario"/></svg>Campanhas</span>
        <span class="valor">${nCampanhas}</span>
    </a>
</div>

<div class="grelha-2">
    <section class="cartao tabela-wrap">
        <div class="cabecalho-pagina" style="margin-bottom:.8rem">
            <h2 style="margin:0"><svg class="ic"><use href="#i-recibo"/></svg> Últimas vendas</h2>
            <a class="btn secundario pequeno" href="${ctx}/admin/compras">Ver todas <svg class="ic"><use href="#i-seta"/></svg></a>
        </div>
        <c:choose>
            <c:when test="${empty ultimasVendas}"><p class="vazio">Ainda não há vendas.</p></c:when>
            <c:otherwise>
                <table>
                    <thead><tr><th>Factura</th><th>Cliente</th><th>Data</th><th class="num">Total</th></tr></thead>
                    <tbody>
                    <c:forEach var="v" items="${ultimasVendas}">
                        <tr>
                            <td><a href="${ctx}/factura?id=${v.id}"><strong><c:out value="${v.numeroFactura}"/></strong></a><c:if test="${v.anulada}"> <span class="tag anulada">Anulada</span></c:if></td>
                            <td><c:out value="${v.clienteNome}"/></td>
                            <td class="texto-suave">${ag:dataHora(v.data)}</td>
                            <td class="num"><strong>${ag:mt(v.total)}</strong></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>
    </section>

    <aside class="cartao">
        <h2><svg class="ic"><use href="#i-aviso"/></svg> Stock baixo</h2>
        <p class="texto-suave pequeno">Produtos com ${limiteStock} unidades ou menos.</p>
        <c:choose>
            <c:when test="${empty stockBaixo}">
                <div class="alerta ok"><svg class="ic"><use href="#i-visto"/></svg><span>Todos os produtos têm stock suficiente.</span></div>
            </c:when>
            <c:otherwise>
                <table>
                    <tbody>
                    <c:forEach var="p" items="${stockBaixo}">
                        <tr>
                            <td><c:out value="${p.nome}"/></td>
                            <td class="num"><span class="tag alerta-stock">${p.quantidade} <c:out value="${p.unidade}"/></span></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
                <a class="btn secundario pequeno" style="margin-top:.9rem" href="${ctx}/admin/produtos">Repor stock</a>
            </c:otherwise>
        </c:choose>

        <h2 style="margin-top:1.6rem"><svg class="ic"><use href="#i-calendario"/></svg> Validade</h2>
        <p class="texto-suave pequeno">Produtos fora de validade ou a expirar nos próximos ${diasValidade} dias. Os expirados não podem ser vendidos.</p>
        <c:choose>
            <c:when test="${empty aExpirar}">
                <div class="alerta ok"><svg class="ic"><use href="#i-visto"/></svg><span>Nenhum produto a expirar.</span></div>
            </c:when>
            <c:otherwise>
                <table>
                    <tbody>
                    <c:forEach var="p" items="${aExpirar}">
                        <tr>
                            <td><c:out value="${p.nome}"/></td>
                            <td class="num"><span class="tag ${ag:expirado(p.validade) ? 'alerta-stock' : 'fechada'}">${ag:expirado(p.validade) ? 'expirou ' : ''}${ag:data(p.validade)}</span></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>
    </aside>
</div>

<%@ include file="/WEB-INF/views/_footer.jspf" %>
