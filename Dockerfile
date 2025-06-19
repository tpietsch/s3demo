FROM maven:3.9.5-amazoncorretto-17-debian
COPY . .
RUN mvn clean package
CMD ["java", "-jar", "target/aptible-1.0.0-SNAPSHOT.jar"]
