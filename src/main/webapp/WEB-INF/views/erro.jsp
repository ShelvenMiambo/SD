<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<c:set var="codigo" value="${requestScope['jakarta.servlet.error.status_code']}"/>
<c:set var="titulo" value="Erro ${codigo}"/>
<c:set var="layout" value="auth"/>
<%@ include file="/WEB-INF/views/_header.jspf" %>

<section class="auth-pagina">
    <%@ include file="/WEB-INF/views/_auth-lado.jspf" %>
    <div class="auth-form">
        <div class="auth">
            <a class="marca" href="${ctx}/"><svg><use href="#i-logo"/></svg><span class="marca-nome">Agro<em>Gest</em></span></a>
            <div class="destaque-valor" style="color:hsl(var(--barro));margin-bottom:1rem">${empty codigo ? 500 : codigo}</div>
            <c:choose>
                <c:when test="${codigo == 403}">
                    <h1>Acesso negado</h1>
                    <p class="sub">Não tem permissão para aceder a esta página.</p>
                </c:when>
                <c:when test="${codigo == 404}">
                    <h1>Página não encontrada</h1>
                    <p class="sub">O endereço pedido não existe.</p>
                </c:when>
                <c:otherwise>
                    <h1>Ocorreu um erro</h1>
                    <p class="sub">Algo correu mal ao processar o pedido. Tente novamente.</p>
                </c:otherwise>
            </c:choose>
            <a class="btn grande" href="${ctx}/"><svg class="ic"><use href="#i-casa"/></svg> Voltar ao início</a>
        </div>
    </div>
</section>

<%@ include file="/WEB-INF/views/_footer.jspf" %>
