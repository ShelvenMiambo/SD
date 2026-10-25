<%@ page contentType="text/html;charset=UTF-8" %>
<c:set var="titulo" value="Persistência"/>
<c:set var="menu" value="persistencia"/>
<%@ include file="/WEB-INF/views/_header.jspf" %>

<div class="cabecalho-pagina">
    <div>
        <div class="sobretitulo">Administração</div>
        <h1>Persistência em ficheiro</h1>
        <p>O estado é gravado automaticamente quando o Tomcat encerra e restaurado no arranque.</p>
    </div>
</div>

<div class="grelha-2">
    <section class="cartao">
        <h2>Estado do ficheiro</h2>
        <div class="resumo">
            <dl>
                <dt>Ficheiro</dt><dd style="word-break:break-all"><code><c:out value="${ficheiro}"/></code></dd>
                <dt>Existe</dt><dd>${existe ? 'Sim' : 'Não'}</dd>
                <dt>Última gravação</dt><dd>${ag:dataHora(ultimaGravacao)}</dd>
                <dt>Tamanho</dt><dd>${existe ? tamanhoKb : 0} KB</dd>
            </dl>
        </div>
        <h3>Como funciona</h3>
        <ul class="pequeno texto-suave">
            <li>Serialização Java (<code>ObjectOutputStream</code>) de todas as listas em memória.</li>
            <li>Escrita atómica: grava num ficheiro <code>.tmp</code> e depois substitui o original.</li>
            <li>A versão anterior fica em <code>.bak</code> e é usada se o ficheiro principal estiver corrompido.</li>
        </ul>
    </section>

    <aside class="cartao">
        <h2>Ações</h2>
        <form method="post" action="${ctx}/admin/persistencia" class="form"><input type="hidden" name="_csrf" value="${csrf}">
            <input type="hidden" name="acao" value="gravar">
            <button type="submit" class="btn bloco grande">Gravar agora</button>
        </form>
        <form method="post" action="${ctx}/admin/persistencia" class="form" style="margin-top:1rem"
              onsubmit="return confirm('Eliminar o ficheiro gravado? Os dados em memória mantêm-se, mas serão gravados de novo ao encerrar o Tomcat.')"><input type="hidden" name="_csrf" value="${csrf}">
            <input type="hidden" name="acao" value="eliminar">
            <button type="submit" class="btn perigo bloco">Eliminar ficheiro</button>
        </form>
        <p class="pequeno texto-suave" style="margin-top:1rem">
            Para repor os dados de exemplo: pare o Tomcat, apague o ficheiro (e o <code>.bak</code>)
            e volte a arrancar.
        </p>
    </aside>
</div>

<%@ include file="/WEB-INF/views/_footer.jspf" %>
