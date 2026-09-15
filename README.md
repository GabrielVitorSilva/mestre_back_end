# Mestre — BE-001, BE-002 e BE-003

API local do Mestre com cadastro de aluno e saúde.

`POST http://127.0.0.1:8080/api/v1/users`

[Contrato, decisões, arquitetura, testes e evidências da BE-003](docs/BE-003.md). O cadastro aceita qualquer domínio de e-mail, cria somente aluno ativo e não exige confirmação por e-mail.

## Organização do código

O módulo `identity` segue a ordem **módulo de negócio → camada → funcionalidade**. Por exemplo, o cadastro HTTP está em `adapter/http/registration`, o caso de uso em `application/registration` e suas regras no domínio em `domain/registration`. Componentes reutilizáveis de conta e senha têm pastas próprias (`account` e `password`), para não parecerem exclusivos do cadastro.

O caminho de uma requisição é: `RegistrationController` recebe o JSON → `RegisterStudentUseCase` coordena a operação → `UserAccount` e `RegistrationRules` preservam as invariantes → `PasswordHasher` gera o hash → `JpaUserAccountRepository` persiste a entidade → `RegistrationResponse` devolve somente os campos públicos. A configuração conecta essas peças por injeção de construtor.

Saúde:

`GET http://127.0.0.1:8080/actuator/health`

## Pré-requisito

Docker Desktop (ou Docker Engine com Docker Compose) deve estar disponível. Não é necessário instalar Java ou Maven no macOS.

## Docker

Docker Compose inicia a aplicação e um PostgreSQL local. Para construir a imagem e iniciar os serviços, execute:

```sh
docker compose up --build -d
```

O PostgreSQL não publica nenhuma porta no host: ele só pode ser acessado pela rede interna do Compose. A aplicação aguarda o healthcheck do banco antes de iniciar e usa credenciais exclusivamente locais de desenvolvimento. O Flyway estabelece o schema `mestre` (V1) e a tabela `user_accounts` (V2).

Consulte os logs da aplicação e, em seguida, a saúde:

```sh
docker compose logs app
curl -i http://127.0.0.1:8080/actuator/health
```

O resultado esperado é HTTP `200`, com o campo `status` igual a `UP` e sem `details`. As rotas `/actuator`, `/actuator/info`, `/actuator/metrics` e `/actuator/env` continuam respondendo `404`.

Para encerrar os serviços e remover os contêineres e a rede do Compose:

```sh
docker compose down
```

## Executar pelo IntelliJ

Use Java 25 e inicie somente o banco com a configuração adicional para a IDE:

```sh
docker compose -f compose.yaml -f compose.intellij.yaml up -d --wait postgres
```

No seletor de execução do IntelliJ, escolha **Mestre IntelliJ** e clique em Run.
A configuração compartilhada em `.run/` conecta a aplicação ao PostgreSQL do
projeto em `127.0.0.1:5433`, com credenciais locais de desenvolvimento. A porta
5433 evita conflito com outros bancos que já utilizam a 5432. O Compose padrão
continua sem publicar a porta do banco.

Se preferir sua configuração de execução existente, defina estas variáveis em
**Run → Edit Configurations → Environment variables**:

```text
DATABASE_URL=jdbc:postgresql://127.0.0.1:5433/mestre
DATABASE_USERNAME=mestre
DATABASE_PASSWORD=mestre_local_password
```

Se a aplicação do Compose estiver em execução, pare-a com `docker compose stop app`
antes de executar pela IDE, para liberar a porta 8080. Para encerrar o banco:

```sh
docker compose -f compose.yaml -f compose.intellij.yaml down
```

## Testes de integração

Execute a suíte completa com Java 25 e Testcontainers usando o serviço temporário de desenvolvimento:

```sh
docker compose --profile test run --rm test
```

Esse serviço monta o projeto como diretório de trabalho e o socket Docker exclusivamente para que o Testcontainers inicie um PostgreSQL isolado. Ele não expõe portas, não depende do PostgreSQL do Compose e não usa credenciais reais. A imagem da aplicação executa somente o empacotamento durante o build; os testes continuam obrigatórios neste comando de verificação.

## Verificação local com Java 25

```sh
./mvnw -v
./mvnw clean verify
```

Docker deve estar disponível para os testes com PostgreSQL isolado via Testcontainers. A suíte não depende de subir o banco do Compose. Esse banco não publica porta no macOS; iniciar apenas `postgres` não permite conectar a aplicação executada no host sem configuração adicional.
