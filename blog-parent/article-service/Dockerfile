FROM docker.m.daocloud.io/amazoncorretto:21
WORKDIR /app
COPY target/*.jar article.jar
ENTRYPOINT ["java","-Xms128m","-Xmx256m","-jar","article.jar"]
