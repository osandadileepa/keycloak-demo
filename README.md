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

## Keycloak Export

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

copy the tegra.json file to /tmp then run the following to import

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

