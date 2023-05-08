# FROM openjdk:17-alpine
FROM openjdk:17-jdk-slim as build

ARG WORK_DIR=/opt/authn-service
ARG ARTIFACT_NAME=weshopify-authn-service.jar
ARG SERVICE_PORT=5022

ENV FINAL_ARTIFACT=${ARTIFACT_NAME}

RUN mkdir ${WORK_DIR}

WORKDIR ${WORK_DIR}

COPY target/${ARTIFACT_NAME} ${WORK_DIR}

EXPOSE ${SERVICE_PORT}

CMD [ "sh","-c", "java -jar ${FINAL_ARTIFACT}"]
