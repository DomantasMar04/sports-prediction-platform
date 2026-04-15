# Naudojame Java 17 arba 21 (priklauso nuo tavo projekto)
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Nukopijuojame Maven wrapperį ir pom.xml
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

# Nukopijuojame kodą ir sugeneruojame .jar failą
COPY src ./src
RUN ./mvnw package -DskipTests

# Paleidžiame aplikaciją
EXPOSE 8080
CMD ["java", "-jar", "target/*.jar"]