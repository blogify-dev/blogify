FROM openjdk:8-jre

WORKDIR /var/server/

ADD build/dist/jar/blogify-alpha-0.0.1-all.jar .

EXPOSE 8080

CMD ["java", "-server", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-XX:InitialRAMFraction=2", "-XX:MinRAMFraction=2", "-XX:MaxRAMFraction=2", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-jar", "blogify-alpha-0.0.1-all.jar"]