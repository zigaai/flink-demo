# # FROM openjdk:17-alpine
FROM registry.cn-hongkong.aliyuncs.com/zigaai/flink:1.17.2-java8
WORKDIR /opt/flink
#ARG JAR_FILE=target/*.jar
ARG JAR_FILE=target/FlinkCDCTest-1.0-SNAPSHOT.jar
COPY ${JAR_FILE} /opt/flink/app.jar
# RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.ustc.edu.cn/g' /etc/apk/repositories
# ENTRYPOINT ["java", "-jar", "/opt/flink/app.jar"]