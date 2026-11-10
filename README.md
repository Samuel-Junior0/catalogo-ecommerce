# Catálogo E-commerce

Mini catálogo de e-commerce com **Spring Boot** e **Cloud Firestore**, rodando localmente contra o **Firestore Emulator**.

## Stack

- Java 23+
- Spring Boot 4.1.2 (Web MVC + Validation)
- Maven (wrapper `./mvnw`)
- Firebase Admin SDK (Firestore)
- Springdoc OpenAPI (Swagger UI)
- Firebase Emulator Suite

## Pré-requisitos

- JDK 23 ou superior
- Firebase CLI (`npm install -g firebase-tools`)

Não é necessária nenhuma credencial do Google: o projeto usa o emulador local.

## Como rodar

**1. Subir o emulador do Firestore** (deixe o terminal aberto):

```bash
firebase emulators:start
```

O projeto padrão (`demo-catalogo`) já está em `.firebaserc`. Serviços:

| Serviço     | Endereço              |
| ----------- | --------------------- |
| Firestore   | `127.0.0.1:8080`      |
| Emulator UI | http://127.0.0.1:4000 |

**2. Rodar a aplicação:**

```bash
./mvnw spring-boot:run
```

Ou execute `CatalogoEcommerceApplication` na IDE.

**3. Acessar:** http://localhost:8081

O host do emulador vem de `FIRESTORE_EMULATOR_HOST` (padrão `localhost:8080`). A porta da API está em `application.properties` (`server.port=8081`).

## Swagger

| Recurso        | URL                                   |
| -------------- | ------------------------------------- |
| Swagger UI     | http://localhost:8081/swagger-ui.html |
| OpenAPI (JSON) | http://localhost:8081/v3/api-docs     |

## Endpoints

As rotas seguem o padrão **ação + recurso** (`/listar`, `/buscar`, `/criar`, `/atualizar`, `/deletar`).

| Recurso    | Endpoints |
| ---------- | --------- |
| Status     | `GET /`, `GET /health/firestore` |
| Produtos   | `GET /listar/produtos`, `GET /buscar/produtos/{id}`, `POST /criar/produtos`, `PUT /atualizar/produtos/{id}`, `DELETE /deletar/produtos/{id}`, `POST /repor-estoque/produtos/{id}` |
| Usuários   | `GET /listar/usuarios`, `GET /buscar/usuarios/{id}`, `POST /criar/usuarios`, `PUT /atualizar/usuarios/{id}`, `DELETE /deletar/usuarios/{id}` |
| Carrinhos  | `GET /listar/carrinhos`, `GET /buscar/carrinhos/{id}`, `POST /criar/carrinhos`, `PUT /atualizar/carrinhos/{id}`, `DELETE /deletar/carrinhos/{id}`, `POST /adicionar-item/carrinhos/{id}` |
| Vendas     | `GET /listar/vendas`, `GET /buscar/vendas/{id}`, `POST /criar/vendas`, `DELETE /deletar/vendas/{id}`, `POST /confirmar-pagamento/vendas/{vendaId}` |
| Pagamentos | `GET /listar/pagamentos`, `GET /buscar/pagamentos/{id}`, `POST /criar/pagamentos`, `DELETE /deletar/pagamentos/{id}` |

### Exemplos

Criar produto:

```bash
curl -X POST http://localhost:8081/criar/produtos \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Teclado","precoEmCentavos":19990,"estoque":5,"categoriaId":"informatica"}'
```

Criar carrinho:

```http
POST /criar/carrinhos
{ "usuarioId": "id-do-usuario", "itens": [{ "produtoId": "id-do-produto", "quantidade": 2 }] }
```

Adicionar item (soma se o produto já existir):

```http
POST /adicionar-item/carrinhos/{id}
{ "produtoId": "id-do-produto", "quantidade": 1 }
```

Repor estoque:

```http
POST /repor-estoque/produtos/{id}
{ "quantidade": 5 }
```

### Fluxo de venda e pagamento

1. **Criar venda a partir do carrinho** — o servidor copia itens e calcula o total:
   `POST /criar/vendas` com `{ "carrinhoId": "..." }`
2. **Criar pagamento a partir da venda** — valor copiado do total da venda:
   `POST /criar/pagamentos` com `{ "vendaId": "...", "metodoPagamento": "PIX" }`
3. **Confirmar pagamento** — atualiza venda e pagamento na mesma transação (idempotente):
   `POST /confirmar-pagamento/vendas/{vendaId}`

### Códigos de resposta

| Código | Situação |
| ------ | -------- |
| `200`  | Consulta ou atualização com sucesso |
| `201`  | Recurso criado (corpo com `{"id": "..."}`) |
| `204`  | Remoção ou confirmação de pagamento sem corpo |
| `400`  | Dados inválidos |
| `404`  | Recurso não encontrado |
| `409`  | E-mail duplicado, venda paga ou venda já com pagamento |
| `500`  | Falha de acesso ao Firestore |

Erros retornam um corpo padronizado (`status`, `erro`, `mensagem`, `timestamp`).

## Testes

```bash
./mvnw test
```

O Surefire já aponta `FIRESTORE_EMULATOR_HOST` para `localhost:8080`.

## Menu interativo

Com o emulador rodando:

```bash
./mvnw compile exec:exec
```

Abre um menu no terminal (`Main`) para manipular os dados. A opção **7** executa a `BateriaAutomatica`, que cria, lê, atualiza e remove documentos em todas as coleções.

## Estrutura

```
src/main/java/br/edu/ifpb/catalogo_ecommerce/
├── CatalogoEcommerceApplication.java  # main
├── controller/                        # endpoints REST + GlobalExceptionHandler
├── dto/                               # requests e responses
├── service/                           # regras de negócio
├── repository/                        # acesso ao Firestore
├── model/                             # documentos persistidos
└── config/                            # Firestore e OpenAPI
```

## Limitações (projeto acadêmico)

- **Sem autenticação/autorização**: qualquer cliente acessa qualquer endpoint.
- **Senhas em texto puro** (não aparecem nas respostas, mas não há hash).
- **Sem paginação ou filtros**: os `GET` de coleção retornam tudo.
- **Sem integração real de pagamento**: apenas registro de status.
- O emulador não usa `firestore.rules`, então libera todas as leituras/escritas.

## Observações

- Dados do emulador ficam em memória e são perdidos ao reiniciar. Para persistir:
  ```bash
  firebase emulators:start --import=./.firestore-data --export-on-exit
  ```
- E-mails de usuários são normalizados e validados como únicos na aplicação.
- Itens, totais e status são sempre calculados pelo servidor; campos derivados enviados pelo cliente são ignorados.
- No JDK 24+ pode aparecer um aviso do gRPC/Netty. Para silenciá-lo, adicione às VM options:
  `--enable-native-access=ALL-UNNAMED`
