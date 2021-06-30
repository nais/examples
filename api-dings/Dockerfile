FROM navikt/java:14
ENV JAVA_OPTS="-Dlogback.configurationFile=logback-remote.xml -Dhttps.protocols=TLSv1.3"
COPY build/libs/*-all.jar app.jar
