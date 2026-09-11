# Mestre — BE-001

Base local mínima da API do Mestre. Nesta etapa, a única rota exposta é a saúde:

`GET http://127.0.0.1:8080/actuator/health`

## Pré-requisito

Java 25 LTS deve estar disponível no `PATH`. O projeto usa o Maven Wrapper fixado em Maven 3.9.16; não é necessário instalar Maven globalmente.

## Verificação

```sh
./mvnw -v
./mvnw verify
java -jar target/mestre-0.0.1-SNAPSHOT.jar
```

Em outro terminal, verifique a saúde:

```sh
curl --fail http://127.0.0.1:8080/actuator/health
```

O resultado esperado é HTTP `200`, com o campo `status` igual a `UP` e sem um campo `details`. O corpo pode incluir indicadores de grupos de saúde; não dependa de uma estrutura JSON exata. As rotas `/actuator`, `/actuator/info`, `/actuator/metrics` e `/actuator/env` devem responder `404`.

## Docker

Docker permite executar a aplicação com Java 25 sem alterar a versão de Java instalada no macOS. Para construir a imagem e iniciar o serviço, execute:

```sh
docker compose up --build
```

Em outro terminal, consulte a saúde:

```sh
curl -i http://127.0.0.1:8080/actuator/health
```

O serviço é publicado somente no loopback (`127.0.0.1`). Para pará-lo e remover os recursos criados pelo Compose:

```sh
docker compose down
```
