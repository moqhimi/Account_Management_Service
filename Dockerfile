FROM openjdk:8
VOLUME /tmp
COPY target/Accountservice.jar /app.jar
RUN sh -c 'touch /app.jar'
ENV JAVA_OPTS=""
ENTRYPOINT ["java", "-jar", "/app.jar" ]
