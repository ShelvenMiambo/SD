<%@ page contentType="text/html;charset=UTF-8" %>
<c:set var="titulo" value="Criar conta"/>
<c:set var="layout" value="auth"/>
<%@ include file="/WEB-INF/views/_header.jspf" %>

<section class="auth-pagina">
    <%@ include file="/WEB-INF/views/_auth-lado.jspf" %>
    <div class="auth-form">
        <div class="auth">
            <a class="marca" href="${ctx}/"><svg><use href="#i-logo"/></svg><span class="marca-nome">Agro<em>Gest</em></span></a>
            <h1>Criar conta</h1>
            <p class="sub">Registe-se para comprar sementes, fertilizantes e ferramentas.</p>

            <c:if test="${not empty erro}">
                <div class="alerta erro" role="alert"><svg class="ic"><use href="#i-aviso"/></svg><span><c:out value="${erro}"/></span></div>
            </c:if>

            <form method="post" action="${ctx}/registo" class="form">
                <label>Nome completo
                    <span class="campo-icone">
                        <svg class="ic"><use href="#i-utilizador"/></svg>
                        <input type="text" name="nome" value="<c:out value='${param.nome}'/>" required maxlength="100" autocomplete="name">
                    </span>
                </label>
                <label>Email
                    <span class="campo-icone">
                        <svg class="ic"><use href="#i-email"/></svg>
                        <input type="email" name="email" value="<c:out value='${param.email}'/>" placeholder="nome@exemplo.mz" required maxlength="120" autocomplete="email">
                    </span>
                </label>
                <div class="form-linha">
                    <label>Palavra-passe
                        <span class="campo-icone">
                            <svg class="ic"><use href="#i-cadeado"/></svg>
                            <input type="password" name="password" placeholder="Mín. 4" required minlength="4" autocomplete="new-password">
                        </span>
                    </label>
                    <label>Confirmar
                        <span class="campo-icone">
                            <svg class="ic"><use href="#i-cadeado"/></svg>
                            <input type="password" name="confirmacao" required minlength="4" autocomplete="new-password">
                        </span>
                    </label>
                </div>
                <label>Telefone
                    <span class="campo-icone">
                        <svg class="ic"><use href="#i-telefone"/></svg>
                        <input type="tel" name="telefone" value="<c:out value='${param.telefone}'/>" placeholder="+258 84 000 0000" required maxlength="30" autocomplete="tel">
                    </span>
                </label>
                <label>Endereço
                    <span class="campo-icone">
                        <svg class="ic"><use href="#i-local"/></svg>
                        <input type="text" name="endereco" value="<c:out value='${param.endereco}'/>" placeholder="Av. Eduardo Mondlane, Nampula" required maxlength="200" autocomplete="street-address">
                    </span>
                </label>
                <button type="submit" class="btn bloco grande">Criar conta</button>
            </form>

            <p class="rodape-auth">Já tem conta? <a href="${ctx}/login">Entrar</a></p>
            <a class="voltar" href="${ctx}/"><svg class="ic"><use href="#i-seta-esq"/></svg> Voltar ao início</a>
        </div>
    </div>
</section>

<%@ include file="/WEB-INF/views/_footer.jspf" %>
