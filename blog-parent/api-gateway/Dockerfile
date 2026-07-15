FROM docker.m.daocloud.io/amazoncorretto:21
WORKDIR /app
COPY target/*.jar gateway.jar
ENTRYPOINT ["java","-Xms128m","-Xmx256m","-jar","gateway.jar"]
EXPOSE 8080
