FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/enlive-test-0.0.1-SNAPSHOT-standalone.jar /enlive-test/app.jar

EXPOSE 8080

CMD ["java", "-jar", "/enlive-test/app.jar"]
