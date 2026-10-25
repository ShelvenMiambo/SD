# AgroGest — Sistema de Gestão Agrícola

Aplicação web em **Java 17 + Jakarta Servlets 6 + JSP/JSTL**, publicada no **Apache Tomcat 10.1**,
para um agricultor da província de Nampula gerir fornecedores, produtos em stock, campanhas agrícolas,
cultivos e colheitas, e vender produtos a clientes com emissão de factura.

Trabalho prático de **Sistemas Distribuídos** (Engenharia Informática, ISCTEM, 2026).
Restrição do enunciado: **sem base de dados**. Os dados vivem em listas em memória e são serializados para ficheiro.

---

## 1. Credenciais de teste

| Perfil | Email | Palavra-passe |
|---|---|---|
| Administrador | `admin@agro.mz` | `admin234` |
| Cliente | `ana@teste.mz` | `1234` |
| Cliente | `carlos@teste.mz`, `fatima@teste.mz`, `pedro@teste.mz`, `rosa@teste.mz` | `1234` |

No primeiro arranque (sem ficheiro de dados) o sistema carrega dados de exemplo:
**3 fornecedores · 3 produtos · 5 clientes · 4 vendas · 1 campanha** (com 1 cultivo e 1 colheita).
As 4 vendas de exemplo são de 2025 (`FT-2025-1` a `FT-2025-4`), por isso a primeira compra feita na
demonstração recebe a factura **`FT-2026-1`**, como no exemplo do enunciado.

---

## 2. Requisitos

- Java 17 ou superior (JDK)
- Apache Tomcat 10.1
- Maven 3.9 — **não é preciso instalar**: o projecto inclui o Maven Wrapper (`mvnw` / `mvnw.cmd`)

---

## 3. Compilar e gerar o WAR

```bash
# Linux / macOS / Git Bash
./mvnw clean package

# Windows (PowerShell ou cmd)
mvnw.cmd clean package
```

Resultado: `target/agrogest.war`. O comando também corre os testes unitários (`src/test`).

---

## 4. Deploy

### 4.1 Linux com systemd (servidor de produção)

O script `deploy/instalar.sh` instala o Java 17, o Tomcat 10.1 em `/opt/tomcat`, configura a **porta 8081**,
publica o WAR e regista o serviço systemd.

```bash
./mvnw clean package
sudo bash deploy/instalar.sh target/agrogest.war
```

Aceder em **http://&lt;ip-do-servidor&gt;:8081/agrogest**

Gestão do serviço:

```bash
sudo systemctl status tomcat      # estado
sudo systemctl stop tomcat        # pára (o AgroGest grava os dados antes de terminar)
sudo systemctl start tomcat       # arranca (o AgroGest restaura os dados)
sudo systemctl restart tomcat
sudo journalctl -u tomcat -f      # logs em tempo real
```

O ficheiro da unidade está em [`deploy/tomcat.service`](deploy/tomcat.service).

<details>
<summary>Passos manuais (sem o script)</summary>

```bash
sudo apt install openjdk-17-jdk-headless
sudo useradd -r -m -U -d /opt/tomcat -s /bin/false tomcat
curl -O https://archive.apache.org/dist/tomcat/tomcat-10/v10.1.61/bin/apache-tomcat-10.1.61.tar.gz
sudo tar -xzf apache-tomcat-10.1.61.tar.gz -C /opt/tomcat --strip-components=1

# Porta 8081: em /opt/tomcat/conf/server.xml
#   <Connector port="8081" protocol="HTTP/1.1" ... />

sudo cp target/agrogest.war /opt/tomcat/webapps/
sudo mkdir -p /opt/tomcat/data
sudo chown -R tomcat:tomcat /opt/tomcat

sudo cp deploy/tomcat.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now tomcat
```
</details>

### 4.2 Windows (desenvolvimento)

1. Descarregar o Tomcat 10.1 (zip) de https://tomcat.apache.org e extrair.
2. Em `conf/server.xml`, mudar `port="8080"` para `port="8081"`.
3. Copiar `target/agrogest.war` para `webapps/`.
4. Definir `JAVA_HOME` e arrancar: `bin\startup.bat` (parar com `bin\shutdown.bat`).
5. Abrir http://localhost:8081/agrogest

