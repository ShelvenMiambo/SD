<%@ tag pageEncoding="UTF-8" description="Cabeçalho de coluna que ordena a listagem" %>
<%@ attribute name="campo" required="true" %>
<%@ attribute name="rotulo" required="true" %>
<%@ attribute name="base" required="true" description="URL sem ordem/dir, terminado em ? ou &amp;" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ativo" value="${requestScope.ordem == campo}"/>
<c:set var="proxDir" value="${ativo and not requestScope.desc ? 'desc' : 'asc'}"/>
<a class="ordenar ${ativo ? 'ativo' : ''}" href="${pageContext.request.contextPath}${base}ordem=${campo}&amp;dir=${proxDir}">${rotulo}<c:if test="${ativo}">${requestScope.desc ? ' ▼' : ' ▲'}</c:if></a>
