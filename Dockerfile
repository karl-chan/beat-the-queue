FROM sbtscala/scala-sbt:eclipse-temurin-11.0.17_8_1.8.2_3.2.1
WORKDIR /app
COPY . /app
ENV SBT_OPTS="-Xmx400m"
CMD ["sbt", "runMain com.github.karlchan.beatthequeue.server.Server"]
EXPOSE 8080