---

## 5. Arquitectura

Padrão **MVC** em camadas:

```
 Browser ──HTTP :8081──▶ Apache Tomcat 10.1
                          │
                          ├─ filter/    AuthFilter ........ sessão + perfil (403 em /admin/*)
                          ├─ servlet/   Controladores ..... Login, Registo, Catálogo, Carrinho,
                          │                                 Compra, Factura, admin/* (CRUDs, Relatórios)
                          ├─ service/   Regras de negócio . VendaService (synchronized, transaccional)
                          ├─ repo/      DataStore ......... listas em memória (synchronizedList)
                          │             Persistencia ...... ficheiro .ser (escrita atómica)
                          ├─ model/     POJOs Serializable  Utilizador, Produto, Venda, ...
                          ├─ listener/  AppListener ....... restaura no arranque, grava no encerramento
                          ├─ util/      PasswordUtil, Formatos, Parametros, Flash
                          └─ WEB-INF/views/  JSP + JSTL + CSS (vista)
```

### Pacotes

| Pacote | Responsabilidade |
|---|---|
| `mz.agrogest.model` | POJOs do domínio (`Utilizador`, `Fornecedor`, `Produto`, `Campanha`, `Cultivo`, `Colheita`, `Venda`, `VendaItem`, `Carrinho`) |
| `mz.agrogest.repo` | `DataStore` (estado em memória, IDs por `AtomicInteger`) e `Persistencia` (serialização) |
| `mz.agrogest.service` | Regras de negócio e validações (`CatalogoService`, `AgricolaService`, `VendaService`, `RelatorioService`, `UtilizadorService`) |
| `mz.agrogest.servlet` | Controladores `@WebServlet` do cliente; `servlet.admin` para o administrador |
| `mz.agrogest.filter` | `AuthFilter` (`@WebFilter`) |
| `mz.agrogest.listener` | `AppListener` (`@WebListener`) |
| `mz.agrogest.util` | Hash de passwords, formatação (MT, datas), leitura de parâmetros, mensagens flash |

### Modelo de domínio

```
Utilizador 1 ── N Venda 1 ── N VendaItem N ── 1 Produto N ── 1 Fornecedor
Campanha   1 ── N Cultivo 1 ── N Colheita
```

### Rotas

| Rota | Perfil | Função |
|---|---|---|
| `/` | público | Página inicial (quem tem sessão vai para o catálogo ou para o painel) |
| `/login`, `/registo`, `/logout` | público | Autenticação e auto-registo |
| `/catalogo` | sessão | Catálogo com filtro por categoria e pesquisa (o admin vê só para consulta) |
| `/carrinho`, `/carrinho/{adicionar,atualizar,remover,esvaziar,ajustar}` | cliente | Carrinho guardado na sessão HTTP |
| `/compra` | cliente | Confirmação (GET) e finalização da compra (POST) |
| `/factura?id=` | sessão | Factura (cliente só vê as suas) |
| `/minhas-compras` | cliente | Histórico do cliente |
| `/perfil` | cliente | Alterar dados de facturação e palavra-passe |
| `/admin` | admin | Dashboard |
| `/admin/fornecedores`, `/admin/produtos` | admin | Gestão do catálogo (criar, editar, repor stock, remover) |
| `/admin/campanhas`, `/admin/cultivos`, `/admin/colheitas` | admin | Gestão agrícola |
| `/admin/listagens?tipo=&ordem=&dir=` | admin | Listagens ordenáveis (produtos, fornecedores, clientes, armazém, compras) |
| `/admin/compras` | admin | Compras detalhadas (paginadas) e anulação de vendas |
| `/admin/relatorios` | admin | Relatórios de clientes |
| `/admin/persistencia` | admin | Gravação e eliminação manual do ficheiro |

### Interface

Identidade visual própria, "Barro & Papel": terracota (o solo vermelho de Nampula) sobre papel quente,
títulos em Fraunces e números em Inter com algarismos tabulares, como num livro-caixa. O administrador
trabalha num layout de aplicação com barra lateral (gaveta no telemóvel); o cliente tem uma barra superior
simples e separadores em baixo no telemóvel. Há **modo escuro** (botão da lua, guardado no browser).

