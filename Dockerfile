FROM eclipse-temurin:17-jdk
WORKDIR /app
ADD https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/9.1.0/mysql-connector-j-9.1.0.jar /app/mysql-connector-j.jar
COPY src ./src
RUN mkdir out && javac -cp mysql-connector-j.jar -d out src/*.java
CMD ["java", "-cp", "out:mysql-connector-j.jar", "WebApp"]
