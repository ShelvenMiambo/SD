<%@ page contentType="text/html;charset=UTF-8" %>
<c:set var="titulo" value="Listagens"/>
<c:set var="menu" value="listagens"/>
<%@ include file="/WEB-INF/views/_header.jspf" %>
<c:set var="base" value="/admin/listagens?tipo=${tipo}&"/>

<div class="cabecalho-pagina">
    <div>
        <div class="sobretitulo">Administração</div>
        <h1>Listagens</h1>
        <p>${pagina.totalItens} registo(s). Clique num cabeçalho para ordenar.</p>
    </div>
</div>

<nav class="separadores">
    <a href="${ctx}/admin/listagens?tipo=produtos" class="${tipo == 'produtos' ? 'ativo' : ''}">Produtos</a>
    <a href="${ctx}/admin/listagens?tipo=fornecedores" class="${tipo == 'fornecedores' ? 'ativo' : ''}">Fornecedores</a>
    <a href="${ctx}/admin/listagens?tipo=clientes" class="${tipo == 'clientes' ? 'ativo' : ''}">Clientes</a>
    <a href="${ctx}/admin/listagens?tipo=armazem" class="${tipo == 'armazem' ? 'ativo' : ''}">Armazém (stock &gt; 0)</a>
    <a href="${ctx}/admin/listagens?tipo=compras" class="${tipo == 'compras' ? 'ativo' : ''}">Compras</a>
</nav>

<section class="cartao tabela-wrap">
    <c:choose>
        <c:when test="${tipo == 'produtos' or tipo == 'armazem'}">
            <table>
                <thead><tr>
                    <th><t:ordenar campo="nome" rotulo="Nome" base="${base}"/></th>
                    <th><t:ordenar campo="categoria" rotulo="Categoria" base="${base}"/></th>
                    <th>Fornecedor</th>
                    <th class="num"><t:ordenar campo="preco" rotulo="Preço" base="${base}"/></th>
                    <th class="num"><t:ordenar campo="stock" rotulo="Stock" base="${base}"/></th>
                    <th><t:ordenar campo="data" rotulo="Validade" base="${base}"/></th>
                </tr></thead>
                <tbody>
                <c:forEach var="p" items="${lista}">
                    <tr>
                        <td><c:out value="${p.nome}"/></td>
                        <td><span class="tag ${p.categoria}">${p.categoria}</span></td>
                        <td><c:out value="${fornecedoresPorId[p.fornecedorId].nome}"/></td>
                        <td class="num">${ag:mt(p.preco)}</td>
                        <td class="num">${p.quantidade} <c:out value="${p.unidade}"/></td>
                        <td>${ag:data(p.validade)}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:when>

        <c:when test="${tipo == 'fornecedores'}">
            <table>
                <thead><tr>
                    <th><t:ordenar campo="nome" rotulo="Nome" base="${base}"/></th>
                    <th><t:ordenar campo="tipo" rotulo="Tipo" base="${base}"/></th>
                    <th>Contacto</th>
                    <th><t:ordenar campo="provincia" rotulo="Província" base="${base}"/></th>
                </tr></thead>
                <tbody>
                <c:forEach var="f" items="${lista}">
                    <tr>
                        <td><c:out value="${f.nome}"/></td>
                        <td><span class="tag ${f.tipo}">${f.tipo}</span></td>
                        <td><c:out value="${f.contacto}"/></td>
                        <td><c:out value="${f.provincia}"/></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:when>

        <c:when test="${tipo == 'clientes'}">
            <table>
                <thead><tr>
                    <th><t:ordenar campo="nome" rotulo="Nome" base="${base}"/></th>
                    <th><t:ordenar campo="email" rotulo="Email" base="${base}"/></th>
                    <th>Telefone</th>
                    <th>Endereço</th>
                    <th><t:ordenar campo="data" rotulo="Registo" base="${base}"/></th>
                    <th>Estado</th>
                    <th></th>
                </tr></thead>
                <tbody>
                <c:forEach var="cl" items="${lista}">
                    <tr>
                        <td><c:out value="${cl.nome}"/></td>
                        <td><c:out value="${cl.email}"/></td>
                        <td><c:out value="${cl.telefone}"/></td>
                        <td><c:out value="${cl.endereco}"/></td>
                        <td>${ag:dataHora(cl.dataRegisto)}</td>
                        <td><span class="tag ${cl.ativo ? 'ativo' : 'inativo'}">${cl.ativo ? 'Ativo' : 'Inativo'}</span></td>
                        <td>
                            <form method="post" action="${ctx}/admin/listagens" class="form-inline"><input type="hidden" name="_csrf" value="${csrf}">
                                <input type="hidden" name="id" value="${cl.id}">
                                <button type="submit" class="btn ${cl.ativo ? 'perigo' : 'secundario'} pequeno">${cl.ativo ? 'Desativar' : 'Ativar'}</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:when>

        <c:otherwise>
            <table>
                <thead><tr>
                    <th>Factura</th>
                    <th><t:ordenar campo="data" rotulo="Data" base="${base}"/></th>
                    <th><t:ordenar campo="nome" rotulo="Cliente" base="${base}"/></th>
                    <th class="num">Itens</th>
                    <th class="num"><t:ordenar campo="valor" rotulo="Total" base="${base}"/></th>
                    <th></th>
                </tr></thead>
                <tbody>
                <c:forEach var="v" items="${lista}">
                    <tr>
                        <td><c:out value="${v.numeroFactura}"/><c:if test="${v.anulada}"> <span class="tag anulada">Anulada</span></c:if></td>
                        <td>${ag:dataHora(v.data)}</td>
                        <td><c:out value="${v.clienteNome}"/></td>
                        <td class="num">${fn:length(v.itens)}</td>
                        <td class="num">${ag:mt(v.total)}</td>
                        <td><a class="btn secundario pequeno" href="${ctx}/factura?id=${v.id}">Factura</a></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
            <p><a href="${ctx}/admin/compras">Ver compras detalhadas →</a></p>
        </c:otherwise>
    </c:choose>
    <c:if test="${empty lista}"><p class="vazio">Sem registos.</p></c:if>
    <t:paginacao pagina="${pagina}" base="${base}ordem=${ordem}&amp;dir=${desc ? 'desc' : 'asc'}&amp;"/>
</section>

<%@ include file="/WEB-INF/views/_footer.jspf" %>
