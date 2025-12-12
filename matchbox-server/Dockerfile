FROM bellsoft/liberica-openjdk-alpine:latest
EXPOSE 80
WORKDIR /app

COPY ./target/matchbox.jar matchbox.jar

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'
RUN mkdir -p data/hapi/lucenefiles && chmod 775 data/hapi/lucenefiles
RUN apk --purge del

RUN mkdir igs && chmod 775 igs

# Create a new user and switch to it
RUN adduser -D matchbox --disabled-password --gecos '' matchbox
ENV HOME=/home/matchbox
RUN mkdir -p database && chown matchbox:matchbox database
RUN mkdir -p config && chown matchbox:matchbox config
RUN chown matchbox:matchbox /app
COPY with-ca/application.yaml config
COPY with-ca/fhir-settings.json config
COPY with-ca/ca.on.oh-ereferral-econsult-0.12.0-alpha1.0.7-snapshots.tgz igs

USER matchbox

ENTRYPOINT ["java", "-Xmx3072M", "-Dfhir.settings.path=config/fhir-settings.json", "-Dspring.config.additional-location=file:config/application.yaml", "-jar", "matchbox.jar"]

