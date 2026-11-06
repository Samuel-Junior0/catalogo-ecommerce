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

### 2. Configurar a variável de ambiente

O cliente Java do Firestore detecta o emulador pela variável:

```
FIRESTORE_EMULATOR_HOST=localhost:8080
```

#### No IntelliJ IDEA

1. **Run → Edit Configurations…**
2. Selecione a configuração `CatalogoEcommerceApplication` (criada automaticamente ao executar o `main`).
3. No campo **Environment variables**, adicione:
   ```
   FIRESTORE_EMULATOR_HOST=localhost:8080
   ```
4. **Apply → OK**.

> A configuração de execução não é versionada: cada pessoa cria a sua no IntelliJ. O que precisa ser igual para todos é apenas a variável `FIRESTORE_EMULATOR_HOST=localhost:8080`.

#### No terminal

```bash
export FIRESTORE_EMULATOR_HOST=localhost:8080
./mvnw spring-boot:run
```

### 3. Acessar a aplicação

```
http://localhost:8081
```

A porta está definida em `src/main/resources/application.properties` (`server.port=8081`).

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
