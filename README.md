# Catálogo E-commerce

Mini catálogo de e-commerce com **Spring Boot** e **Cloud Firestore**, rodando localmente contra o **Firestore Emulator**.

## Stack

- Java 23+
- Spring Boot 4.1.2 (Web MVC + Validation)
- Maven (via wrapper `./mvnw`)
- Firebase Admin SDK 9.2.0 (Firestore)
- Springdoc OpenAPI 3.2.0 (Swagger UI)
- Firebase Emulator Suite (Firestore + UI)

## Pré-requisitos

- **JDK 23 ou superior** (`java -version`)
- **Firebase CLI** (`npm install -g firebase-tools` ou equivalente)
- Nenhuma credencial do Google é necessária: o projeto usa o emulador local.

## Como rodar

### 1. Subir o emulador do Firestore

Na raiz do projeto:

```bash
firebase emulators:start
```

O projeto padrão já está definido em `.firebaserc` (`demo-catalogo`), então não é preciso passar `--project`.

Serviços disponíveis:

| Serviço            | Endereço                  |
| ------------------ | ------------------------- |
| Firestore (API)    | `127.0.0.1:8080`          |
| Emulator UI        | http://127.0.0.1:4000     |
| Emulator Hub       | `127.0.0.1:4400`          |

Deixe esse terminal aberto.

### 2. Rodar a aplicação

O host do emulador já vem configurado em `src/main/resources/application.properties`:

```properties
firestore.project-id=demo-catalogo
firestore.emulator-host=${FIRESTORE_EMULATOR_HOST:localhost:8080}
```

Ou seja: se a variável `FIRESTORE_EMULATOR_HOST` estiver definida, ela é usada; caso contrário,
vale `localhost:8080`. Não é preciso configurar nada no IntelliJ para rodar localmente.

#### No IntelliJ IDEA

Basta executar a classe `CatalogoEcommerceApplication` (ou a configuração criada automaticamente
ao executar o `main`).

#### No terminal

```bash
./mvnw spring-boot:run
```

> Para usar outro host/porta de emulador, defina `FIRESTORE_EMULATOR_HOST` (por exemplo,
> `FIRESTORE_EMULATOR_HOST=127.0.0.1:9090`) ou sobrescreva a propriedade
> `firestore.emulator-host`.

### 3. Acessar a aplicação

```
http://localhost:8081
```

A porta está definida em `src/main/resources/application.properties` (`server.port=8081`).

## Documentação da API (Swagger)

Com a aplicação rodando:

| Recurso        | URL                                            |
| -------------- | ---------------------------------------------- |
| Swagger UI     | http://localhost:8081/swagger-ui.html          |
| OpenAPI (JSON) | http://localhost:8081/v3/api-docs              |

A documentação é gerada automaticamente pelo **springdoc-openapi** a partir dos controllers
(`@RestController`) e das anotações `@Operation`/`@Tag`. A configuração de título e versão fica em
`src/main/java/br/edu/ifpb/catalogo_ecommerce/config/OpenApiConfig.java`.

## Endpoints

### Status / saúde

| Método | Rota                | Descrição                                                          |
| ------ | ------------------- | ------------------------------------------------------------------ |
| GET    | `/`                 | Status básico da aplicação                                         |
| GET    | `/health/firestore` | Escreve e lê um documento em `_health/ping` para testar o Firestore |

Exemplo:

```bash
curl http://localhost:8081/health/firestore
```

Resposta quando a aplicação está falando com o emulador (`200 OK`):

```json
{
  "emulatorHost": "localhost:8080",
  "projectId": "demo-catalogo",
  "conectado": true,
  "status": "ok",
  "documento": "_health/ping",
  "dados": { "ultimoPing": "2026-11-06T18:00:00.000Z" },
  "latenciaMs": 42
}
```

Se a conexão falhar, retorna `503 Service Unavailable` com `"conectado": false` e a mensagem de erro.

### CRUD REST

Produtos, usuários e carrinhos expõem `GET / POST / PUT / DELETE`.
Vendas e pagamentos não têm `PUT`: os campos `pago`, `pagamentoId`, `totalEmCentavos` e
`precoEmCentavos` são calculados pelo servidor.
Uma lista vazia retorna `200 OK` com `[]`.

