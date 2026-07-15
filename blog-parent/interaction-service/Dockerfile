FROM docker.m.daocloud.io/amazoncorretto:21
WORKDIR /app
COPY target/*.jar interaction.jar
ENTRYPOINT ["java","-Xms128m","-Xmx256m","-jar","interaction.jar"]
