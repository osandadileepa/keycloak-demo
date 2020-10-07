# Getting Started

## Development Enviroment setup

Make sure following are met

- JDK 1.8
- Docker compose
- VS Code or Spring tool suite 4.0

## Build developmet

In the project root directory

```bash
./mvnw clean install
```

To run the application with the dev profile

```bash
java -jar -Dspring.profiles.active=dev target/keycloak-demo-0.0.1-SNAPSHOT.jar

```

API is available at http://localhost:8080/api

## Keycloak Export

- Use the following command to get the Keycloak configs to single json file and it can be located at /tmp

```bash
docker run --rm \
 --net=keycloak-demo_demo-network \
 --name keycloak_exporter \
 -v /tmp:/tmp/keycloak-export \
 -e DB_PASSWORD="demo31234" \
 -e DB_DATABASE="keycloak" \
 -e DB_ADDR="postgresql" \
 -e DB_USER="demo" \
 -e DB_VENDOR="POSTGRES" \
 -e JDBC_PARAMS="characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true" \
 jboss/keycloak:10.0.0 \
 -Dkeycloak.migration.action=export \
 -Dkeycloak.migration.provider=singleFile \ -Dkeycloak.migration.file=/tmp/keycloak-export/demo.json
```

## Keycloak Import

- Copy the demo.json file to /tmp then run the following to import

```bash
docker run --rm \
    --net=keycloak-demo_demo-network \
    --name keycloak_exporter \
    -v /tmp:/tmp/keycloak-export \
    -e DB_PASSWORD="demo31234" \
    -e DB_DATABASE="keycloak" \
    -e DB_ADDR="postgresql" \
    -e DB_USER="demo" \
    -e DB_VENDOR="POSTGRES" \
    -e JDBC_PARAMS="characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true" \
    jboss/keycloak:10.0.0 \
      -Dkeycloak.migration.action=import \
      -Dkeycloak.migration.file=/tmp/keycloak-export/demo.json \
      -Dkeycloak.migration.provider=singleFile \
      -Dkeycloak.migration.strategy=OVERWRITE_EXISTING
```


