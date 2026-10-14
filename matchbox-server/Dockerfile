FROM bellsoft/liberica-openjdk-debian:26
EXPOSE 8080
WORKDIR /app

# Apply latest Debian security patches to the base image
RUN apt-get update \
 && apt-get upgrade -y \
 && rm -rf /var/lib/apt/lists/*

COPY ./target/matchbox.jar matchbox.jar

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'
RUN mkdir -p data/hapi/lucenefiles && chmod 775 data/hapi/lucenefiles

RUN mkdir igs && chmod 775 igs

# Create a new user and switch to it
RUN adduser --disabled-password --gecos '' matchbox
ENV HOME=/home/matchbox
RUN mkdir -p database && chown matchbox:matchbox database
RUN mkdir -p config && chown matchbox:matchbox config
RUN chown matchbox:matchbox /app
COPY with-ca/application.yaml config
COPY with-ca/fhir-settings.json config
COPY with-ca/ca.on.oh-ereferral-econsult-0.12.0-alpha1.0.7-snapshots.tgz igs
COPY with-ca/hl7.fhir.ca.baseline-1.0.tgz igs
COPY with-ca/hl7.fhir.uv.subscriptions-backport.r4-1.2.0-ballot.tgz igs
#COPY with-ca/ca.infoway.io.halo-1.1.1-dft-preballot-snapshots.tgz igs

USER matchbox

# App listens on 8080 (EB Dockerrun ContainerPort), context path /matchboxv3
ENV HEALTHCHECK_URL=http://localhost:8080/matchboxv3/actuator/health

HEALTHCHECK --interval=10s --timeout=3s --start-period=300s --retries=3 \
  CMD curl -sf $HEALTHCHECK_URL || exit 1

ENTRYPOINT ["java", "-Xms14g", "-Xmx14g", "-Dfhir.settings.path=config/fhir-settings.json", "-Dspring.config.additional-location=file:config/application.yaml", "-jar", "matchbox.jar"]
