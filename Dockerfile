FROM openjdk:10-jre

WORKDIR /var/server/

ADD build/dist/jar/blogify-alpha-0.0.1-all.jar .

EXPOSE 8080
EXPOSE 5005

CMD ["java", "-server", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-jar", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "blogify-alpha-0.0.1-all.jar"]