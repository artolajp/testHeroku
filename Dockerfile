FROM openjdk:8-alpine
EXPOSE 8090:8080
RUN mkdir /app
WORKDIR /app/bin
COPY ./build/install/creating-http-api-ktor/ /app/
CMD ["./creating-http-api-ktor"]