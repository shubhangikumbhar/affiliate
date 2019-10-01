FROM openjdk:11-jdk
RUN mkdir /app
COPY ./build/libs/affiliate-service-0.0.1-SNAPSHOT.jar /app
WORKDIR /app
EXPOSE 8087
ENTRYPOINT ["java","-jar", "affiliate-service-0.0.1-SNAPSHOT.jar"]
