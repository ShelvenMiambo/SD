<%@ page contentType="text/html;charset=UTF-8" %>
<c:set var="titulo" value="Fornecedores"/>
<c:set var="menu" value="fornecedores"/>
<%@ include file="/WEB-INF/views/_header.jspf" %>

<div class="cabecalho-pagina">
    <div>
        <div class="sobretitulo">Administração</div>
        <h1>Fornecedores</h1>
        <p>${fn:length(fornecedores)} fornecedor(es) registado(s)</p>
    </div>
</div>

<div class="grelha-2">
    <section class="cartao tabela-wrap">
        <table>
            <thead>
            <tr><th>#</th><th>Nome</th><th>Tipo</th><th>Contacto</th><th>Província</th><th></th></tr>
            </thead>
            <tbody>
            <c:forEach var="f" items="${fornecedores}">
                <tr>
                    <td>${f.id}</td>
                    <td><strong><c:out value="${f.nome}"/></strong></td>
                    <td><span class="tag ${f.tipo}">${f.tipo}</span></td>
                    <td><c:out value="${f.contacto}"/></td>
                    <td><c:out value="${f.provincia}"/></td>
                    <td>
                        <form method="post" action="${ctx}/admin/fornecedores" class="form-inline"
                              onsubmit="return confirm('Remover este fornecedor?')">
                            <input type="hidden" name="acao" value="remover">
                            <input type="hidden" name="id" value="${f.id}">
                            <button type="submit" class="btn perigo pequeno"><svg class="ic"><use href="#i-lixo"/></svg> Remover</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty fornecedores}">
                <tr><td colspan="6" class="vazio">Sem fornecedores.</td></tr>
            </c:if>
            </tbody>
        </table>
    </section>

    <aside class="cartao">
        <h2>Novo fornecedor</h2>
        <form method="post" action="${ctx}/admin/fornecedores" class="form">
            <input type="hidden" name="acao" value="criar">
            <label>Nome <input type="text" name="nome" required maxlength="100" placeholder="Sementes do Sul, Lda"></label>
            <label>Tipo
                <select name="tipo" required>
                    <c:forEach var="t" items="${tipos}"><option value="${t}">${t}</option></c:forEach>
                </select>
            </label>
            <label>Contacto <input type="text" name="contacto" required maxlength="100" placeholder="+258 84 000 0000"></label>
            <label>Província <input type="text" name="provincia" required maxlength="60" placeholder="Maputo"></label>
            <button type="submit" class="btn"><svg class="ic"><use href="#i-mais"/></svg> Criar fornecedor</button>
        </form>
    </aside>
</div>

<%@ include file="/WEB-INF/views/_footer.jspf" %>
