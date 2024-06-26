FROM openjdk:11
EXPOSE 8080
ADD target/movie-0.0.1-SNAPSHOT.jar movieapp.jar
ENTRYPOINT ["java", "-jar", "/movieapp.jar"]