As rotas são nomeadas por **ação + recurso**, por exemplo `GET /buscar/produtos/{id}`.
Isso deixa explícita a operação de cada endpoint:

| Ação        | Prefixo          | Exemplo                        |
| ----------- | ---------------- | ------------------------------ |
| Listar      | `/listar`        | `GET /listar/produtos`         |
| Buscar      | `/buscar`        | `GET /buscar/produtos/{id}`    |
| Criar       | `/criar`         | `POST /criar/produtos`         |
| Atualizar   | `/atualizar`     | `PUT /atualizar/produtos/{id}` |
| Deletar     | `/deletar`       | `DELETE /deletar/produtos/{id}`|

Nenhum model é usado diretamente como corpo de requisição ou resposta. Cada recurso tem
DTOs próprios em `dto/`, e as validações ficam nos requests:

| Recurso    | Request                                                       | Response             |
| ---------- | ------------------------------------------------------------- | -------------------- |
| Produto    | `ProdutoRequest`                                              | `ProdutoResponse`    |
| Usuário    | `UsuarioRequest` / `UsuarioUpdateRequest`                     | `UsuarioResponse`    |
| Carrinho   | `CarrinhoRequest` / `CarrinhoUpdateRequest`                   | `CarrinhoResponse`   |
| Venda      | `VendaRequest`                                                | `VendaResponse`      |
| Pagamento  | `PagamentoRequest`                                            | `PagamentoResponse`  |

Os requests de carrinho, venda e pagamento aceitam apenas as chaves necessárias. Campos
derivados como `nome`, `precoEmCentavos`, `subtotal`, `totalEmCentavos`, `pago` e
`pagamentoId` são ignorados se enviados pelo cliente.

### Produto

**Repor estoque**

```http
POST /repor-estoque/produtos/{id}
{ "quantidade": 5 }
```

Soma a quantidade informada ao estoque atual, em uma transação, e retorna o produto
atualizado. A quantidade deve ser maior que zero. Para definir um valor absoluto de
estoque, continue usando `PUT /atualizar/produtos/{id}`.

### Carrinho

**Criar**

```http
POST /criar/carrinhos
{
  "usuarioId": "id-do-usuario",
  "itens": [
    { "produtoId": "id-do-produto", "quantidade": 2 }
  ]
}
```

O campo `itens` é opcional: omiti-lo (ou enviar `[]`) cria um carrinho vazio, que pode
receber itens depois pelo endpoint de adição.

```http
POST /criar/carrinhos
{ "usuarioId": "id-do-usuario" }
```

O servidor valida o usuário e cada produto, exige produto ativo, quantidade positiva,
estoque suficiente e ausência de produto repetido. Depois monta os itens com `nome` e
`precoEmCentavos` copiados do produto e calcula `subtotal` e `totalEmCentavos`.

**Adicionar item**

```http
POST /adicionar-item/carrinhos/{id}
{ "produtoId": "id-do-produto", "quantidade": 1 }
```

Adiciona um item sem substituir os existentes. Se o produto já estiver no carrinho, a
quantidade é **somada** à existente e o preço/nome são atualizados a partir do produto.
A validação de produto ativo e de estoque considera a quantidade total resultante.

**Atualizar (substitui todos os itens)**

```http
PUT /atualizar/carrinhos/{id}
{
  "itens": [
    { "produtoId": "id-do-produto", "quantidade": 1 }
  ]
}
```

O `usuarioId` não pode ser alterado pelo `PUT`. O total é sempre recalculado pelo servidor.
Para adicionar sem sobrescrever, use `POST /adicionar-item/carrinhos/{id}`.