Tudo em CSS próprio (`css/style.css`), sem frameworks. As animações seguem regras simples: só `transform`
e `opacity`, curvas *ease-out* fortes, menos de 300 ms, feedback de pressão nos botões, efeitos de *hover*
só em dispositivos com rato e respeito por `prefers-reduced-motion`.
Os tipos de letra vêm do Google Fonts; sem Internet, o browser usa Georgia e o tipo de letra do sistema.

---

## 6. Regras de negócio

| Regra | Onde é garantida |
|---|---|
| **RN-a** Não vender mais do que o stock | `VendaService.adicionarAoCarrinho` / `atualizarCarrinho` e de novo em `registarVenda` |
| **RN-b** Venda atómica (tudo ou nada) | `VendaService.registarVenda`: dentro de `synchronized`, valida **todos** os itens primeiro e só depois desconta o stock |
| **RN-c** Cliente só vê as suas facturas | `VendaService.podeVer` + `FacturaServlet` (HTTP 403) |
| **RN-d** `/admin/*` só para o administrador | `AuthFilter` (HTTP 403) |
| **RN-e** Rotas do cliente exigem sessão | `AuthFilter` (redirecciona para `/login`) |
| **RN-f** IDs sequenciais por `AtomicInteger` | `DataStore.proximoId` (um contador por entidade) |
| Campanha com cultivos não pode ser removida | `AgricolaService.removerCampanha` |
| Cultivo com colheitas não pode ser removido | `AgricolaService.removerCultivo` |
| Fornecedor com produtos não pode ser removido | `CatalogoService.removerFornecedor` |
| Campanha fechada não aceita cultivos nem colheitas | `AgricolaService.criarCultivo` / `criarColheita` |
| Email único no registo | `UtilizadorService.criar` |
| Não se vendem produtos fora de validade | `VendaService` (ao adicionar e na compra) |
| Máximo de 10 000 unidades por linha; só números inteiros | `VendaService.exigirStock`, `Parametros.inteiro` |
| O administrador não compra (carrinho, compra e perfil são só do cliente) | `AuthFilter` + `VendaService.registarVenda` |
| Anular uma venda devolve as quantidades ao stock e tira-a da facturação | `VendaService.anularVenda` |
| Fornecedor só vende as categorias do seu tipo (SEMENTES → SEMENTE, …) | `TipoFornecedor` + `CatalogoService.validarProduto` |
| Nome de produto único por fornecedor; nome de fornecedor e de campanha únicos | `CatalogoService`, `AgricolaService` |
| Unidade, província e cultura escolhidas de listas fechadas | `Opcoes` (validado no servidor) |
| Validade não pode ser passada; preço ≤ 10 000 000 MT; stock ≤ 1 000 000 | `CatalogoService.validarProduto` |
| Telefone moçambicano (82–87 ou fixo 2x) guardado num formato único | `Validacao.telefone` |
| Nome de cliente com nome e apelido, só letras | `Validacao.nomePessoa` |
| Campanha: fim depois do início, no máximo 24 meses; aviso de sobreposição | `AgricolaService.validarCampanha` / `sobreposicoes` |
| Plantio dentro das datas da campanha | `AgricolaService.criarCultivo` |
| Colheita depois do plantio e até 6 meses após o fim da campanha | `AgricolaService.criarColheita` |
| Hectares, kg e preço/kg com máximos e no máximo 2 casas decimais | `Validacao.positivo` |

**Concorrência:** todas as escritas são feitas em `synchronized (DataStore.get())`, o mesmo trinco usado pela
persistência. O teste `comprasConcorrentesNuncaVendemMaisDoQueOStock` lança 20 compras em paralelo sobre um
produto com 10 unidades e verifica que só 10 têm sucesso e que o stock termina em 0.

**Formulários:** quando uma acção falha, a mensagem aparece no topo **e junto ao campo com erro**, que fica
assinalado, e os valores escritos são repostos. Os campos numéricos têm `max` igual ao stock disponível; os
botões de envio desactivam-se depois do primeiro clique (evita compras duplicadas).

**Factura:** número `FT-<ano>-<n>` com sequência própria por ano, IVA de 16% sobre o subtotal,
dados do cliente copiados no momento da compra e botão **Imprimir** (estilos `@media print`).

---

## 7. Persistência e tolerância a falhas

