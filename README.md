# Catálogo E-commerce

Mini catálogo de e-commerce com **Spring Boot** e **Cloud Firestore**, rodando localmente contra o **Firestore Emulator**.

## Stack

- Java 23+
- Spring Boot 4.1.2 (Web MVC + Validation)
- Maven (via wrapper `./mvnw`)
- Firebase Admin SDK 9.2.0 (Firestore)
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

## Endpoints

| Método | Rota                 | Descrição                                                        |
| ------ | -------------------- | ---------------------------------------------------------------- |
| GET    | `/`                  | Status básico da aplicação                                       |
| GET    | `/health/firestore`  | Escreve e lê um documento em `_health/ping` para testar o Firestore |

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

Se a conexão falhar, retorna `503 Service Unavailable` com `"conectado": false` e a mensagem de erro. Você também pode conferir o documento criado em http://127.0.0.1:4000/firestore.

## Rodar os testes

```bash
./mvnw test
```

O `maven-surefire-plugin` já define `FIRESTORE_EMULATOR_HOST=localhost:8080` para os testes, então não é preciso subir o emulador só para rodar o teste de contexto.

## Estrutura do projeto

```
src/main/java/br/edu/ifpb/catalogo_ecommerce/
├── CatalogoEcommerceApplication.java   # classe main
└── config/
    └── FirestoreConfig.java            # bean do Firestore (projectId demo-catalogo)

src/main/resources/application.properties
firebase.json                            # configuração dos emuladores
.firebaserc                              # projeto padrão (demo-catalogo)
```

## Observações

- Os dados do emulador são **em memória**: reiniciar o emulador limpa o banco. Para persistir, use `firebase emulators:export` / `--import`.
- O emulador está sem `firestore.rules`; por padrão ele libera todas as leituras e escritas. Isso é aceitável para desenvolvimento local, mas deve ser revisto antes de qualquer deploy.
- No **JDK 24+** aparece o aviso `WARNING: A restricted method in java.lang.System has been called` causado pelo gRPC/Netty. Não é erro. Para silenciá-lo, adicione às **VM options** da execução:
  ```
  --enable-native-access=ALL-UNNAMED
  ```
