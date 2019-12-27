FROM adoptopenjdk/openjdk8 AS EXPLODED
COPY target/kerberos-spring-cloud-gateway-1.0-SNAPSHOT.jar /app.jar
RUN mkdir /app && cd app && jar xf /app.jar

FROM adoptopenjdk/openjdk8
COPY --from=exploded /app/BOOT-INF/lib /app/lib
COPY --from=exploded /app/META-INF /app/META-INF
COPY --from=exploded /app/BOOT-INF/classes /app

VOLUME /conf

ENV SERVICE_ENDPOINT http://some-service
ENV SERVICE_PATH /some-path
ENV SERVICE_PRINCIPAL service/localhost
ENV KRB5_CONF /conf/krb5.conf
ENV KEYTAB_LOCATION /conf/gateway.keytab

WORKDIR /app
EXPOSE 8080

ENTRYPOINT java -cp "/app:/app/lib/*" com.fha.kerberos.scg.Application ${KRB5_CONF}

