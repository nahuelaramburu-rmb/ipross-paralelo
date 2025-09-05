FROM azul/zulu-openjdk:11.0.12

LABEL version="1.0"
LABEL maintainer="Sampietro, Martin"
LABEL "com.capacidad.validationapi"="Spring Boot Validation API Docker Image"

EXPOSE ${PORT}

RUN mkdir -p /app/

WORKDIR /app/

COPY ./build/libs/validation-api-0.0.1-SNAPSHOT.jar /app/

CMD java -jar -Dspring.profiles.active=$PROFILE /app/validation-api-0.0.1-SNAPSHOT.jar