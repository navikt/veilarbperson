FROM gcr.io/distroless/java21-debian12
COPY /target/veilarbperson.jar app.jar
CMD ["app.jar"]