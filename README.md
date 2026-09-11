# Mestre — BE-001 e BE-002

Base local mínima da API do Mestre. Nesta etapa, a única rota exposta é a saúde:

`GET http://127.0.0.1:8080/actuator/health`

## Pré-requisito

Docker Desktop (ou Docker Engine com Docker Compose) deve estar disponível. Não é necessário instalar Java ou Maven no macOS.

## Docker

Docker Compose inicia a aplicação e um PostgreSQL local. Para construir a imagem e iniciar os serviços, execute:

```sh
docker compose up --build -d
```

O PostgreSQL não publica nenhuma porta no host: ele só pode ser acessado pela rede interna do Compose. A aplicação aguarda o healthcheck do banco antes de iniciar e usa credenciais exclusivamente locais de desenvolvimento. O Flyway estabelece o schema técnico `mestre`; esta etapa não cria tabelas de negócio.

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

## Testes de integração

Execute a suíte completa com Java 25 e Testcontainers usando o serviço temporário de desenvolvimento:

```sh
docker compose --profile test run --rm test
```

Esse serviço monta o projeto como diretório de trabalho e o socket Docker exclusivamente para que o Testcontainers inicie um PostgreSQL isolado. Ele não expõe portas, não depende do PostgreSQL do Compose e não usa credenciais reais. A imagem da aplicação executa somente o empacotamento durante o build; os testes continuam obrigatórios neste comando de verificação.
