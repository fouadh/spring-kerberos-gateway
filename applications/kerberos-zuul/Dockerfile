FROM adoptopenjdk/openjdk8 AS EXPLODED
COPY target/kerberos-zuul-1.0-SNAPSHOT.jar /app.jar
RUN mkdir /app && cd app && jar xf /app.jar

FROM adoptopenjdk/openjdk8
COPY --from=exploded /app/BOOT-INF/lib /app/lib
COPY --from=exploded /app/META-INF /app/META-INF
COPY --from=exploded /app/BOOT-INF/classes /app

VOLUME /tmp

ENV SERVICE_ENDPOINT http://some-service
ENV SERVICE_PATH /some-path
ENV KRB5_CONF /tmp/somewhere/krb5.conf
ENV SERVICE_PRINCIPAL service/localhost
ENV KEYTAB_LOCATION /tmp/somelocation

WORKDIR /app
EXPOSE 8080

ENTRYPOINT java -cp "/app:/app/lib/*" com.fha.kerberos.gateway.zuul.Application ${KRB5_CONF}

