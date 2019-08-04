FROM openjdk:jre

RUN apt-get update && apt-get install -y \
  libgtk2.0-0 
ADD target/${project.build.finalName}.jar /target/
ADD target/${resources.external} /target/${resources.external}/
ENTRYPOINT ["java", "-jar", "/target/${project.build.finalName}.jar"]