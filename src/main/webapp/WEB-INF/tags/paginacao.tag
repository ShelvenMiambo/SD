<%@ tag pageEncoding="UTF-8" description="Navegação entre páginas de uma listagem" %>
<%@ attribute name="pagina" required="true" type="mz.agrogest.util.Pagina" %>
<%@ attribute name="base" required="true" description="URL sem o parâmetro pagina, terminado em ? ou &amp;" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${pagina.totalPaginas > 1}">
    <nav class="paginacao" aria-label="Páginas">
        <c:choose>
            <c:when test="${pagina.temAnterior}"><a class="btn secundario pequeno" href="${pageContext.request.contextPath}${base}pagina=${pagina.numero - 1}">Anterior</a></c:when>
            <c:otherwise><span class="btn secundario pequeno" aria-disabled="true" style="opacity:.45">Anterior</span></c:otherwise>
        </c:choose>
        <span class="pequeno texto-suave">Página ${pagina.numero} de ${pagina.totalPaginas} · ${pagina.totalItens} registo(s)</span>
        <c:choose>
            <c:when test="${pagina.temSeguinte}"><a class="btn secundario pequeno" href="${pageContext.request.contextPath}${base}pagina=${pagina.numero + 1}">Seguinte</a></c:when>
            <c:otherwise><span class="btn secundario pequeno" aria-disabled="true" style="opacity:.45">Seguinte</span></c:otherwise>
        </c:choose>
    </nav>
</c:if>