- Ficheiro: `${catalina.base}/data/agrogest.ser` (em systemd: `/opt/tomcat/data/agrogest.ser`).
  Pode ser alterado com `-Dagrogest.dados=/caminho/ficheiro.ser`.
- **Arranque:** `AppListener.contextInitialized` restaura o estado; se não houver ficheiro válido, carrega os dados de exemplo.
- **Encerramento:** `AppListener.contextDestroyed` grava o estado automaticamente.
- **Manual:** menu *Persistência* → *Gravar agora* / *Eliminar ficheiro*.
- **Escrita atómica:** grava em `agrogest.ser.tmp`, força a escrita no disco (`fsync`) e faz `Files.move(ATOMIC_MOVE)`.
  Uma falha a meio nunca corrompe o ficheiro principal.
- **Backup:** a versão anterior fica em `agrogest.ser.bak` e é usada se o ficheiro principal estiver ilegível.
- **Repor os dados de exemplo:** parar o Tomcat, apagar `agrogest.ser` e `agrogest.ser.bak`, arrancar de novo.

---

## 8. Segurança

- Palavras-passe guardadas com **PBKDF2-HMAC-SHA256** (65 536 iterações, sal aleatório). Nunca em texto puro.
- O ID da sessão muda no login (protecção contra fixação de sessão).
- **Token CSRF** em todos os formulários POST (`CsrfFilter`), além do cookie de sessão `HttpOnly` e `SameSite=Lax`.
- **Limite de tentativas de login:** 5 falhas seguidas bloqueiam o email durante 1 minuto (`TentativasLogin`).
- Todas as acções que alteram dados são `POST`. Os dados escritos pelos utilizadores são escapados nas JSP (`<c:out>`).
- As JSP ficam em `WEB-INF/views` e só são acessíveis através dos servlets.
- A foto do produto só aceita `http://`, `https://` ou um caminho da aplicação começado por `/`.
- Um cliente desativado pelo administrador perde a sessão no pedido seguinte.

---

## 9. Testes

```bash
./mvnw test
```

36 testes (`RegrasNegocioTest`, `ValidacaoTest` e `FormatosTest`) cobrem: compra com IVA, stock insuficiente,
venda atómica, compras concorrentes, produtos fora de validade, limite por linha, "ajustar ao stock", anulação de
vendas, numeração das facturas, visibilidade das facturas, validação de nome/telefone/província, alteração de
palavra-passe, bloqueio de login, categorias por tipo de fornecedor, nomes únicos, datas de plantio e colheita,
pesquisa sem acentos, paginação, relatórios e persistência (incluindo recuperação pelo `.bak`).

---

## 10. Decisões tomadas

- **Tipo de fornecedor `FERRAMENTAS`:** o enunciado lista os tipos SEMENTES, MAQUINARIA e FERTILIZANTES, mas os
  produtos têm a categoria FERRAMENTA. Foi acrescentado FERRAMENTAS para haver fornecedores dessa categoria.
- **"Clientes que compraram (ordenados por volume)":** volume = valor total gasto (com IVA).
- **Fotos dos produtos:** fotografias reais em `img/produtos/` (Wikimedia Commons, licenças livres; ver `img/CREDITOS.txt`).
  O campo aceita um caminho da aplicação (`/img/produtos/feijao-nhemba.jpg`) ou um URL `https://`. Vazio = fotografia da categoria, para a
  demonstração funcionar sem Internet.
- **Validade** é opcional (ferramentas e maquinaria não têm validade).

---

## 11. Problemas comuns

| Problema | Solução |
|---|---|
| `JAVA_HOME environment variable is not defined correctly` | Definir `JAVA_HOME` para a pasta do JDK (ex.: `C:\Program Files\Java\jdk-21`) |
| `PKIX path building failed` ao compilar (antivírus ou proxy a inspeccionar HTTPS) | No Windows: `set MAVEN_OPTS=-Djavax.net.ssl.trustStoreType=Windows-ROOT` antes do `mvnw` |
| Porta 8081 ocupada | Mudar a porta em `conf/server.xml` ou parar o processo que a usa |
| Acentos trocados | Confirmar que o Tomcat arranca com `-Dfile.encoding=UTF-8` (já incluído no `tomcat.service`) |
