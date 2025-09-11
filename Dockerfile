FROM azul/zulu-openjdk:11.0.3

LABEL version="1.0"
LABEL maintainer="Sampietro, Martin"
LABEL "com.capacidad.identityservice"="Spring Boot Identity Service Docker Image"

ARG ARTIFACT=identity-service-0.0.1-SNAPSHOT.jar

EXPOSE ${PORT}

RUN mkdir -p /app/

WORKDIR /app/

COPY ./build/libs/identity-service-0.0.1-SNAPSHOT.jar /app/

CMD java -jar -Dspring.profiles.active=$PROFILE /app/identity-service-0.0.1-SNAPSHOT.jar