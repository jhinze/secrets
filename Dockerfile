FROM quay.io/quarkus/ubi-quarkus-native-image:21.3-java11 as builder
WORKDIR /code
USER root
ENV JAVA_OPTS="-Xms6g -Xmx6g"
ENV MAVEN_OPTS="-Xms6g -Xmx6g"
RUN microdnf install npm
RUN npm cache clean -f
RUN npm install -g n
RUN n stable
RUN npm install -g yarn
ADD . /code
RUN (cd /code/web && yarn install --network-timeout 1000000)
RUN (cd /code/web && yarn build)
RUN (cd /code/secret-api && ./mvnw -B org.apache.maven.plugins:maven-dependency-plugin:3.1.2:go-offline)
RUN mkdir -p /code/secret-api/src/main/resources/META-INF/resources
RUN cp -r /code/web/build/* /code/secret-api/src/main/resources/META-INF/resources/
RUN (cd /code/secret-api && ./mvnw package -Pnative -Dquarkus.native.native-image-xmx=6g)

FROM registry.access.redhat.com/ubi8/ubi-minimal:8.4
WORKDIR /work/
RUN chown 1001 /work \
    && chmod "g+rwX" /work \
    && chown 1001:root /work
COPY --from=builder --chown=1001:root /code/secret-api/target/*-runner /work/application
EXPOSE 8080
USER 1001
CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]