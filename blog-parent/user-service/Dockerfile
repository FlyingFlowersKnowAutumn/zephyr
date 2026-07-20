FROM docker.m.daocloud.io/amazoncorretto:21
WORKDIR /app
COPY target/*.jar user.jar
ENTRYPOINT ["java","-Xms128m","-Xmx256m","-jar","user.jar"]
