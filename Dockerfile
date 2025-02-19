FROM gcr.io/distroless/java21-debian12:nonroot
LABEL org.opencontainers.image.source="https://github.com/navikt/veilarbperson"

WORKDIR /app

COPY /target/veilarbperson.jar app.jar

USER nonroot

ENV LANG='nb_NO.UTF-8' LC_ALL='nb_NO.UTF-8' TZ="Europe/Oslo"

ENV TZ="Europe/Oslo"
EXPOSE 8080
CMD ["-jar", "app.jar"]