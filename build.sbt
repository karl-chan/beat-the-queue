val scala3Version = "3.3.0"

val circeVersion = "0.14.3"
val emilVersion = "0.13.0"
val http4sVersion = "0.23.13"
val mongo4catsVersion = "0.6.5"
val sttpVersion = "3.8.8"

lazy val root = project
  .in(file("."))
  .settings(
    name := "Beat the Queue",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    scalaVersion := "3.3.0",
    semanticdbEnabled := true,
    libraryDependencies ++= Seq(
      // java dependencies
      "ch.qos.logback" % "logback-classic" % "1.4.5",
      "org.jsoup" % "jsoup" % "1.15.3",
      "nl.martijndwars" % "web-push" % "5.1.1"
    ) ++ Seq(
      // scala 3 dependencies
      "com.github.eikek" %% "emil-common" % emilVersion,
      "com.github.eikek" %% "emil-javamail" % emilVersion,
      "com.github.jwt-scala" %% "jwt-circe" % "9.1.2",
      "com.softwaremill.quicklens" %% "quicklens" % "1.9.0",
      "com.softwaremill.sttp.client3" %% "armeria-backend-cats" % sttpVersion,
      "com.softwaremill.sttp.client3" %% "cats" % sttpVersion,
      "com.softwaremill.sttp.client3" %% "circe" % sttpVersion,
      "com.softwaremill.sttp.client3" %% "core" % sttpVersion,
      "com.softwaremill.sttp.client3" %% "slf4j-backend" % sttpVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.github.jmcardon" %% "tsec-http4s" % "0.4.0",
      "io.github.kirill5k" %% "mongo4cats-circe" % mongo4catsVersion,
      "io.github.kirill5k" %% "mongo4cats-core" % mongo4catsVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.scalatest" %% "scalatest" % "3.2.15" % Test,
      "org.typelevel" %% "cats-core" % "2.9.0",
      "org.typelevel" %% "cats-effect" % "3.4.4",
      "org.typelevel" %% "cats-effect-testing-scalatest" % "1.5.0" % Test,
      "org.typelevel" %% "log4cats-slf4j" % "2.5.0"
    ) ++ Seq(
      // scala 2 dependencies
      "com.github.pureconfig" %% "pureconfig" % "0.17.2",
      "com.lihaoyi" %% "scalatags" % "0.12.0"
    ).map(_.cross(CrossVersion.for3Use2_13)),
    // plugin configurations
    mainClass in (Compile, run) := Some(
      "com.github.karlchan.beatthequeue.Main"
    ),
    reStart / mainClass := Some(
      "com.github.karlchan.beatthequeue.server.Server"
    ),
    Revolver.enableDebugging()
  )
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .enablePlugins(GraalVMNativeImagePlugin)
