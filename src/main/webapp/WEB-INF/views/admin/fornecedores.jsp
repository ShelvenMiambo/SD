<%@ page contentType="text/html;charset=UTF-8" %>
<c:set var="titulo" value="Fornecedores"/>
<c:set var="menu" value="fornecedores"/>
<%@ include file="/WEB-INF/views/_header.jspf" %>
<c:set var="editar" value="${not empty fornecedor}"/>

<div class="cabecalho-pagina">
    <div>
        <div class="sobretitulo">Administração</div>
        <h1>Fornecedores</h1>
        <p>${fn:length(fornecedores)} fornecedor(es) registado(s). Fornecedores com produtos não podem ser removidos.</p>
    </div>
</div>

<div class="grelha-2">
    <section class="cartao tabela-wrap">
        <table>
            <thead>
            <tr><th>#</th><th>Nome</th><th>Tipo</th><th>Contacto</th><th>Província</th><th>Ações</th></tr>
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
                        <div class="acoes">
                            <a class="btn secundario pequeno" href="${ctx}/admin/fornecedores?editar=${f.id}"><svg class="ic"><use href="#i-lapis"/></svg> Editar</a>
                            <form method="post" action="${ctx}/admin/fornecedores" class="form-inline"
                                  onsubmit="return confirm('Remover este fornecedor?')"><input type="hidden" name="_csrf" value="${csrf}">
                                <input type="hidden" name="acao" value="remover">
                                <input type="hidden" name="id" value="${f.id}">
                                <button type="submit" class="btn perigo pequeno"><svg class="ic"><use href="#i-lixo"/></svg> Remover</button>
                            </form>
                        </div>
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
        <h2>${editar ? 'Editar fornecedor' : 'Novo fornecedor'}</h2>
        <form method="post" action="${ctx}/admin/fornecedores" class="form"><input type="hidden" name="_csrf" value="${csrf}">
            <input type="hidden" name="acao" value="${editar ? 'atualizar' : 'criar'}">
            <c:if test="${editar}"><input type="hidden" name="id" value="${fornecedor.id}"></c:if>
            <label>Nome
                <input type="text" name="nome" required minlength="3" maxlength="100" placeholder="Sementes do Sul, Lda" value="<c:out value='${fornecedor.nome}'/>">
            </label>
            <label>Tipo
                <select name="tipo" required>
                    <c:forEach var="t" items="${tipos}">
                        <option value="${t}" ${fornecedor.tipo == t ? 'selected' : ''}>${t}</option>
                    </c:forEach>
                </select>
                <span class="ajuda">Define as categorias de produto que o fornecedor pode vender.</span>
            </label>
            <label>Contacto (telefone)
                <input type="tel" name="contacto" required maxlength="30" placeholder="+258 84 000 0000" value="<c:out value='${fornecedor.contacto}'/>"
                       pattern="\+?[0-9 \(\)\-]{8,20}" title="Número moçambicano, ex.: +258 84 123 4567">
            </label>
            <label>Província
                <select name="provincia" required>
                    <option value="">Escolha…</option>
                    <c:forEach var="pv" items="${provincias}">
                        <option value="${pv}" ${fornecedor.provincia == pv ? 'selected' : ''}>${pv}</option>
                    </c:forEach>
                </select>
            </label>
            <div class="acoes">
                <button type="submit" class="btn"><svg class="ic"><use href="#i-${editar ? 'guardar' : 'mais'}"/></svg> ${editar ? 'Guardar alterações' : 'Criar fornecedor'}</button>
                <c:if test="${editar}"><a class="btn secundario" href="${ctx}/admin/fornecedores">Cancelar</a></c:if>
            </div>
        </form>
    </aside>
</div>

<%@ include file="/WEB-INF/views/_footer.jspf" %>
