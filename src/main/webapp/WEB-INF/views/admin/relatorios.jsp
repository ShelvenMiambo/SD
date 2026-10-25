<%@ page contentType="text/html;charset=UTF-8" %>
<c:set var="titulo" value="Relatórios"/>
<c:set var="menu" value="relatorios"/>
<%@ include file="/WEB-INF/views/_header.jspf" %>

<div class="cabecalho-pagina">
    <div>
        <div class="sobretitulo">Administração</div>
        <h1>Relatórios de clientes</h1>
        <p>Clientes, volume de compras e assiduidade</p>
    </div>
    <button type="button" class="btn secundario nao-imprimir" onclick="window.print()">Imprimir</button>
</div>

<section class="cartao tabela-wrap">
    <h2>Clientes que compraram (por volume)</h2>
    <table>
        <thead><tr><th>#</th><th>Cliente</th><th>Email</th><th class="num">Compras</th><th class="num">Total gasto</th><th>Última compra</th></tr></thead>
        <tbody>
        <c:forEach var="r" items="${compraram}" varStatus="st">
            <tr>
                <td>${st.count}</td>
                <td><c:out value="${r.cliente.nome}"/></td>
                <td><c:out value="${r.cliente.email}"/></td>
                <td class="num">${r.numeroCompras}</td>
                <td class="num"><strong>${ag:mt(r.totalGasto)}</strong></td>
                <td>${ag:dataHora(r.ultimaCompra)}</td>
            </tr>
        </c:forEach>
        <c:if test="${empty compraram}"><tr><td colspan="6" class="vazio">Nenhum cliente comprou ainda.</td></tr></c:if>
        </tbody>
    </table>
</section>

<div class="grelha-2">
    <section class="cartao tabela-wrap">
        <h2>Clientes mais assíduos</h2>
        <table>
            <thead><tr><th>Posição</th><th>Cliente</th><th class="num">N.º compras</th><th class="num">Total gasto</th></tr></thead>
            <tbody>
            <c:forEach var="r" items="${assiduos}" varStatus="st">
                <tr>
                    <td>${st.count}.º</td>
                    <td><c:out value="${r.cliente.nome}"/></td>
                    <td class="num"><strong>${r.numeroCompras}</strong></td>
                    <td class="num">${ag:mt(r.totalGasto)}</td>
                </tr>
            </c:forEach>
            <c:if test="${empty assiduos}"><tr><td colspan="4" class="vazio">Sem dados.</td></tr></c:if>
            </tbody>
        </table>
    </section>

    <section class="cartao tabela-wrap">
        <h2>Clientes sem compras</h2>
        <table>
            <thead><tr><th>Cliente</th><th>Contacto</th></tr></thead>
            <tbody>
            <c:forEach var="cl" items="${semCompras}">
                <tr>
                    <td><c:out value="${cl.nome}"/></td>
                    <td class="pequeno"><c:out value="${cl.email}"/><br><c:out value="${cl.telefone}"/></td>
                </tr>
            </c:forEach>
            <c:if test="${empty semCompras}"><tr><td colspan="2" class="vazio">Todos os clientes já compraram.</td></tr></c:if>
            </tbody>
        </table>
    </section>
</div>

<section class="cartao tabela-wrap">
    <h2>Clientes ordenados por nome</h2>
    <table>
        <thead><tr><th>Nome</th><th>Email</th><th>Telefone</th><th>Endereço</th><th>Estado</th></tr></thead>
        <tbody>
        <c:forEach var="cl" items="${porNome}">
            <tr>
                <td><c:out value="${cl.nome}"/></td>
                <td><c:out value="${cl.email}"/></td>
                <td><c:out value="${cl.telefone}"/></td>
                <td><c:out value="${cl.endereco}"/></td>
                <td><span class="tag ${cl.ativo ? 'ativo' : 'inativo'}">${cl.ativo ? 'Ativo' : 'Inativo'}</span></td>
            </tr>
        </c:forEach>
        <c:if test="${empty porNome}"><tr><td colspan="5" class="vazio">Sem clientes.</td></tr></c:if>
        </tbody>
    </table>
</section>

<%@ include file="/WEB-INF/views/_footer.jspf" %>
