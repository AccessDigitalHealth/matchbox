# Running matchbox in docker

You can run matchbox directly in docker.

```bash
docker run -d --name europe-west6-docker.pkg.dev/ahdis-ch/ahdis/matchbox:latest -p 8080:8080 -e matchbox.fhir.context.onlyOneEngine=true -v ${PWD}/config/:/config/ matchbox
docker logs --follow matchbox
```
If you see A 'FHIR has been lit on this server' you can point your browser to http://localhost:8080/matchboxv3/fhir/metadata.

The optional local volume /Users/oliveregger/config/ will be mapped inside the container and Matchbox will use [fhir-settings.json](https://confluence.hl7.org/display/FHIR/Using+fhir-settings.json) and application.yaml for additional configuration see [https://github.com/ahdis/matchbox/tree/main/matchbox-server](https://github.com/ahdis/matchbox/tree/main/matchbox-server) different directories started with with-xxx for sample configurations.

The parameter (matchbox.fhir.context.onlyOneEngine) is to set development environment, which allows you to create/update conformance resources (e.g. transform StructureMaps). If not provided, you need to provide the conformance resources by an FHIR Implementation Guide.

We recommend to put at least 2.5 GB of RAM for the container instance, depending on how many ImplementationGuides's you plan to install and want to use.

## Live and Readyness checks

To check if the container is live and ready you can check the health:

```http
GET http:///localhost:8080/matchboxv3/actuator/health HTTP/1.1
Accept: application/vnd.spring-boot.actuator.v3+json

HTTP/1.1 200
Content-Type: application/vnd.spring-boot.actuator.v3+json
Transfer-Encoding: chunked
Date: Thu, 02 Feb 2023 15:55:12 GMT
Via: 1.1 google
Alt-Svc: h3=":443"; ma=2592000,h3-29=":443"; ma=2592000
Connection: close

{
  "status": "UP",
  "groups": [
    "liveness",
    "readiness"
  ]
}
```

You can also use actuator/health/liveness or actuator/health/readiness.

To check the amount of memory used by the jvm use: /actuator/metrics/jvm.memory.used

## Using docker-compose with a persistent postgreSQL database

To use docker-compose with Matchbox you need to checkout Matchbox from [github](https://github.com/ahdis/matchbox).

The database will be stored in the "data" directory. The configuration can be found in the "with-postgres" directory or in the "with-preload" directory.

Change to either with-posgres directory or the with-preload directory

For the first time, you might need to do

```
mkdir data
docker-compose up matchbox-db
```

that the database gets initialized before Matchbox is starting up (needs a fix)

```
docker-compose up
```

Matchbox will be available at [http://localhost:8080/matchboxv3/fhir](http://localhost:8080/matchboxv3/fhir)
Matchbox-gui will be available at [http://localhost:8080/matchboxv3/#/](http://localhost:8080/matchboxv3/#/)

Export the DB data:

```
docker-compose exec -T matchbox-test-db pg_dump -Fc -U matchbox matchbox > mydump
```

Reimport the DB data:

```
docker-compose exec -T matchbox-test-db pg_restore -c -U matchbox -d matchbox < mydump
```

## Configure a own docker image with preinstalled packages

During a regular container startup all implementation guides will be deployed to the database. This packages can be provided by the

1. fhir package servers
2. absolute http address to package
3. classpath
4. filesystem

If you want to prepare a container which does not need internet access during the startup (required by 1 and 2) you can
build a new container image will do the download and installation packages already during the startup process (adding this line
into the Dockerfile):

```
RUN java -Xmx1G -Xms1G -jar /matchbox.jar --hapi.fhir.only_install_packages=true
```
