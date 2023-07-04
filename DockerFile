FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/*.jar vms.jar
ENTRYPOINT ["java","-jar","/vms.jar"]
EXPOSE 7009