| Recurso     | Endpoints |
| ----------- | --------- |
| Produtos    | `GET /listar/produtos`, `GET /buscar/produtos/{id}`, `POST /criar/produtos`, `PUT /atualizar/produtos/{id}`, `DELETE /deletar/produtos/{id}`, `POST /repor-estoque/produtos/{id}` |
| Usuários    | `GET /listar/usuarios`, `GET /buscar/usuarios/{id}`, `POST /criar/usuarios`, `PUT /atualizar/usuarios/{id}`, `DELETE /deletar/usuarios/{id}` |
| Carrinhos   | `GET /listar/carrinhos`, `GET /buscar/carrinhos/{id}`, `POST /criar/carrinhos`, `PUT /atualizar/carrinhos/{id}`, `DELETE /deletar/carrinhos/{id}`, `POST /adicionar-item/carrinhos/{id}` |
| Vendas      | `GET /listar/vendas`, `GET /buscar/vendas/{id}`, `POST /criar/vendas`, `DELETE /deletar/vendas/{id}`, `POST /confirmar-pagamento/vendas/{vendaId}` |
| Pagamentos  | `GET /listar/pagamentos`, `GET /buscar/pagamentos/{id}`, `POST /criar/pagamentos`, `DELETE /deletar/pagamentos/{id}` |

### Fluxo de venda e pagamento

**1. Criar a venda a partir do carrinho**

```http
POST /criar/vendas
{ "carrinhoId": "id-do-carrinho" }
```

O servidor carrega o carrinho, copia os itens, calcula o total e grava a venda como pendente.
Campos como `totalEmCentavos`, `itens`, `pago` e `pagamentoId` são ignorados se enviados.

**2. Criar o pagamento a partir da venda**

```http
POST /criar/pagamentos
{ "vendaId": "id-da-venda", "metodoPagamento": "PIX" }
```

O valor é copiado do total da venda. Cada venda aceita apenas um pagamento e não pode estar paga.

**3. Confirmar o pagamento**

```http
POST /confirmar-pagamento/vendas/{vendaId}
```

O servidor localiza o pagamento vinculado à venda e atualiza venda e pagamento na mesma transação.
A operação é idempotente: confirmar uma venda já paga retorna `204` sem alterações.

Exemplo rápido de criação de produto:

```bash
curl -i -X POST http://localhost:8081/criar/produtos \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Teclado","precoEmCentavos":19990,"estoque":5,"categoriaId":"informatica"}'
```

### Códigos de resposta

| Código | Situação                                                              |
| ------ | --------------------------------------------------------------------- |
| `200`  | Consulta ou atualização com sucesso                                   |
| `201`  | Recurso criado (o corpo traz `{"id": "..."}`)                          |
| `204`  | Remoção ou confirmação de pagamento sem corpo                         |
| `400`  | Dados inválidos (validação, enum inexistente, JSON malformado)         |
| `404`  | Recurso não encontrado                                                |
| `409`  | E-mail já cadastrado, venda já paga ou venda que já possui pagamento  |
| `500`  | Falha de acesso ao Firestore                                          |

Os erros são centralizados no `GlobalExceptionHandler` e retornam um corpo padronizado:

```json
{
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "nome: nome é obrigatório",
  "timestamp": "2026-11-06T18:00:00Z"
}
```

### Senha

A senha é recebida por `UsuarioRequest`/`UsuarioUpdateRequest`, mas `UsuarioResponse`
expõe apenas `id` e `email`. No `PUT`, o campo `senha` é opcional: se omitido ou em branco,
a senha atual é mantida.

## Rodar os testes

```bash
./mvnw test
```

O `maven-surefire-plugin` já define `FIRESTORE_EMULATOR_HOST=localhost:8080` para os testes, então não é preciso subir o emulador só para rodar o teste de contexto.

## Testar as classes `Main` e `BateriaAutomatica`

Com o emulador rodando (`firebase emulators:start`), execute a classe `Main` pela IDE ou pelo terminal:

```bash
./mvnw compile exec:exec
```

Ela abre um menu interativo. A opção **7** executa a `BateriaAutomatica`, que cria, lê, atualiza e remove documentos em todas as coleções (`produtos`, `usuarios`, `carrinho`, `vendas`, `pagamentos` e `codigosVerificacao`).

> **Importante:** os registros criados pela bateria permanecem no emulador para inspeção. Se quiser limpar tudo ao final, descomente a chamada `executarLimpeza()` em `BateriaAutomatica.java` (linha próxima ao fim do método `executar`). A limpeza está desativada por padrão para não apagar dados durante a demonstração.

