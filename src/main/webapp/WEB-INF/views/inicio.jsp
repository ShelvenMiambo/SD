<%@ page contentType="text/html;charset=UTF-8" %>
<c:set var="titulo" value="Gestão agrícola em Nampula"/>
<c:set var="layout" value="inicio"/>
<%@ include file="/WEB-INF/views/_header.jspf" %>

<section class="inicio-hero">
    <div class="container">
        <div>
            <div class="sobretitulo"><svg class="ic"><use href="#i-local"/></svg> Gestão agrícola · Nampula</div>
            <h1>A sua campanha, do armazém <em>à factura.</em></h1>
            <p class="lead">
                Controle o stock de sementes, fertilizantes e ferramentas, registe campanhas,
                cultivos e colheitas, e venda aos seus clientes com factura emitida no momento.
            </p>
            <div class="acoes">
                <a class="btn grande" href="${ctx}/registo">Criar conta <svg class="ic"><use href="#i-seta"/></svg></a>
                <a class="btn grande secundario" href="${ctx}/login">Entrar</a>
            </div>
            <div class="nota">
                <span><svg class="ic"><use href="#i-visto"/></svg>IVA de 16% calculado</span>
                <span><svg class="ic"><use href="#i-visto"/></svg>Stock sempre certo</span>
                <span><svg class="ic"><use href="#i-visto"/></svg>Pronto a imprimir</span>
            </div>
        </div>

        <div class="demo-factura" aria-hidden="true">
            <div class="papel">
                <div class="papel-topo">
                    <span class="marca"><svg style="width:24px;height:24px"><use href="#i-logo"/></svg><span class="marca-nome">Agro<em>Gest</em></span></span>
                    <span class="papel-numero">Factura<strong>FT-2026-1</strong></span>
                </div>
                <div class="cascata">
                    <div class="papel-linha"><span>Semente de milho</span><span>3 kg</span><span>255,00 MT</span></div>
                    <div class="papel-linha"><span>Adubo NPK 12-24-12</span><span>1 saco</span><span>2.350,00 MT</span></div>
                    <div class="papel-linha"><span>Enxada de cabo longo</span><span>2 un.</span><span>900,00 MT</span></div>
                    <div class="papel-totais">
                        <span>Subtotal</span><span>3.505,00 MT</span>
                        <span>IVA 16%</span><span>560,80 MT</span>
                        <span class="t">Total</span><span class="t">4.065,80 MT</span>
                    </div>
                </div>
                <span class="papel-carimbo">Pago</span>
            </div>
        </div>
    </div>
</section>

<section class="secao" id="como-funciona">
    <div class="container">
        <div class="secao-titulo">
            <div class="sobretitulo">Como funciona</div>
            <h2>Quatro passos, um só registo.</h2>
            <p>Cada movimento fica ligado ao seguinte: o que entra em armazém é o que se vende, e o que se vende sai do stock.</p>
        </div>
        <div class="etapas">
            <div class="etapa">
                <span class="n">01</span>
                <h3>Fornecedores e stock</h3>
                <p>Registe fornecedores e produtos com preço, unidade, validade e quantidade em armazém.</p>
            </div>
            <div class="etapa">
                <span class="n">02</span>
                <h3>Campanhas</h3>
                <p>Abra a campanha, registe os cultivos por hectare e, no fim, as colheitas com o valor obtido.</p>
            </div>
            <div class="etapa">
                <span class="n">03</span>
                <h3>Vendas</h3>
                <p>O cliente escolhe no catálogo, confirma o carrinho e o stock é descontado de uma só vez.</p>
            </div>
            <div class="etapa">
                <span class="n">04</span>
                <h3>Factura e relatórios</h3>
                <p>Factura numerada com IVA, histórico de compras e relatórios de clientes por volume e assiduidade.</p>
            </div>
        </div>
    </div>
</section>

<section class="faixa">
    <div class="container">
        <div>
            <h2>Pronto para a <em>próxima campanha?</em></h2>
            <p>Crie a sua conta e veja o catálogo de insumos.</p>
        </div>
        <div class="numeros">
            <div><strong>${nProdutos}</strong><span>produtos</span></div>
            <div><strong>${nFornecedores}</strong><span>fornecedores</span></div>
            <div><strong>${nClientes}</strong><span>clientes</span></div>
        </div>
        <a class="btn grande" href="${ctx}/registo">Criar conta <svg class="ic"><use href="#i-seta"/></svg></a>
    </div>
</section>

<%@ include file="/WEB-INF/views/_footer.jspf" %>
