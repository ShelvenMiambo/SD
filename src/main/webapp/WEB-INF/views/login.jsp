<%@ page contentType="text/html;charset=UTF-8" %>
<c:set var="titulo" value="Entrar"/>
<c:set var="layout" value="auth"/>
<%@ include file="/WEB-INF/views/_header.jspf" %>

<section class="auth-pagina">
    <%@ include file="/WEB-INF/views/_auth-lado.jspf" %>
    <div class="auth-form">
        <div class="auth">
            <a class="marca" href="${ctx}/"><svg><use href="#i-logo"/></svg><span class="marca-nome">Agro<em>Gest</em></span></a>
            <h1>Entrar</h1>
            <p class="sub">Aceda à sua conta para comprar ou gerir a campanha.</p>

            <%@ include file="/WEB-INF/views/_flash.jspf" %>
            <c:if test="${not empty erro}">
                <div class="alerta erro" role="alert"><svg class="ic"><use href="#i-aviso"/></svg><span><c:out value="${erro}"/></span></div>
            </c:if>

            <form method="post" action="${ctx}/login" class="form"><input type="hidden" name="_csrf" value="${csrf}">
                <label>Email
                    <span class="campo-icone">
                        <svg class="ic"><use href="#i-email"/></svg>
                        <input type="email" name="email" value="<c:out value='${email}'/>" placeholder="nome@exemplo.mz" required autofocus autocomplete="username">
                    </span>
                </label>
                <label>Palavra-passe
                    <span class="campo-icone">
                        <svg class="ic"><use href="#i-cadeado"/></svg>
                        <input type="password" name="password" required autocomplete="current-password">
                    </span>
                </label>
                <button type="submit" class="btn bloco grande">Entrar</button>
            </form>

            <p class="rodape-auth">Ainda não tem conta? <a href="${ctx}/registo">Criar conta</a></p>

            <div class="credenciais">
                <strong>Contas de demonstração</strong><br>
                Administrador: <code>admin@agro.mz</code> / <code>admin234</code><br>
                Cliente: <code>ana@teste.mz</code> / <code>1234</code>
            </div>

            <a class="voltar" href="${ctx}/"><svg class="ic"><use href="#i-seta-esq"/></svg> Voltar ao início</a>
        </div>
    </div>
</section>

<%@ include file="/WEB-INF/views/_footer.jspf" %>
