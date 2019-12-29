FROM openjdk:10-jre

WORKDIR /var/server/

ADD build/dist/jar/blogify-0.1.0-all.jar .

EXPOSE 80
EXPOSE 443

CMD ["java", "-server", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-jar", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "blogify-0.1.0-all.jar"]