Também é possível consultar pelo REST do emulador:

```bash
curl "http://127.0.0.1:8080/v1/projects/demo-catalogo/databases/(default)/documents/produtos"
```

Lembrete: os dados do emulador ficam em memória e são perdidos quando ele é reiniciado.

## Estrutura do projeto

```
src/main/java/br/edu/ifpb/catalogo_ecommerce/
├── CatalogoEcommerceApplication.java   # classe main
├── controller/                         # endpoints REST + GlobalExceptionHandler
├── dto/                                # requests e responses da API
├── service/                            # regras de negócio
├── repository/                         # acesso ao Firestore
├── model/                              # documentos persistidos
└── config/
    ├── FirestoreConfig.java            # bean do Firestore (projectId demo-catalogo)
    └── OpenApiConfig.java              # metadados do Swagger/OpenAPI

src/main/resources/application.properties
firebase.json                            # configuração dos emuladores
.firebaserc                              # projeto padrão (demo-catalogo)
```

## Limitações (escopo do trabalho)

Este é um projeto acadêmico e a API foi entregue **sem autenticação/autorização**. Explicitamente fora do escopo:

- **Sem login / sessão / token.** Qualquer cliente acessa qualquer endpoint.
- **Sem controle de acesso por usuário.** Qualquer um pode ler, alterar ou remover qualquer carrinho; não há vínculo verificado entre `usuarioId` e o requisitante.
- **Sem perfil de administrador.** Qualquer um pode criar, alterar e remover produtos do catálogo.
- **Senhas em texto puro.** O campo não aparece nas respostas, mas não há hash (ex.: BCrypt) nem política de senha.
- **Sem paginação, filtros ou busca avançada.** Os `GET` de coleção retornam todos os documentos.
- **Sem integração real de pagamento.** O pagamento só registra um status; não há gateway, cartão ou conciliação.
- **Sem envio de e-mail.** A coleção `codigosVerificacao` existe, mas não há provedor SMTP configurado.

Essas decisões são intencionais para priorizar o CRUD REST, a validação e a demonstração dentro do prazo.

## Persistência do emulador

Por padrão o Firestore Emulator mantém os dados **em memória**. Para preservar os dados entre execuções, use as flags nativas do emulador:

```bash
# Exporta ao encerrar e reimporta na próxima inicialização
firebase emulators:start --import=./.firestore-data --export-on-exit
```

O diretório `.firestore-data/` já está no `.gitignore`.

## Observações

- O emulador está sem `firestore.rules`; por padrão ele libera todas as leituras e escritas. Isso é aceitável para desenvolvimento local, mas deve ser revisto antes de qualquer deploy.
- Os e-mails de `usuarios` são normalizados (minúsculos) e validados como únicos na aplicação (`UsuarioService`), já que o Firestore não possui restrição `UNIQUE`.
- A venda é criada somente com `carrinhoId`: itens e total vêm do carrinho, nunca do cliente.
- O pagamento é criado somente com `vendaId` e `metodoPagamento`: o valor vem do total da venda.
- A confirmação de pagamento (`VendaService.confirmarPagamento`) localiza o pagamento da venda e atualiza os dois documentos em uma única transação do Firestore.
- Cada carrinho aceita uma única venda; o id da venda é o id do carrinho, com criação transacional para evitar duplicatas concorrentes.
- O estoque é validado e baixado na mesma transação que cria a venda. Excluir uma venda remove seus pagamentos e devolve as unidades ao estoque; excluir um pagamento desvincula-o da venda.
- A criação de pagamentos é transacional e usa o id da venda como id determinístico do pagamento, impedindo pagamentos duplicados mesmo com requisições concorrentes.
- O menu `Main` usa os mesmos métodos de domínio para carrinho, venda e pagamento; vendas/pagamentos não podem ser editados diretamente por esse menu.
- No **JDK 24+** aparece o aviso `WARNING: A restricted method in java.lang.System has been called` causado pelo gRPC/Netty. Não é erro. Para silenciá-lo, adicione às **VM options** da execução:
  ```
  --enable-native-access=ALL-UNNAMED
  ```
